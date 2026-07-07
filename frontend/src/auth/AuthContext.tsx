import { createContext, useContext, useState, type ReactNode } from 'react'
import { getStoredAuth, setStoredAuth, type StoredAuth } from '@/api/client'

interface AuthCtx {
  auth: StoredAuth | null
  isLoggedIn: boolean
  login: (a: StoredAuth) => void
  logout: () => void
  /** Sync session interest after saving so onboarding/edit prefills stay consistent. */
  setInterest: (interest: string) => void
  /** Sync session avatar after changes so the header and comments update immediately. */
  setAvatar: (avatar: number) => void
}

const Ctx = createContext<AuthCtx>(null!)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState<StoredAuth | null>(() => getStoredAuth())
  const login = (a: StoredAuth) => {
    setStoredAuth(a)
    setAuth(a)
  }
  const logout = () => {
    setStoredAuth(null)
    setAuth(null)
  }
  const setInterest = (interest: string) => {
    setAuth((prev) => {
      if (!prev) return prev
      const next = { ...prev, interest }
      setStoredAuth(next)
      return next
    })
  }
  const setAvatar = (avatar: number) => {
    setAuth((prev) => {
      if (!prev) return prev
      const next = { ...prev, avatar }
      setStoredAuth(next)
      return next
    })
  }
  return (
    <Ctx.Provider value={{ auth, isLoggedIn: !!auth, login, logout, setInterest, setAvatar }}>{children}</Ctx.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(Ctx)
