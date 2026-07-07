import type { ReactNode } from 'react'
import type { MusicDigest } from '@/api/types'
import MusicCard from './MusicCard'

interface Props {
  title: string
  subtitle?: string
  items: MusicDigest[]
  loading?: boolean
  /** Show 1..N rank numbers. */
  showRank?: boolean
  /** Emphasized center column styling. */
  center?: boolean
  /** Empty-state content. */
  empty?: ReactNode
  /** Called when a card is clicked. */
  onItemClick?: (m: MusicDigest) => void
  /** Guest lock: hide content with a blurred list and login prompt. */
  locked?: boolean
  /** Called when the locked login button is clicked. */
  onUnlock?: () => void
}

/**
 * Shared home-page column for Top, Recent, and recommendations.
 * Uses normal document flow instead of absolute positioning.
 */
export default function MusicColumn({
  title,
  subtitle,
  items,
  loading,
  showRank,
  center,
  empty,
  onItemClick,
  locked,
  onUnlock,
}: Props) {
  if (locked) {
    // Guest lock: blurred skeleton rows plus prompt in the same grid cell.
    return (
      <section className="mm-col">
        <header className="mm-col__head">
          <h2 className="mm-col__title">{title}</h2>
          {subtitle && <p className="mm-col__sub">{subtitle}</p>}
        </header>
        <div className="mm-col__lock">
          {/* Ghost rows keep the locked column visually aligned with normal lists. */}
          <ul className="mm-col__list mm-col__lock-bg" aria-hidden="true">
            {Array.from({ length: 10 }).map((_, i) => (
              <li key={i} className="mm-ghost">
                <span className="mm-ghost__cover" style={{ filter: `hue-rotate(${i * 36}deg)` }} />
                <span className="mm-ghost__lines">
                  <span className="mm-ghost__line" />
                  <span className="mm-ghost__line mm-ghost__line--short" />
                </span>
                <span className="mm-ghost__pill" />
              </li>
            ))}
          </ul>
          <div className="mm-col__lock-tip">
            <div className="mm-col__lock-icon">🔒</div>
            <div className="mm-col__lock-text">Login to unlock this feature</div>
            {onUnlock && (
              <button className="mm-btn mm-btn--primary" onClick={onUnlock}>
                Log In
              </button>
            )}
          </div>
        </div>
      </section>
    )
  }

  return (
    <section className={`mm-col${center ? ' mm-col--center' : ''}`}>
      <header className="mm-col__head">
        <h2 className="mm-col__title">{title}</h2>
        {subtitle && <p className="mm-col__sub">{subtitle}</p>}
      </header>

      {loading ? (
        <ul className="mm-col__list">
          {Array.from({ length: 6 }).map((_, i) => (
            <li key={i} className="mm-skel" />
          ))}
        </ul>
      ) : items.length === 0 ? (
        <div className="mm-col__empty">{empty ?? 'No data yet'}</div>
      ) : (
        <ol className="mm-col__list">
          {items.map((m, i) => (
            <li key={m.id}>
              <MusicCard
                item={m}
                rank={showRank ? i + 1 : undefined}
                onClick={onItemClick ? () => onItemClick(m) : undefined}
              />
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}
