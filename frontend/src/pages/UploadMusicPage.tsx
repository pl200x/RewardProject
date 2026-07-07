import { useRef, useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { musicApi } from '@/api/music'
import { useAuth } from '@/auth/AuthContext'
import { toast } from '@/ui/toast'
import { GENRE_PRESETS } from '@/ui/genres'
import TagChip from '@/components/TagChip'

const MAX_MB = 20

/**
 * Upload music from the account menu. Signed-in users only.
 * Requires an mp3 file, a globally unique title, and an artist. Genres,
 * release year, and lyrics are optional.
 * Songs without genres are excluded from interest-based recommendations.
 * After success, go straight to the new song detail page.
 */
export default function UploadMusicPage() {
  const nav = useNavigate()
  const { auth } = useAuth()

  const fileRef = useRef<HTMLInputElement>(null)
  const [file, setFile] = useState<File | null>(null)
  const [title, setTitle] = useState('')
  const [artist, setArtist] = useState('')
  const [year, setYear] = useState(() => new Date().getFullYear())
  const [tags, setTags] = useState<string[]>([])
  const [lyrics, setLyrics] = useState('')
  const [busy, setBusy] = useState(false)
  const [progress, setProgress] = useState(0)

  if (!auth) return <Navigate to="/login" replace />

  const pickFile = (f: File | null) => {
    setFile(f)
    // Pre-fill the title from the filename unless the user already typed one.
    if (f && !title.trim()) setTitle(f.name.replace(/\.mp3$/i, '').trim())
  }

  const toggleTag = (t: string) =>
    setTags((prev) => (prev.includes(t) ? prev.filter((x) => x !== t) : [...prev, t]))

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    const t = title.trim()
    const a = artist.trim()
    if (!file) return void toast.error('Please choose an .mp3 audio file.')
    if (!/\.mp3$/i.test(file.name) && !file.type.includes('mpeg')) return void toast.error('Only .mp3 audio is supported.')
    if (file.size > MAX_MB * 1024 * 1024) return void toast.error(`File is too large. Limit: ${MAX_MB}MB.`)
    if (!t || !a) return void toast.error('Title and artist are required.')
    if (t.includes('/')) return void toast.error("Title cannot contain '/'.")

    const fd = new FormData()
    fd.append('title', t)
    fd.append('artist', a)
    fd.append('releaseYear', String(year || new Date().getFullYear()))
    fd.append('tags', tags.join(','))
    fd.append('lyrics', lyrics)
    fd.append('file', file)

    setBusy(true)
    setProgress(0)
    try {
      const resp = await musicApi.upload(fd, setProgress)
      if (!resp.baseVO?.success || resp.musicId == null) {
        setBusy(false) // Duplicate/format errors are shown by the interceptor.
        return
      }
      toast.success(`"${t}" uploaded successfully!`)
      nav(`/music/${resp.musicId}`, { replace: true })
    } catch {
      setBusy(false) // Network errors are handled by the interceptor.
    }
  }

  return (
    <div className="mm-interest">
      <form className="mm-interest__card" onSubmit={onSubmit}>
        <h1 className="mm-interest__title">Upload Music</h1>
        <p className="mm-interest__hint">Share your music with the world. Your mp3 will be playable right after upload.</p>

        {/* File picker */}
        <input
          ref={fileRef}
          type="file"
          accept=".mp3,audio/mpeg"
          hidden
          onChange={(e) => pickFile(e.target.files?.[0] ?? null)}
        />
        <button type="button" className="mm-upload__file" onClick={() => fileRef.current?.click()}>
          {file ? (
            <>
              <span className="mm-upload__file-icon">🎵</span>
              <span className="mm-upload__file-name">{file.name}</span>
              <span className="mm-upload__file-size">{(file.size / 1024 / 1024).toFixed(1)}MB</span>
            </>
          ) : (
            <>
              <span className="mm-upload__file-icon">⬆️</span>
              <span>Choose an .mp3 file (up to {MAX_MB}MB)</span>
            </>
          )}
        </button>

        <div className="mm-upload__row">
          <label className="mm-field" style={{ flex: 2 }}>
            <span className="mm-field__label">Song Title (globally unique)</span>
            <input className="mm-input" value={title} onChange={(e) => setTitle(e.target.value)} />
          </label>
          <label className="mm-field" style={{ flex: 1 }}>
            <span className="mm-field__label">Release Year</span>
            <input
              className="mm-input"
              type="number"
              min={1800}
              max={2100}
              value={year}
              onChange={(e) => setYear(Number(e.target.value))}
            />
          </label>
        </div>
        <label className="mm-field">
          <span className="mm-field__label">Artist</span>
          <input className="mm-input" value={artist} onChange={(e) => setArtist(e.target.value)} />
        </label>

        <div className="mm-field">
          <span className="mm-field__label">Genre Tags (optional, multiple allowed)</span>
          <div className="mm-interest__chips">
            {GENRE_PRESETS.map((g) => (
              <TagChip key={g} label={g} selected={tags.includes(g)} onClick={() => toggleTag(g)} />
            ))}
          </div>
          {tags.length === 0 && (
            <div className="mm-upload__warn">Without genres, this song will not appear in interest-based recommendations or new-release emails.</div>
          )}
        </div>

        <label className="mm-field">
          <span className="mm-field__label">Lyrics (optional)</span>
          <textarea
            className="mm-input mm-upload__lyrics"
            rows={4}
            value={lyrics}
            onChange={(e) => setLyrics(e.target.value)}
            placeholder="Shown only on the song detail page"
          />
        </label>

        {busy && (
          <div className="mm-progress">
            <div className="mm-progress__bar">
              <div className="mm-progress__fill" style={{ width: `${progress}%` }} />
            </div>
            <div className="mm-progress__label">Uploading... {progress}%</div>
          </div>
        )}

        <div className="mm-interest__actions">
          <button type="button" className="mm-btn mm-btn--ghost" onClick={() => nav('/home')} disabled={busy}>
            Cancel
          </button>
          <button className="mm-btn mm-btn--primary" type="submit" disabled={busy}>
            {busy ? 'Uploading...' : '⬆️ Upload'}
          </button>
        </div>
      </form>
    </div>
  )
}
