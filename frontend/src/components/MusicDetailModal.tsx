import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import type { MusicDigest } from '@/api/types'
import { coverGradient, firstTag } from '@/ui/cover'

interface Props {
  item: MusicDigest
  liked: boolean
  isLoggedIn: boolean
  busy?: boolean
  onToggleLike: () => void
  onClose: () => void
}

/**
 * Quick detail modal using only list digest data plus likes.
 * Playback lives on the detail page at /music/:id.
 */
export default function MusicDetailModal({ item, liked, isLoggedIn, busy, onToggleLike, onClose }: Props) {
  const nav = useNavigate()

  // Close on Esc.
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  const initial = (item.title || '?').trim().charAt(0).toUpperCase() || '?'
  const tag = firstTag(item.tags)

  return (
    <div className="mm-modal" onClick={onClose}>
      <div className="mm-modal__panel" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        <button className="mm-modal__close" onClick={onClose} aria-label="Close">
          ×
        </button>

        <div className="mm-modal__head">
          <div className="mm-modal__cover" style={{ background: coverGradient(item.title || String(item.id)) }}>
            <span>{initial}</span>
          </div>
          <div className="mm-modal__headinfo">
            <h2 className="mm-modal__title">{item.title}</h2>
            <div className="mm-modal__artist">{item.artist || 'Unknown artist'}</div>
            <div className="mm-modal__stat">
              {typeof item.score === 'number' && item.score > 0 && <span>♥ Popularity {Math.round(item.score)}</span>}
              {tag && <span className="mm-card__tag">{tag}</span>}
            </div>
          </div>
        </div>

        <div className="mm-modal__actions">
          <button className="mm-btn mm-btn--primary" onClick={() => nav(`/music/${item.id}`)}>
            ▶ Play · Full Details
          </button>
          <button
            className={`mm-btn ${liked ? 'mm-btn--liked' : 'mm-btn--ghost'}`}
            onClick={onToggleLike}
            disabled={busy}
            title={isLoggedIn ? '' : 'Log in to like'}
          >
            {liked ? '♥ Liked (remove)' : '♡ Like'}
          </button>
        </div>
        {!isLoggedIn && <div className="mm-modal__hint">Guests can preview on the detail page. Log in to accumulate listening time and unlock rewards.</div>}
      </div>
    </div>
  )
}
