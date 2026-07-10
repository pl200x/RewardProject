import { useState, type ReactNode } from 'react'
import type { MusicDigest } from '@/api/types'
import MusicCard from './MusicCard'

/** Items per page; also the row count the fixed column height is based on. */
const PAGE_SIZE = 10
const TOP_N_MIN = 1
const TOP_N_MAX = 50

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
  /** Current Top-N setting; enables the header input and fixed-height list. */
  topN?: number
  /** Called with the clamped value when the user commits a new Top-N. */
  onTopNChange?: (n: number) => void
}

/** Header input for Top-N; commits (clamped to 1..50) on Enter or blur. */
function TopNInput({ topN, onCommit }: { topN: number; onCommit: (n: number) => void }) {
  const [draft, setDraft] = useState(String(topN))
  const commit = () => {
    const parsed = Number.parseInt(draft, 10)
    const next = Number.isNaN(parsed) ? topN : Math.min(TOP_N_MAX, Math.max(TOP_N_MIN, parsed))
    setDraft(String(next))
    if (next !== topN) onCommit(next)
  }
  return (
    <label className="mm-col__topn" title={`How many songs to show (${TOP_N_MIN}-${TOP_N_MAX})`}>
      Top
      <input
        className="mm-col__topn-input"
        type="number"
        min={TOP_N_MIN}
        max={TOP_N_MAX}
        value={draft}
        onChange={(e) => setDraft(e.target.value)}
        onBlur={commit}
        onKeyDown={(e) => {
          if (e.key === 'Enter') (e.target as HTMLInputElement).blur()
        }}
        aria-label="Number of songs to show"
      />
    </label>
  )
}

/**
 * Shared home-page column for Top, Recent, and recommendations.
 * Uses normal document flow instead of absolute positioning.
 * With `topN` set, the list keeps a fixed 10-row height (fewer items leave
 * blank space) and anything beyond 10 items is paged client-side.
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
  topN,
  onTopNChange,
}: Props) {
  // Clamp instead of resetting so background refreshes (e.g. after a like)
  // keep the user on their current page.
  const [page, setPage] = useState(1)
  const totalPages = Math.max(1, Math.ceil(items.length / PAGE_SIZE))
  const safePage = Math.min(page, totalPages)
  const pageItems = items.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE)
  const rankOffset = (safePage - 1) * PAGE_SIZE
  const fixed = topN != null

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
      <header className={`mm-col__head${topN != null && onTopNChange ? ' mm-col__head--row' : ''}`}>
        <div>
          <h2 className="mm-col__title">{title}</h2>
          {subtitle && <p className="mm-col__sub">{subtitle}</p>}
        </div>
        {topN != null && onTopNChange && <TopNInput topN={topN} onCommit={onTopNChange} />}
      </header>

      {loading ? (
        <ul className={`mm-col__list${fixed ? ' mm-col__list--fixed' : ''}`}>
          {Array.from({ length: 6 }).map((_, i) => (
            <li key={i} className="mm-skel" />
          ))}
        </ul>
      ) : items.length === 0 ? (
        <div className={`mm-col__empty${fixed ? ' mm-col__empty--fixed' : ''}`}>{empty ?? 'No data yet'}</div>
      ) : (
        <>
          <ol className={`mm-col__list${fixed ? ' mm-col__list--fixed' : ''}`}>
            {pageItems.map((m, i) => (
              <li key={m.id}>
                <MusicCard
                  item={m}
                  rank={showRank ? rankOffset + i + 1 : undefined}
                  onClick={onItemClick ? () => onItemClick(m) : undefined}
                />
              </li>
            ))}
          </ol>
          {totalPages > 1 && (
            <nav className="mm-pager" aria-label={`${title} pages`}>
              <button
                className="mm-pager__btn"
                disabled={safePage === 1}
                onClick={() => setPage(safePage - 1)}
                aria-label="Previous page"
              >
                ‹
              </button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
                <button
                  key={p}
                  className={`mm-pager__btn${p === safePage ? ' mm-pager__btn--active' : ''}`}
                  onClick={() => setPage(p)}
                  aria-current={p === safePage ? 'page' : undefined}
                >
                  {p}
                </button>
              ))}
              <button
                className="mm-pager__btn"
                disabled={safePage === totalPages}
                onClick={() => setPage(safePage + 1)}
                aria-label="Next page"
              >
                ›
              </button>
            </nav>
          )}
        </>
      )}
    </section>
  )
}
