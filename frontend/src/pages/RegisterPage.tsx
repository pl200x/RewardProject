import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { musicApi } from '@/api/music'
import { useAuth } from '@/auth/AuthContext'
import { toast } from '@/ui/toast'

/**
 * Registration page. The backend RegisterCmd requires name/password plus
 * age/gender/email/job/interest. This form collects username, email, and
 * password, then defaults the rest and sends users to interest onboarding.
 */
export default function RegisterPage() {
  const nav = useNavigate()
  const { login } = useAuth()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [busy, setBusy] = useState(false)

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!name.trim() || !password) {
      toast.error('Username and password are required.')
      return
    }
    if (password !== confirm) {
      toast.error('The two passwords do not match.')
      return
    }
    setBusy(true)
    try {
      const reg = await musicApi.register({
        name: name.trim(),
        password,
        age: 0,
        gender: 0,
        email: email.trim(),
        job: '',
        interest: '',
      })
      if (!reg.success) {
        setBusy(false) // The interceptor shows failures such as duplicate usernames.
        return
      }
      // Auto-login to get a token and write the session.
      const resp = await musicApi.login(name.trim(), password)
      if (!resp.baseVO?.success || !resp.token || resp.userId == null) {
        toast.info('Registration successful. Please log in.')
        nav('/login', { replace: true })
        return
      }
      const uname = resp.userName ?? name.trim()
      login({ token: resp.token, userId: resp.userId, userName: uname, interest: '' })
      toast.success(`Login successful! Welcome, ${uname}`)
      window.setTimeout(() => nav('/interests?mode=onboarding', { replace: true }), 1100)
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
        <h1 className="mm-auth__title">Register</h1>

        <label className="mm-field">
          <span className="mm-field__label">Username</span>
          <input className="mm-input" value={name} onChange={(e) => setName(e.target.value)} autoFocus />
        </label>
        <label className="mm-field">
          <span className="mm-field__label">Email</span>
          <input
            className="mm-input"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Optional"
          />
        </label>
        <label className="mm-field">
          <span className="mm-field__label">Password</span>
          <input
            className="mm-input"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
          />
        </label>
        <label className="mm-field">
          <span className="mm-field__label">Confirm Password</span>
          <input
            className="mm-input"
            type="password"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            autoComplete="new-password"
          />
        </label>

        <button className="mm-btn mm-btn--primary mm-btn--block" type="submit" disabled={busy}>
          {busy ? 'Registering...' : 'Register'}
        </button>

        <p className="mm-auth__foot">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </form>
    </div>
  )
}
