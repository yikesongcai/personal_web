<template>
  <div class="ai-chat-root">
    <button class="toggle-btn" @click="togglePanel">
      {{ open ? '收起助手' : 'AI 导览' }}
    </button>

    <transition name="panel-fade">
      <section v-if="open" class="chat-panel">
        <header class="chat-header">
          <h3>个人站点 AI 导览</h3>
          <p>基于网站项目与文章内容的 RAG 问答</p>
        </header>

        <div ref="messageBox" class="message-box">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-item"
            :class="msg.role"
          >
            <div class="bubble-wrap">
              <div class="bubble">{{ msg.content }}</div>
              <div
                v-if="msg.role === 'assistant' && msg.sources && msg.sources.length"
                class="sources"
              >
                <p class="sources-title">参考来源</p>
                <a
                  v-for="(source, sourceIndex) in msg.sources"
                  :key="`${index}-${sourceIndex}`"
                  class="source-link"
                  :href="source.url"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  {{ source.title }}
                </a>
              </div>
            </div>
          </div>
          <div v-if="loading" class="loading-text">正在生成回答...</div>
        </div>

        <form class="input-row" @submit.prevent="sendQuestion">
          <textarea
            v-model="question"
            rows="2"
            placeholder="例如：Sketched-AirDefense 的核心创新是什么？"
            :disabled="loading"
          />
          <button type="submit" :disabled="loading || !question.trim()">
            发送
          </button>
        </form>
      </section>
    </transition>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue';

const SESSION_KEY = 'personal-web-chat-session-id';

const defaultAssistantMessage = {
  role: 'assistant',
  content: '你好，我可以帮你检索本站的项目作品和文章信息。',
  sources: []
};

const props = defineProps({
  apiUrl: {
    type: String,
    default: '/api/chat'
  },
  historyUrl: {
    type: String,
    default: '/api/chat/history'
  }
});

const open = ref(false);
const loading = ref(false);
const question = ref('');
const sessionId = ref('');
const messages = ref([defaultAssistantMessage]);
const messageBox = ref(null);

onMounted(async () => {
  sessionId.value = getOrCreateSessionId();
  await loadHistory();
});

function togglePanel() {
  open.value = !open.value;
}

async function sendQuestion() {
  const rawQuestion = question.value.trim();
  if (!rawQuestion || loading.value) {
    return;
  }

  messages.value.push({ role: 'user', content: rawQuestion });
  messages.value.push({ role: 'assistant', content: '', sources: [] });

  loading.value = true;
  question.value = '';
  await scrollToBottom();

  try {
    const response = await fetch(props.apiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream'
      },
      body: JSON.stringify({
        question: rawQuestion,
        sessionId: sessionId.value
      })
    });

    if (!response.ok || !response.body) {
      throw new Error('SSE 连接失败');
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        break;
      }

      buffer += decoder.decode(value, { stream: true });
      const chunks = buffer.split('\n\n');
      buffer = chunks.pop() || '';

      for (const chunk of chunks) {
        const event = parseSseEvent(chunk);
        if (!event.data) {
          continue;
        }

        if (event.name === 'token' || event.name === 'message') {
          appendAssistantText(event.data);
          continue;
        }

        if (event.name === 'sources') {
          setAssistantSources(event.data);
          continue;
        }

        if (event.name === 'error') {
          appendAssistantText('\n[错误] ' + event.data);
        }
      }

      await scrollToBottom();
    }
  } catch (error) {
    appendAssistantText('\n[错误] 回答生成失败，请稍后重试。');
  } finally {
    loading.value = false;
    await scrollToBottom();
  }
}

function appendAssistantText(delta) {
  const last = messages.value[messages.value.length - 1];
  if (last && last.role === 'assistant') {
    last.content += delta;
  }
}

function setAssistantSources(raw) {
  const last = messages.value[messages.value.length - 1];
  if (!last || last.role !== 'assistant') {
    return;
  }

  try {
    const parsed = JSON.parse(raw);
    last.sources = Array.isArray(parsed)
      ? parsed.map((item) => ({
          title: item.title || '未命名来源',
          url: item.url || '#',
          sourceType: item.sourceType || 'unknown'
        }))
      : [];
  } catch (error) {
    last.sources = [];
  }
}

function parseSseEvent(chunk) {
  const lines = chunk.split('\n');
  let name = 'message';
  const dataLines = [];

  for (const line of lines) {
    if (line.startsWith('event:')) {
      name = line.replace(/^event:\s?/, '').trim();
    }
    if (line.startsWith('data:')) {
      dataLines.push(line.replace(/^data:\s?/, ''));
    }
  }

  return {
    name,
    data: dataLines.join('\n')
  };
}

