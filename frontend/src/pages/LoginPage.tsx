import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { musicApi } from '@/api/music'
import { useAuth } from '@/auth/AuthContext'
import { toast } from '@/ui/toast'

export default function LoginPage() {
  const nav = useNavigate()
  const { login } = useAuth()
  const [userName, setUserName] = useState('')
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!userName.trim() || !password) {
      toast.error('Please enter your username and password.')
      return
    }
    setBusy(true)
    try {
      const resp = await musicApi.login(userName.trim(), password)
      if (!resp.baseVO?.success || !resp.token || resp.userId == null) {
        setBusy(false) // The axios interceptor shows the failure message.
        return
      }
      const interest = (resp.interest ?? '').trim()
      const name = resp.userName ?? userName.trim()
      login({ token: resp.token, userId: resp.userId, userName: name, interest, avatar: resp.avatar ?? 0 })
      // Let the welcome toast animate in before navigating.
      toast.success(`Login successful! Welcome, ${name}`)
      const firstTime = !interest
      window.setTimeout(() => {
        nav(firstTime ? '/interests?mode=onboarding' : '/home', { replace: true })
      }, 1100)
    } catch {
      setBusy(false)
    }
  }

  return (
    <div className="mm-auth">
      <form className="mm-auth__card" onSubmit={onSubmit}>
        <Link to="/" className="mm-brand mm-brand--sm">
          <span className="mm-brand__logo">🎵</span>
          <span className="mm-brand__name">Music Map</span>
        </Link>
        <h1 className="mm-auth__title">Log In</h1>

        <label className="mm-field">
          <span className="mm-field__label">Username</span>
          <input
            className="mm-input"
            value={userName}
            onChange={(e) => setUserName(e.target.value)}
            placeholder="Username / Email"
            autoComplete="username"
            autoFocus
          />
        </label>
        <label className="mm-field">
          <span className="mm-field__label">Password</span>
          <input
            className="mm-input"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Password"
            autoComplete="current-password"
          />
        </label>

        <button className="mm-btn mm-btn--primary mm-btn--block" type="submit" disabled={busy}>
          {busy ? 'Logging in...' : 'Log In'}
        </button>

        <p className="mm-auth__foot">
          Don&apos;t have an account yet? <Link to="/register">Register now</Link>
        </p>
      </form>
    </div>
  )
}
