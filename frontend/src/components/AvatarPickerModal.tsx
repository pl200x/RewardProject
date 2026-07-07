import { useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { AVATAR_COUNT, avatarUrl } from '@/ui/avatar'

interface Props {
  /** Current avatar index used for highlighting. */
  current: number
  busy?: boolean
  onSelect: (avatar: number) => void
  onCancel: () => void
}

/** Default avatar picker. Portaled to body like ConfirmDialog. */
export default function AvatarPickerModal({ current, busy, onSelect, onCancel }: Props) {
  const [picked, setPicked] = useState(current)

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onCancel()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onCancel])

  return createPortal(
    <div className="mm-confirm" onClick={onCancel}>
      <div className="mm-confirm__panel" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        <div className="mm-confirm__title">Change Avatar</div>
        <div className="mm-confirm__text">Choose a default avatar. Other users will see it on likes and comments.</div>
        <div className="mm-avatar-grid">
          {Array.from({ length: AVATAR_COUNT }, (_, i) => (
            <button
              key={i}
              type="button"
              className={`mm-avatar-grid__item ${picked === i ? 'mm-avatar-grid__item--active' : ''}`}
              onClick={() => setPicked(i)}
              aria-label={`Avatar ${i}`}
            >
              <img src={avatarUrl(i)} alt={`Avatar ${i}`} />
            </button>
          ))}
        </div>
        <div className="mm-confirm__actions">
          <button className="mm-btn mm-btn--ghost" onClick={onCancel}>
            Cancel
          </button>
          <button className="mm-btn mm-btn--primary" disabled={busy || picked === current} onClick={() => onSelect(picked)}>
            {busy ? 'Saving...' : 'Save'}
          </button>
        </div>
      </div>
    </div>,
    document.body
  )
}
