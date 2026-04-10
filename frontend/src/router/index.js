import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/projects', component: () => import('../views/Projects.vue') },
  { path: '/projects/:id', component: () => import('../views/ProjectDetail.vue') },
  { path: '/articles', component: () => import('../views/Articles.vue') },
  { path: '/articles/:id', component: () => import('../views/ArticleDetail.vue') },
  { 
    path: '/admin', 
    component: () => import('../views/admin/Admin.vue'),
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'projects', component: () => import('../views/admin/ProjectManage.vue') },
      { path: 'articles', component: () => import('../views/admin/ArticleManage.vue') }
    ]
  }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})