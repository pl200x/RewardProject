import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { musicApi } from '@/api/music'
import type { MusicDigest } from '@/api/types'
import { toast } from '@/ui/toast'
import AdminShell from './AdminShell'
import ConfirmDialog from '@/components/ConfirmDialog'

/**
 * Delete music: search or browse recent music, then confirm deletion.
 * Backend deletion sets OFFLINE and removes the song from rankings and recommendations.
 */
export default function AdminMusicPage() {
  const [q, setQ] = useState('')
  const [mode, setMode] = useState<'recent' | 'search'>('recent')
  const [list, setList] = useState<MusicDigest[]>([])
  const [loading, setLoading] = useState(true)
  const [pendingDel, setPendingDel] = useState<MusicDigest | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      if (mode === 'search' && q.trim()) {
        const r = await musicApi.search(q.trim())
        setList(r.musicDigestVOList ?? [])
      } else {
        const r = await musicApi.recent(20)
        setList(r.musicDigestVOList ?? [])
      }
    } catch {
      setList([])
    } finally {
      setLoading(false)
    }
  }, [mode, q])

  useEffect(() => {
    load()
  }, [load])

  const onSearch = (e: FormEvent) => {
    e.preventDefault()
    setMode(q.trim() ? 'search' : 'recent')
  }

  const onDelete = async () => {
    if (!pendingDel) return
    const r = await musicApi.deleteMusic(pendingDel.id)
    if (r.success) toast.success(`"${pendingDel.title}" deleted and removed from rankings and recommendations`)
    setPendingDel(null)
    load()
  }

  return (
    <AdminShell>
      <h2 className="ad-title">🗑️ Delete Music</h2>
      <p className="ad-note">Delete means taking a song offline and removing it from rankings and recommendations. Playback and reward records are retained.</p>

      <form className="ad-card ad-row" onSubmit={onSearch}>
        <input
          className="ad-input ad-input--wide"
          placeholder="Search by song title or artist; leave blank to show the latest 20"
          value={q}
          onChange={(e) => setQ(e.target.value)}
        />
        <button className="ad-btn ad-btn--primary" type="submit">
          Search
        </button>
        {mode === 'search' && (
          <button
            type="button"
            className="ad-btn ad-btn--ghost"
            onClick={() => {
              setQ('')
              setMode('recent')
            }}
          >
            Clear
          </button>
        )}
      </form>

      <div className="ad-card">
        {loading ? (
          <div className="ad-empty">Loading...</div>
        ) : list.length === 0 ? (
          <div className="ad-empty">{mode === 'search' ? 'No matching music' : 'No music yet'}</div>
        ) : (
          <table className="ad-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Song Title</th>
                <th>Artist</th>
                <th>Genre</th>
                <th>Popularity</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {list.map((m) => (
                <tr key={m.id}>
                  <td>{m.id}</td>
                  <td>{m.title}</td>
                  <td className="ad-td-muted">{m.artist}</td>
                  <td>{m.tags ? <span className="ad-pill">{m.tags}</span> : '—'}</td>
                  <td className="ad-td-muted">{m.score ? `♥ ${Math.round(m.score)}` : '—'}</td>
                  <td>
                    <button className="ad-btn ad-btn--danger" onClick={() => setPendingDel(m)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {pendingDel && (
        <ConfirmDialog
          title={`Delete "${pendingDel.title}"?`}
          text="This song will go offline and disappear from Top 10, interest recommendations, recent lists, and search. This page cannot undo the action."
          confirmText="Delete"
          danger
          onConfirm={onDelete}
          onCancel={() => setPendingDel(null)}
        />
      )}
    </AdminShell>
  )
}
