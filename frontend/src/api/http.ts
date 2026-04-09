import axios from 'axios'
import { useAuthStore } from '../store/auth'
import { router } from '../router'

/**
 * Axios instance
 * - baseURL uses Vite proxy
 * - attach Authorization: Bearer <token>
 */
export const http = axios.create({
  baseURL: '',
  timeout: 60_000,
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => resp,
  (err) => {
    if (err?.response?.status === 401) {
      const auth = useAuthStore()
      auth.logout()
      router.push('/login')
    }
    return Promise.reject(err)
  },
)

