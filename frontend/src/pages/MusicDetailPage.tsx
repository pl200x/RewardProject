import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { musicApi } from '@/api/music'
import { rewardApi } from '@/api/reward'
import { useAuth } from '@/auth/AuthContext'
import type { CommentVO, MusicDetail, RewardRecord, UserDigestVO } from '@/api/types'
import { coverGradient } from '@/ui/cover'
import { rewardProgress } from '@/ui/reward'
import { nowDateTime, todayDate } from '@/ui/datetime'
import { toast } from '@/ui/toast'
import { avatarUrl } from '@/ui/avatar'
import AudioPlayer from '@/components/AudioPlayer'
import PrizeUnlockedModal from '@/components/PrizeUnlockedModal'

/**
 * Full song detail page (/music/:id). Fetches MusicPageVO on demand.
 * Real playback lives here; every 30 seconds of actual playback reports one
 * reward-chain event. Also shows today's progress and unlock celebration.
 */
export default function MusicDetailPage() {
  const { id } = useParams()
  const nav = useNavigate()
  const { auth, isLoggedIn } = useAuth()
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading')
  const [music, setMusic] = useState<MusicDetail | null>(null)

  // Playback -> rewards
  const [todayDuration, setTodayDuration] = useState(0)
  const [todayPrizeKeys, setTodayPrizeKeys] = useState<Set<string>>(new Set())
  const [unlocked, setUnlocked] = useState<RewardRecord | null>(null)
  const prog = rewardProgress(todayDuration)

  // Comments are publicly readable; posting requires login.
  const [comments, setComments] = useState<CommentVO[]>([])
  const [commentTotal, setCommentTotal] = useState(0)
  const [commentText, setCommentText] = useState('')
  const [commentBusy, setCommentBusy] = useState(false)

  // Like avatar wall is public; liking/unliking requires login.
  const [likedUsers, setLikedUsers] = useState<UserDigestVO[]>([])
  const [likeBusy, setLikeBusy] = useState(false)
  const iLiked = !!auth && likedUsers.some((u) => u.userId === auth.userId)

  useEffect(() => {
    const mid = Number(id)
    if (!Number.isInteger(mid) || mid <= 0) {
      setState('error')
      return
    }
    let alive = true
    setState('loading')
    musicApi
      .musicById(mid)
      .then((r) => {
        if (!alive) return
        if (r.baseVO?.success && r.musicDetailVO) {
          setMusic(r.musicDetailVO)
          setState('ready')
        } else {
          setState('error')
        }
      })
      .catch(() => alive && setState('error'))
    return () => {
      alive = false
    }
  }, [id])

  const loadComments = useCallback(async () => {
    const mid = Number(id)
    if (!Number.isInteger(mid) || mid <= 0) return
    const r = await musicApi.commentsByMusic(mid).catch(() => null)
    if (r?.baseVO?.success) {
      setComments(r.comments ?? [])
      setCommentTotal(r.totalComment ?? 0)
    }
  }, [id])

  useEffect(() => {
    loadComments()
  }, [loadComments])

  const loadLikes = useCallback(async () => {
    const mid = Number(id)
    if (!Number.isInteger(mid) || mid <= 0) return
    const r = await musicApi.likesByMusic(mid).catch(() => null)
    if (r?.baseVO?.success) setLikedUsers(r.users ?? [])
  }, [id])

  useEffect(() => {
    loadLikes()
  }, [loadLikes])

  // Silently refresh score/ranking after likes or first comments.
  const refreshDetail = (musicId: number) => {
    musicApi
      .musicById(musicId)
      .then((r) => {
        if (r.baseVO?.success && r.musicDetailVO) setMusic(r.musicDetailVO)
      })
      .catch(() => {})
  }

  // Like/unlike, then reload the avatar wall.
  const toggleLike = async () => {
    if (!auth || !music) return
    setLikeBusy(true)
    try {
      const res = iLiked ? await musicApi.unlike(auth.userId, music.id) : await musicApi.like(auth.userId, music.id)
      if (res.success) {
        toast.success(iLiked ? 'Like removed' : 'Liked ♥')
        loadLikes()
        refreshDetail(music.id)
      }
      // The interceptor shows the failure message.
    } catch {
      /* Network errors are handled by the interceptor. */
    } finally {
      setLikeBusy(false)
    }
  }

  // Post a comment, refresh the list, and quietly refresh score/ranking.
  const submitComment = async (e: FormEvent) => {
    e.preventDefault()
    if (!auth || !music) return
    const text = commentText.trim()
    if (!text) return
    setCommentBusy(true)
    try {
      const res = await musicApi.commentAdd(auth.userId, music.id, text)
      if (res.success) {
        toast.success('Comment posted!')
        setCommentText('')
        loadComments()
        refreshDetail(music.id)
      }
      // The interceptor shows auth/content validation failures.
    } catch {
      /* Network errors are handled by the interceptor. */
    } finally {
      setCommentBusy(false)
    }
  }

  // Today's accumulated duration and awarded outbizno set.
  const loadRewardState = useCallback(async () => {
    if (!auth) {
      setTodayDuration(0)
      setTodayPrizeKeys(new Set())
      return
    }
    const today = todayDate()
    const [ana, prizes] = await Promise.all([
      rewardApi.analysis(auth.userId, today, true).catch(() => null),
      rewardApi.prizeByUserDate(auth.userId, today, true).catch(() => null),
    ])
    setTodayDuration(ana?.userAnalysisVO?.totalDuration ?? 0)
    setTodayPrizeKeys(new Set((prizes?.prizeRecordVOList ?? []).map((r) => r.outBizNo)))
  }, [auth])

  useEffect(() => {
    loadRewardState()
  }, [loadRewardState])

  // Report every 30 seconds of real playback. Guests only preview audio.
  const reportPlayChunk = async (musicId: number) => {
    if (!auth) return
    const before = todayPrizeKeys
    try {
      const res = await musicApi.play({
        userId: auth.userId,
        musicId,
        syncTime: nowDateTime(),
        duration: 30,
        scene: 'Listening_Music',
        code: 'coin',
      })
      if (!res.success) return // The interceptor shows failures and session expiry.
      const today = todayDate()
      const [ana, prizes] = await Promise.all([
        rewardApi.analysis(auth.userId, today, true).catch(() => null),
        rewardApi.prizeByUserDate(auth.userId, today, true).catch(() => null),
      ])
      setTodayDuration((prev) => ana?.userAnalysisVO?.totalDuration ?? prev + 30)
      const list = prizes?.prizeRecordVOList ?? []
      setTodayPrizeKeys(new Set(list.map((r) => r.outBizNo)))
      const fresh = list.filter((r) => !before.has(r.outBizNo))
      if (fresh.length) {
        // One report can cross at most one tier; show the highest stage.
        const rec = fresh.reduce((a, b) => (b.prizeStage > a.prizeStage ? b : a))
        setUnlocked(rec)
      } else {
        toast.success('🎵 Listening time +30s')
      }
    } catch {
      /* Network errors are handled by the interceptor. */
    }
  }

  const tags = (music?.tags || '')
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean)
  const initial = (music?.title || '?').trim().charAt(0).toUpperCase() || '?'

  return (
    <div className="mm-detail">
      <header className="mm-home__bar">
        <button className="mm-brand mm-brand--btn" onClick={() => nav('/home')} title="Back to home">
          <span className="mm-brand__logo">🎵</span>
          <span className="mm-brand__name">Music Map</span>
        </button>
        <button className="mm-btn mm-btn--ghost" onClick={() => nav(-1)}>
          ← Back
        </button>
      </header>

      <main className="mm-detail__main">
        {state === 'loading' && (
          <div className="mm-detail__card">
            <div className="mm-skel" style={{ height: 120 }} />
            <div className="mm-skel" style={{ height: 62, marginTop: 12 }} />
            <div className="mm-skel" style={{ height: 62, marginTop: 12 }} />
          </div>
        )}

        {state === 'error' && (
          <div className="mm-detail__card mm-detail__card--center">
            <div style={{ fontSize: 40 }}>🫥</div>
            <h2>Song Not Found</h2>
            <p className="mm-detail__muted">It may have been removed, or the link may be wrong.</p>
            <button className="mm-btn mm-btn--primary" onClick={() => nav('/home')}>
              Back to Home
            </button>
          </div>
        )}

        {state === 'ready' && music && (
          <div className="mm-detail__card">
            <div className="mm-detail__head">
              <div
                className="mm-detail__cover"
                style={{ background: coverGradient(music.title || String(music.id)) }}
              >
                <span>{initial}</span>
              </div>
              <div className="mm-detail__headinfo">
                <h1 className="mm-detail__title">{music.title}</h1>
                <div className="mm-detail__artist">{music.artist || 'Unknown artist'}</div>
                <div className="mm-detail__stats">
                  {typeof music.score === 'number' && music.score > 0 && <span>♥ Popularity {Math.round(music.score)}</span>}
                  {music.ranking != null && music.ranking >= 0 && <span>🏆 Rank #{music.ranking + 1}</span>}
                </div>
              </div>
            </div>

            {/* Playback entry: real audio plus 30-second reward reports. */}
            <AudioPlayer musicId={music.id} onPlayedChunk={reportPlayChunk} />

            {isLoggedIn ? (
              <div className="mm-progress">
                <div className="mm-progress__bar">
                  <div className="mm-progress__fill" style={{ width: `${prog.pct}%` }} />
                </div>
                <div className="mm-progress__label">
                  Today: {todayDuration}s ·{' '}
                  {prog.maxed ? 'Highest tier reached 🏆' : `${prog.remaining}s until next reward`}
                </div>
              </div>
            ) : (
              <div className="mm-modal__hint">Guests can preview. Log in to accumulate listening time and unlock rewards.</div>
            )}

            <dl className="mm-modal__meta mm-detail__meta">
              <div>
                <dt>Release Year</dt>
                <dd>{music.releaseYear && music.releaseYear > 0 ? music.releaseYear : '—'}</dd>
              </div>
              <div>
                <dt>Genre Tags</dt>
                <dd>
                  {tags.length ? (
                    <span className="mm-modal__tags">
                      {tags.map((t) => (
                        <span key={t} className="mm-card__tag">
                          {t}
                        </span>
                      ))}
                    </span>
                  ) : (
                    '—'
                  )}
                </dd>
              </div>
              <div>
                <dt>Status</dt>
                <dd>{music.status || '—'}</dd>
              </div>
            </dl>

            <div className="mm-detail__lyrics">
              <div className="mm-modal__lyrics-label">Lyrics</div>
              {music.lyrics && music.lyrics.trim() ? (
                <p>{music.lyrics}</p>
              ) : (
                <p className="mm-detail__muted">No lyrics yet</p>
              )}
            </div>

            <div className="mm-likes">
              <div className="mm-modal__lyrics-label">
                People who like this song{likedUsers.length > 0 ? ` (${likedUsers.length})` : ''}
              </div>
              <div className="mm-likes__row">
                {isLoggedIn && (
                  <button
                    className={`mm-btn ${iLiked ? 'mm-btn--liked' : 'mm-btn--ghost'}`}
                    disabled={likeBusy}
                    onClick={toggleLike}
                  >
                    {iLiked ? '♥ Liked (remove)' : '♡ Like'}
                  </button>
                )}
                {likedUsers.length ? (
                  <div className="mm-likes__wall">
                    {likedUsers.map((u) => (
                      <img
                        key={u.userId}
                        className="mm-likes__face"
                        src={avatarUrl(u.avatar)}
                        alt={u.userName ?? `User ${u.userId}`}
                        title={u.userName ?? `User ${u.userId}`}
                      />
                    ))}
                  </div>
                ) : (
                  <span className="mm-detail__muted">No likes yet{isLoggedIn ? '. Be the first.' : ''}</span>
                )}
              </div>
              {!isLoggedIn && <div className="mm-detail__muted mm-comments__hint">Log in to like this song</div>}
            </div>

            <div className="mm-comments">
              <div className="mm-modal__lyrics-label">Comments{commentTotal > 0 ? ` (${commentTotal})` : ''}</div>
              {isLoggedIn ? (
                <form className="mm-comments__form" onSubmit={submitComment}>
                  <textarea
                    className="mm-comments__input"
                    value={commentText}
                    maxLength={300}
                    rows={2}
                    placeholder="Say something... (up to 300 characters)"
                    onChange={(e) => setCommentText(e.target.value)}
                  />
                  <button
                    type="submit"
                    className="mm-btn mm-btn--primary"
                    disabled={commentBusy || !commentText.trim()}
                  >
                    {commentBusy ? 'Posting...' : 'Post Comment'}
                  </button>
                </form>
              ) : (
                <div className="mm-detail__muted mm-comments__hint">Log in to post a comment</div>
              )}
              {comments.length ? (
                <ul className="mm-comments__list">
                  {comments.map((c, i) => (
                    <li key={`${c.userId}-${c.time}-${i}`} className="mm-comments__item">
                      <div className="mm-comments__meta">
                        <img className="mm-comments__face" src={avatarUrl(c.avatar)} alt="" />
                        <span className="mm-comments__user">{c.userName || `User ${c.userId}`}</span>
                        <span>{new Date(c.time).toLocaleString()}</span>
                      </div>
                      <p className="mm-comments__content">{c.content}</p>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="mm-detail__muted">No comments yet. Be the first.</p>
              )}
            </div>
          </div>
        )}
      </main>

      {unlocked && <PrizeUnlockedModal record={unlocked} onClose={() => setUnlocked(null)} />}
    </div>
  )
}
