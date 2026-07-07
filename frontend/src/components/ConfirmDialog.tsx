import { useEffect } from 'react'
import { createPortal } from 'react-dom'

interface Props {
  title: string
  text?: string
  confirmText?: string
  cancelText?: string
  /** Dangerous actions such as deletes use a red confirm button. */
  danger?: boolean
  onConfirm: () => void
  onCancel: () => void
}

/** Shared confirmation dialog. Esc cancels. */
export default function ConfirmDialog({
  title,
  text,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  danger,
  onConfirm,
  onCancel,
}: Props) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onCancel()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onCancel])

  // Portal to body so fixed positioning is not affected by backdrop-filter ancestors.
  return createPortal(
    <div className="mm-confirm" onClick={onCancel}>
      <div className="mm-confirm__panel" onClick={(e) => e.stopPropagation()} role="alertdialog" aria-modal="true">
        <div className="mm-confirm__title">{title}</div>
        {text && <div className="mm-confirm__text">{text}</div>}
        <div className="mm-confirm__actions">
          <button className="mm-btn mm-btn--ghost" onClick={onCancel}>
            {cancelText}
          </button>
          <button className={`mm-btn ${danger ? 'mm-btn--danger' : 'mm-btn--primary'}`} onClick={onConfirm}>
            {confirmText}
          </button>
        </div>
      </div>
    </div>,
    document.body
  )
}
