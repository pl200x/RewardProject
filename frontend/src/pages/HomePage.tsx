import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { musicApi } from '@/api/music'
import { useAuth } from '@/auth/AuthContext'
import type { MusicDigest } from '@/api/types'
import { toast } from '@/ui/toast'
import AccountMenu from '@/components/AccountMenu'
import MusicColumn from '@/components/MusicColumn'
import MusicCard from '@/components/MusicCard'
import MusicDetailModal from '@/components/MusicDetailModal'

/**
 * Home page. Layout uses only Flexbox/Grid; no page-level absolute positioning
 * except fixed overlays such as modals and dropdowns.
 * Main layout has three columns: Recent, Top 10, and Music You May Like.
 * The top search opens a quick detail modal from any card.
 */
export default function HomePage() {
  const nav = useNavigate()
  const { auth, isLoggedIn } = useAuth()
  const [top, setTop] = useState<MusicDigest[]>([])
  const [recent, setRecent] = useState<MusicDigest[]>([])
  const [mayLike, setMayLike] = useState<MusicDigest[]>([])
  const [loading, setLoading] = useState(true)

  // Per-column Top-N settings (default 10). Values beyond 10 page client-side.
  const [nRecent, setNRecent] = useState(10)
  const [nTop, setNTop] = useState(10)
  const [nMayLike, setNMayLike] = useState(10)

  // Search
  const [q, setQ] = useState('')
  const [searched, setSearched] = useState(false)
  const [searching, setSearching] = useState(false)
  const [results, setResults] = useState<MusicDigest[]>([])
  const [searchMsg, setSearchMsg] = useState<string | null>(null)

  // Quick detail modal + likes. Playback and rewards live on /music/:id.
  const [selected, setSelected] = useState<MusicDigest | null>(null)
  const [likedIds, setLikedIds] = useState<Set<number>>(new Set())
  const [likeBusy, setLikeBusy] = useState(false)

  const loadColumns = useCallback(() => {
    setLoading(true)
    // Guests do not request personalized recommendations; the right column stays locked.
    const mayLikeReq = auth?.userId ? musicApi.mayLike(auth.userId, nMayLike) : Promise.resolve(null)
    return Promise.allSettled([musicApi.top(nTop), musicApi.recent(nRecent), mayLikeReq]).then((res) => {
      const [t, r, m] = res
      if (t.status === 'fulfilled') setTop(t.value.musicDigestVOList ?? [])
      if (r.status === 'fulfilled') setRecent(r.value.musicDigestVOList ?? [])
      if (m.status === 'fulfilled') setMayLike(m.value?.musicDigestVOList ?? [])
      setLoading(false)
    })
  }, [auth?.userId, nTop, nRecent, nMayLike])

  // Load my liked songs when signed in to initialize heart state.
  const loadLiked = useCallback(() => {
    if (!auth) {
      setLikedIds(new Set())
      return
    }
    musicApi
      .likesByUser(auth.userId, true)
      .then((r) => {
        if (r?.baseVO?.success) setLikedIds(new Set(r.musicIds ?? []))
      })
      .catch(() => {})
  }, [auth])

  useEffect(() => {
    loadColumns()
  }, [loadColumns])
  useEffect(() => {
    loadLiked()
  }, [loadLiked])

  const onSearch = async (e: FormEvent) => {
    e.preventDefault()
    const name = q.trim()
    if (!name) {
      clearSearch()
      return
    }
    setSearching(true)
    try {
      const resp = await musicApi.search(name)
      setResults(resp.musicDigestVOList ?? [])
      setSearchMsg(resp.baseVO?.errorMessage ?? null)
      setSearched(true)
    } catch {
      /* Network errors are handled by the interceptor. */
    } finally {
      setSearching(false)
    }
  }

  const clearSearch = () => {
    setQ('')
    setSearched(false)
    setResults([])
    setSearchMsg(null)
  }

  const toggleLike = async () => {
    if (!selected) return
    if (!auth) {
      toast.error('Please log in before liking music.')
      return
    }
    const id = selected.id
    const wasLiked = likedIds.has(id)
    setLikeBusy(true)
    try {
      const res = wasLiked ? await musicApi.unlike(auth.userId, id) : await musicApi.like(auth.userId, id)
      if (!res.success) return // The interceptor shows the failure message.
      setLikedIds((prev) => {
        const n = new Set(prev)
        if (wasLiked) n.delete(id)
        else n.add(id)
        return n
      })
      // Optimistically update the modal score, then refresh rankings.
      setSelected((prev) => (prev ? { ...prev, score: (prev.score ?? 0) + (wasLiked ? -1 : 1) } : prev))
      toast.success(wasLiked ? 'Like removed' : 'Liked ♥')
      loadColumns()
    } catch {
      /* */
    } finally {
      setLikeBusy(false)
    }
  }

  const hasInterest = !!(auth?.interest && auth.interest.trim())
  const likeSub = hasInterest
    ? 'Recommended from your interests'
    : isLoggedIn
      ? 'Set interests to personalize recommendations'
      : 'Log in and set interests for personalized recommendations'

  return (
    <div className="mm-home">
      <header className="mm-home__bar">
        <button
          className="mm-brand mm-brand--btn"
          onClick={() => {
            clearSearch()
            setSelected(null)
            nav('/home')
          }}
          aria-label="Back to home"
          title="Back to home"
        >
          <span className="mm-brand__logo">🎵</span>
          <span className="mm-brand__name">Music Map</span>
        </button>

        <form className="mm-search" onSubmit={onSearch}>
          <input
            className="mm-search__input"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Search songs or artists..."
            aria-label="Search music"
          />
          {searched && (
            <button type="button" className="mm-search__clear" onClick={clearSearch} aria-label="Clear search">
              ×
            </button>
          )}
          <button className="mm-btn mm-btn--primary mm-search__btn" type="submit" disabled={searching}>
            {searching ? '...' : 'Search'}
          </button>
        </form>

        <AccountMenu />
      </header>

      <main className="mm-home__main">
        {searched ? (
          <section className="mm-results">
            <div className="mm-results__head">
              <h2 className="mm-col__title">Search Results{results.length ? ` (${results.length})` : ''}</h2>
              <button className="mm-btn mm-btn--ghost" onClick={clearSearch}>
                ← Back to Home
              </button>
            </div>
            {results.length === 0 ? (
              <div className="mm-col__empty">
                {searchMsg ?? 'the music you are finding are not found, we will update soon'}
              </div>
            ) : (
              <div className="mm-results__grid">
                {results.map((m) => (
                  <MusicCard key={m.id} item={m} onClick={() => setSelected(m)} />
                ))}
              </div>
            )}
          </section>
        ) : (
          <div className="mm-home__grid">
            <MusicColumn
              title="Recent Music"
              subtitle="New on the platform"
              items={recent}
              loading={loading}
              empty="No new music yet"
              onItemClick={setSelected}
              topN={nRecent}
              onTopNChange={setNRecent}
            />
            <MusicColumn
              title={`Top ${nTop}`}
              subtitle="Global popularity chart"
              items={top}
              loading={loading}
              showRank
              center
              empty="No ranking data yet. Likes will build the chart."
              onItemClick={setSelected}
              topN={nTop}
              onTopNChange={setNTop}
            />
            <MusicColumn
              title="Music You May Like"
              subtitle={likeSub}
              items={mayLike}
              loading={loading}
              showRank
              empty="No recommendations yet"
              onItemClick={setSelected}
              locked={!isLoggedIn}
              onUnlock={() => nav('/login')}
              topN={nMayLike}
              onTopNChange={setNMayLike}
            />
          </div>
        )}
      </main>

      {selected && (
        <MusicDetailModal
          item={selected}
          liked={likedIds.has(selected.id)}
          isLoggedIn={isLoggedIn}
          busy={likeBusy}
          onToggleLike={toggleLike}
          onClose={() => setSelected(null)}
        />
      )}
    </div>
  )
}
