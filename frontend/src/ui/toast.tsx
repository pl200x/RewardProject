import { useEffect, useState } from 'react'

// Lightweight global toast: imperative API plus one <ToastHost/>.
// Shared by Music Map and axios interceptors without depending on Ant Design.

export type ToastKind = 'success' | 'error' | 'info'
export interface ToastItem {
  id: number
  kind: ToastKind
  text: string
}

let items: ToastItem[] = []
let seq = 1
let listeners: Array<(items: ToastItem[]) => void> = []

function emit() {
  // Pass a new array reference so React re-renders.
  const snapshot = items.slice()
  listeners.forEach((l) => l(snapshot))
}

function dismiss(id: number) {
  items = items.filter((i) => i.id !== id)
  emit()
}

function push(kind: ToastKind, text: string, duration: number) {
  const id = seq++
  items = [...items, { id, kind, text }]
  emit()
  window.setTimeout(() => dismiss(id), duration)
  return id
}

export const toast = {
  success: (text: string, duration = 2600) => push('success', text, duration),
  error: (text: string, duration = 3200) => push('error', text, duration),
  info: (text: string, duration = 2600) => push('info', text, duration),
  dismiss,
}

const ICON: Record<ToastKind, string> = { success: '✓', error: '!', info: 'i' }

/** Mount once at the app root. Uses a fixed overlay and does not affect layout. */
export function ToastHost() {
  const [list, setList] = useState<ToastItem[]>(() => items.slice())
  useEffect(() => {
    listeners.push(setList)
    return () => {
      listeners = listeners.filter((l) => l !== setList)
    }
  }, [])

  return (
    <div className="mm-toast-host" role="status" aria-live="polite">
      {list.map((t) => (
        <div key={t.id} className={`mm-toast mm-toast--${t.kind}`} onClick={() => dismiss(t.id)}>
          <span className="mm-toast__icon">{ICON[t.kind]}</span>
          <span className="mm-toast__text">{t.text}</span>
        </div>
      ))}
    </div>
  )
}
