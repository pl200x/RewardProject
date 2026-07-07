import type { ReactNode } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'

/** Admin shell with top navigation and centered content. */
export default function AdminShell({ children }: { children: ReactNode }) {
  const nav = useNavigate()
  const loc = useLocation()
  const atHome = loc.pathname === '/admin'
  return (
    <div className="ad-shell">
      <header className="ad-bar">
        <button className="ad-brand" onClick={() => nav('/admin')} title="Admin home">
          🛠️ Music Map · Admin
        </button>
        <div className="ad-bar__right">
          {!atHome && (
            <button className="ad-btn ad-btn--ghost" onClick={() => nav('/admin')}>
              ← Admin Home
            </button>
          )}
          <button className="ad-btn ad-btn--ghost" onClick={() => nav('/home')}>
            Back to Music Map
          </button>
        </div>
      </header>
      <main className="ad-main">{children}</main>
    </div>
  )
}
