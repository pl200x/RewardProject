import type { MusicDigest } from '@/api/types'
import { coverGradient, firstTag } from '@/ui/cover'

interface Props {
  item: MusicDigest
  /** Shows a rank badge (1..N) when provided. */
  rank?: number
  /** Makes the whole card clickable when provided. */
  onClick?: () => void
}

/** Shared song card for columns and search results. */
export default function MusicCard({ item, rank, onClick }: Props) {
  const tag = firstTag(item.tags)
  const initial = (item.title || '?').trim().charAt(0).toUpperCase() || '?'
  return (
    <div
      className={`mm-card${onClick ? ' mm-card--clickable' : ''}`}
      onClick={onClick}
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      onKeyDown={
        onClick
          ? (e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault()
                onClick()
              }
            }
          : undefined
      }
    >
      {rank != null && <div className="mm-card__rank">{rank}</div>}
      <div className="mm-card__cover" style={{ background: coverGradient(item.title || String(item.id)) }}>
        <span>{initial}</span>
      </div>
      <div className="mm-card__info">
        <div className="mm-card__title" title={item.title}>
          {item.title}
        </div>
        <div className="mm-card__artist" title={item.artist}>
          {item.artist || 'Unknown artist'}
        </div>
      </div>
      <div className="mm-card__meta">
        {tag && <span className="mm-card__tag">{tag}</span>}
        {typeof item.score === 'number' && item.score > 0 && (
          <span className="mm-card__score" title="Popularity score">
            ♥ {Math.round(item.score)}
          </span>
        )}
      </div>
    </div>
  )
}