async function loadHistory() {
  try {
    const response = await fetch(`${props.historyUrl}/${encodeURIComponent(sessionId.value)}`);
    if (!response.ok) {
      return;
    }

    const turns = await response.json();
    if (!Array.isArray(turns) || turns.length === 0) {
      return;
    }

    const restored = [
      {
        role: 'assistant',
        content: '已恢复你最近的会话记录，你可以继续提问。',
        sources: []
      }
    ];

    for (const turn of turns) {
      restored.push({ role: 'user', content: turn.question || '' });
      restored.push({
        role: 'assistant',
        content: turn.answer || '',
        sources: Array.isArray(turn.sources) ? turn.sources : []
      });
    }

    messages.value = restored;
    await scrollToBottom();
  } catch (error) {
    messages.value = [defaultAssistantMessage];
  }
}

function getOrCreateSessionId() {
  const existing = localStorage.getItem(SESSION_KEY);
  if (existing) {
    return existing;
  }

  const generated = typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID()
    : `session-${Date.now()}`;
  localStorage.setItem(SESSION_KEY, generated);
  return generated;
}

async function scrollToBottom() {
  await nextTick();
  if (messageBox.value) {
    messageBox.value.scrollTop = messageBox.value.scrollHeight;
  }
}
</script>

<style scoped>
.ai-chat-root {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 999;
  font-family: 'IBM Plex Sans', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
}

.toggle-btn {
  border: none;
  border-radius: 999px;
  padding: 12px 18px;
  color: #0f172a;
  background: linear-gradient(120deg, #fef08a, #fdba74);
  box-shadow: 0 12px 28px rgba(249, 115, 22, 0.35);
  font-weight: 700;
  cursor: pointer;
}

.chat-panel {
  width: min(360px, calc(100vw - 24px));
  height: min(560px, calc(100vh - 96px));
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background:
    radial-gradient(circle at 80% 0%, rgba(251, 146, 60, 0.28), transparent 32%),
    radial-gradient(circle at 20% 100%, rgba(45, 212, 191, 0.28), transparent 38%),
    #0b1220;
  backdrop-filter: blur(8px);
  box-shadow: 0 18px 38px rgba(2, 6, 23, 0.42);
}

.chat-header {
  padding: 14px 16px;
  color: #e2e8f0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
}

.chat-header p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.message-box {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message-item {
  display: flex;
}

.bubble-wrap {
  max-width: 85%;
}

.message-item.user {
  justify-content: flex-end;
}

.message-item.assistant {
  justify-content: flex-start;
}

.bubble {
  border-radius: 14px;
  padding: 10px 12px;
  font-size: 14px;
  line-height: 1.45;
  white-space: pre-wrap;
}

.message-item.user .bubble {
  color: #082f49;
  background: linear-gradient(120deg, #67e8f9, #a7f3d0);
}

.message-item.assistant .bubble {
  color: #e2e8f0;
  background: rgba(15, 23, 42, 0.72);
  border: 1px solid rgba(148, 163, 184, 0.25);
}

.loading-text {
  color: #f8fafc;
  font-size: 12px;
  opacity: 0.75;
}

.sources {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.sources-title {
  width: 100%;
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
}

.source-link {
  font-size: 12px;
  text-decoration: none;
  color: #22d3ee;
  padding: 4px 8px;
  border-radius: 999px;
  border: 1px solid rgba(34, 211, 238, 0.35);
  background: rgba(15, 23, 42, 0.72);
}

.input-row {
  padding: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  background: rgba(2, 6, 23, 0.65);
}

.input-row textarea {
  resize: none;
  border: 1px solid rgba(148, 163, 184, 0.32);
  border-radius: 10px;
  padding: 8px 10px;
  background: rgba(15, 23, 42, 0.8);
  color: #e2e8f0;
  outline: none;
}

.input-row button {
  border: none;
  border-radius: 10px;
  min-width: 56px;
  padding: 0 12px;
  background: linear-gradient(120deg, #2dd4bf, #facc15);
  color: #0f172a;
  font-weight: 700;
  cursor: pointer;
}

.input-row button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.panel-fade-enter-active,
.panel-fade-leave-active {
  transition: all 0.26s ease;
}

.panel-fade-enter-from,
.panel-fade-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}

@media (max-width: 640px) {
  .ai-chat-root {
    right: 12px;
    bottom: 12px;
  }

  .toggle-btn {
    padding: 11px 15px;
  }

  .chat-panel {
    width: calc(100vw - 24px);
    height: min(68vh, 520px);
  }
}
</style>
