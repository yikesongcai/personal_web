import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/projects', component: () => import('../views/Projects.vue') },
  { path: '/projects/:id', component: () => import('../views/ProjectDetail.vue') },
  { path: '/articles', component: () => import('../views/Articles.vue') },
  { path: '/articles/:id', component: () => import('../views/ArticleDetail.vue') },
  {
    path: '/admin/login',
    component: () => import('../views/admin/AdminLogin.vue')
  },
  { 
    path: '/admin', 
    component: () => import('../views/admin/Admin.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'homepage', component: () => import('../views/admin/HomepageManage.vue') },
      { path: 'projects', component: () => import('../views/admin/ProjectManage.vue') },
      { path: 'articles', component: () => import('../views/admin/ArticleManage.vue') },
      { path: 'rate-limit', component: () => import('../views/admin/RateLimitManage.vue') },
      { path: 'system-log', component: () => import('../views/admin/SystemLogManage.vue') }
    ]
  }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = sessionStorage.getItem('adminToken')
    if (!token) {
      next({ path: '/admin/login' })
    } else {
      next()
    }
  } else {
    next()
  }
})