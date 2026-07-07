import { useNavigate } from 'react-router-dom'
import AdminShell from './AdminShell'

const SECTIONS = [
  { path: '/admin/prizes', icon: '🎁', label: 'Prize Management', desc: 'Add or delete prizes, and adjust coin, gem, and coupon storage' },
  { path: '/admin/rules', icon: '📜', label: 'Reward Rule Management', desc: 'View and edit JEXL rules for stage thresholds and reward amounts' },
  { path: '/admin/music', icon: '🗑️', label: 'Delete Music', desc: 'Take songs offline and remove them from rankings and recommendations' },
  { path: '/admin/records', icon: '📒', label: 'Reward Records', desc: 'Read-only reward ledger and totals, filterable by prize type' },
]

/** Admin home page with large navigation buttons. */
export default function AdminHomePage() {
  const nav = useNavigate()
  return (
    <AdminShell>
      <div className="ad-home">
        <h1 className="ad-home__title">Welcome, Administrator</h1>
        <p className="ad-home__sub">What would you like to do?</p>
        <div className="ad-home__grid">
          {SECTIONS.map((s) => (
            <button key={s.path} className="ad-home__cell" onClick={() => nav(s.path)}>
              <span className="ad-home__icon">{s.icon}</span>
              <span className="ad-home__label">{s.label}</span>
              <span className="ad-home__desc">{s.desc}</span>
            </button>
          ))}
        </div>
      </div>
    </AdminShell>
  )
}
