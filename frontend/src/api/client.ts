import axios, { type AxiosInstance } from 'axios'
import { toast } from '@/ui/toast'
import { expireSession } from '@/ui/session'

// Detect auth/session-expired errors from backend responses or JWT 401.
function textFromCodes(codes: string): string {
  return String.fromCharCode(...codes.split(',').map((code) => Number.parseInt(code, 10)))
}

function looksLikeAuthError(base: any): boolean {
  if (base?.code === 401) return true
  const msg = String(base?.errorMessage || '').toLowerCase()
  const notLoggedIn = textFromCodes('26410,30331,24405')
  const loginFirst = textFromCodes('35831,20808,30331,24405')
  const token = textFromCodes('20196,29260')
  return (
    msg.includes('login first') ||
    msg.includes(notLoggedIn) ||
    msg.includes(loginFirst) ||
    msg.includes(token)
  )
}

// ---- Auth storage (localStorage) ----
const AUTH_KEY = 'reward.auth'
export interface StoredAuth {
  token: string
  userId: number
  userName: string
  /** Current user interests as a comma-separated string. */
  interest?: string
  /** Default avatar index 0-9. */
  avatar?: number
}
export function getStoredAuth(): StoredAuth | null {
  try {
    const s = localStorage.getItem(AUTH_KEY)
    return s ? (JSON.parse(s) as StoredAuth) : null
  } catch {
    return null
  }
}
export function setStoredAuth(a: StoredAuth | null) {
  if (a) localStorage.setItem(AUTH_KEY, JSON.stringify(a))
  else localStorage.removeItem(AUTH_KEY)
}

// ---- axios client factory ----
function createClient(baseURL: string, withAuth = false): AxiosInstance {
  const inst = axios.create({ baseURL, timeout: 10000 })

  if (withAuth) {
    inst.interceptors.request.use((config) => {
      const a = getStoredAuth()
      if (a?.token) config.headers.Authorization = `Bearer ${a.token}`
      return config
    })
  }

  inst.interceptors.response.use(
    (resp) => {
      const data: any = resp.data
      const base = data?.baseVO ?? (typeof data?.success === 'boolean' ? data : null)
      // Session expired: if the client has auth and backend reports auth failure, show the full-screen prompt.
      if (base && base.success === false && getStoredAuth() && looksLikeAuthError(base)) {
        expireSession()
        return resp
      }
      // Other business failures use a normal toast unless the request is silent.
      if (base && base.success === false && base.errorMessage && !(resp.config as any)?.silent) {
        toast.error(base.errorMessage)
      }
      return resp
    },
    (error) => {
      const status = error?.response?.status
      if (status === 401) {
        // JWT expired/invalid. Signed-in users see the expiry overlay; guests get a toast.
        if (getStoredAuth()) expireSession()
        else toast.error('Not logged in or token invalid. Please log in first.')
      } else {
        toast.error(error?.message || 'Network error')
      }
      return Promise.reject(error)
    },
  )
  return inst
}

// Music_management uses JWT; other services are not authenticated yet.
export const musicClient = createClient('/api/music', true)
export const rewardClient = createClient('/api/reward')
export const prizeClient = createClient('/api/prize')
