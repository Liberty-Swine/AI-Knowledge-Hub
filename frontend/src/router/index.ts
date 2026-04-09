import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'

import LoginPage from '../views/LoginPage.vue'
import AppLayout from '../views/layout/AppLayout.vue'
import KnowledgeBasesPage from '../views/KnowledgeBasesPage.vue'
import DocumentsPage from '../views/DocumentsPage.vue'
import SessionsPage from '../views/SessionsPage.vue'
import ChatPage from '../views/ChatPage.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginPage },
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', redirect: '/knowledge-bases' },
        { path: 'knowledge-bases', component: KnowledgeBasesPage },
        { path: 'documents', component: DocumentsPage },
        { path: 'sessions', component: SessionsPage },
        { path: 'chat', component: ChatPage },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path === '/login') return true
  if (!auth.token) return '/login'
  return true
})

