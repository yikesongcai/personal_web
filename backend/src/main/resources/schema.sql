CREATE TABLE IF NOT EXISTS chat_turn (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(128) NOT NULL,
    user_question CLOB NOT NULL,
    assistant_answer CLOB NOT NULL,
    sources_json CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
