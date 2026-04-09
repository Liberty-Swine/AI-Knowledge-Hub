import { defineStore } from 'pinia'

/**
 * JWT auth store
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('jwt') || '',
  }),
  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem('jwt', token)
    },
    logout() {
      this.token = ''
      localStorage.removeItem('jwt')
    },
  },
})

