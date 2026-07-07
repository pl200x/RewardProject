import { useMemo, useState } from 'react'
import { Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { musicApi } from '@/api/music'
import { useAuth } from '@/auth/AuthContext'
import { toast } from '@/ui/toast'
import TagChip from '@/components/TagChip'
import { GENRE_PRESETS as PRESETS } from '@/ui/genres'

/** Convert a comma-separated string into trimmed, case-insensitive unique tags. */
function parseTags(raw: string): string[] {
  const out: string[] = []
  const seen = new Set<string>()
  for (const part of raw.split(',')) {
    const t = part.trim()
    if (!t) continue
    const key = t.toLowerCase()
    if (seen.has(key)) continue
    seen.add(key)
    out.push(t)
  }
  return out
}

/**
 * Interest selection page with two modes:
 * - onboarding: shown after first login/registration, with Skip
 * - edit: opened from the account menu, prefilled with current interests
 * Both modes save through /user/change-tag using a comma-separated string.
 */
export default function InterestSelectionPage() {
  const nav = useNavigate()
  const [params] = useSearchParams()
  const { auth, setInterest } = useAuth()
  const mode = params.get('mode') === 'edit' ? 'edit' : 'onboarding'

  const [raw, setRaw] = useState(() => (mode === 'edit' ? auth?.interest ?? '' : ''))
  const [busy, setBusy] = useState(false)

  const tags = useMemo(() => parseTags(raw), [raw])

  // Saving interests requires auth because change-tag validates the token.
  if (!auth) return <Navigate to="/login" replace />

  const isOn = (preset: string) => tags.some((t) => t.toLowerCase() === preset.toLowerCase())

  const toggle = (preset: string) => {
    const next = isOn(preset)
      ? tags.filter((t) => t.toLowerCase() !== preset.toLowerCase())
      : [...tags, preset]
    setRaw(next.join(', '))
  }

  const onSave = async () => {
    const joined = tags.join(',')
    setBusy(true)
    try {
      const res = await musicApi.changeTag(auth.userId, joined)
      if (!res.success) {
        setBusy(false) // The interceptor shows the failure message.
        return
      }
      setInterest(joined)
      toast.success('Interests saved')
      nav('/home', { replace: true })
    } catch {
      setBusy(false)
    }
  }

  return (
    <div className="mm-interest">
      <div className="mm-interest__card">
        <h1 className="mm-interest__title">{mode === 'edit' ? 'Update Your Interests' : 'Tell Us What You Like'}</h1>
        <p className="mm-interest__hint">
          Tell us what you like — type genres separated by commas, or tap a suggestion below.
        </p>

        <input
          className="mm-input mm-interest__input"
          value={raw}
          onChange={(e) => setRaw(e.target.value)}
          placeholder="For example: Pop, Jazz, Classical"
          autoFocus
        />

        <div className="mm-interest__chips">
          {PRESETS.map((p) => (
            <TagChip key={p} label={p} selected={isOn(p)} onClick={() => toggle(p)} />
          ))}
        </div>

        <div className="mm-interest__count">{tags.length > 0 ? `${tags.length} selected` : 'Nothing selected'}</div>

        <div className="mm-interest__actions">
          {mode === 'onboarding' ? (
            <button className="mm-btn mm-btn--ghost" onClick={() => nav('/home', { replace: true })} disabled={busy}>
              Skip
            </button>
          ) : (
            <button className="mm-btn mm-btn--ghost" onClick={() => nav('/home', { replace: true })} disabled={busy}>
              Cancel
            </button>
          )}
          <button className="mm-btn mm-btn--primary" onClick={onSave} disabled={busy}>
            {busy ? 'Saving...' : 'Save'}
          </button>
        </div>
      </div>
    </div>
  )
}
