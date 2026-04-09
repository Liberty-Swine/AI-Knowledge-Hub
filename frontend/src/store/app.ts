import { defineStore } from 'pinia'
import { http } from '../api/http'
import type { ChatSession, KnowledgeBase, Result } from '../api/types'

/**
 * @Description 全局应用状态：当前选中知识库与会话（用于页面联动）
 */
export const useAppStore = defineStore('app', {
  state: () => ({
    kbId: localStorage.getItem('kbId') || '',
    sessionId: localStorage.getItem('sessionId') || '',
    knowledgeBases: [] as KnowledgeBase[],
    sessions: [] as ChatSession[],
    loadingKb: false,
    loadingSessions: false,
  }),
  actions: {
    setKbId(kbId: string) {
      this.kbId = kbId
      localStorage.setItem('kbId', kbId)
      // 切换知识库后默认清空会话
      this.setSessionId('')
    },
    setSessionId(sessionId: string) {
      this.sessionId = sessionId
      localStorage.setItem('sessionId', sessionId)
    },
    async loadKnowledgeBases() {
      this.loadingKb = true
      try {
        const resp = await http.get<Result<KnowledgeBase[]>>('/api/knowledge-bases')
        this.knowledgeBases = resp.data.data || []
      } finally {
        this.loadingKb = false
      }
    },
    async loadSessionsByKbId(kbId?: string) {
      const id = (kbId ?? this.kbId).trim()
      if (!id) {
        this.sessions = []
        return
      }
      this.loadingSessions = true
      try {
        const resp = await http.get<Result<ChatSession[]>>(`/api/chat/sessions?kbId=${encodeURIComponent(id)}`)
        this.sessions = resp.data.data || []
      } finally {
        this.loadingSessions = false
      }
    },
  },
})

