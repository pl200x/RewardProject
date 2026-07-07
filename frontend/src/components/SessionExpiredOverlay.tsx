import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/auth/AuthContext'
import { subscribeSession, clearSessionExpired, isSessionExpired } from '@/ui/session'

/**
 * Full-screen blocking overlay shown when the session expires.
 * Clicking Back to welcome page clears the session and returns to the start page.
 */
export default function SessionExpiredOverlay() {
  const [show, setShow] = useState(isSessionExpired())
  const nav = useNavigate()
  const { logout } = useAuth()

  useEffect(() => subscribeSession(setShow), [])

  if (!show) return null

  const backToWelcome = () => {
    clearSessionExpired()
    logout()
    nav('/', { replace: true })
  }

  return (
    <div className="mm-session">
      <div className="mm-session__card">
        <div className="mm-session__icon">⏱</div>
        <h2 className="mm-session__title">Your session has been expired</h2>
        <p className="mm-session__sub">Your login session has expired. Please return to the welcome page and sign in again.</p>
        <button className="mm-btn mm-btn--primary mm-btn--lg" onClick={backToWelcome}>
          Back to welcome page
        </button>
      </div>
    </div>
  )
}
