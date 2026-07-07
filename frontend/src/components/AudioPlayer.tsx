import { useEffect, useRef, useState } from 'react'
import { musicApi } from '@/api/music'

interface Props {
  musicId: number
  /** Called after every 30 seconds of real playback. Pause/end time does not count. */
  onPlayedChunk?: (musicId: number) => void
}

function fmt(sec: number): string {
  if (!Number.isFinite(sec) || sec < 0) return '0:00'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

/**
 * Real audio player using a MinIO presigned URL. Playback is available only
 * on the song detail page. Every 30 seconds of active playback triggers one report.
 */
export default function AudioPlayer({ musicId, onPlayedChunk }: Props) {
  const audioRef = useRef<HTMLAudioElement>(null)
  const playedSecRef = useRef(0)
  const chunkCbRef = useRef(onPlayedChunk)
  chunkCbRef.current = onPlayedChunk // Keep the latest callback for the interval.

  const [audioState, setAudioState] = useState<'loading' | 'ready' | 'none'>('loading')
  const [audioUrl, setAudioUrl] = useState<string | null>(null)
  const [playing, setPlaying] = useState(false)
  const [curTime, setCurTime] = useState(0)
  const [duration, setDuration] = useState(0)

  // Fetch the audio URL on mount/song change, and stop playback on unmount.
  useEffect(() => {
    let alive = true
    playedSecRef.current = 0
    setAudioState('loading')
    setAudioUrl(null)
    setPlaying(false)
    setCurTime(0)
    setDuration(0)
    musicApi
      .audioUrl(musicId)
      .then((r) => {
        if (!alive) return
        if (r.baseVO?.success && r.url) {
          setAudioUrl(r.url)
          setAudioState('ready')
        } else {
          setAudioState('none')
        }
      })
      .catch(() => alive && setAudioState('none'))
    return () => {
      alive = false
      audioRef.current?.pause()
    }
  }, [musicId])

  // Tick once per second; only active playback accumulates report time.
  useEffect(() => {
    const t = window.setInterval(() => {
      const a = audioRef.current
      if (!a) return
      setCurTime(a.currentTime || 0)
      if (!a.paused && !a.ended) {
        playedSecRef.current += 1
        if (playedSecRef.current >= 30) {
          playedSecRef.current -= 30
          chunkCbRef.current?.(musicId)
        }
      }
    }, 1000)
    return () => window.clearInterval(t)
  }, [musicId])

  const togglePlay = () => {
    const a = audioRef.current
    if (!a || audioState !== 'ready') return
    if (a.paused) a.play().catch(() => setAudioState('none'))
    else a.pause()
  }

  const seek = (v: number) => {
    const a = audioRef.current
    if (a && Number.isFinite(v)) a.currentTime = v
  }

  return (
    <>
      <audio
        ref={audioRef}
        src={audioUrl ?? undefined}
        preload="metadata"
        onPlay={() => setPlaying(true)}
        onPause={() => setPlaying(false)}
        onEnded={() => setPlaying(false)}
        onLoadedMetadata={(e) => setDuration(e.currentTarget.duration || 0)}
      />
      {audioState === 'none' ? (
        <div className="mm-player mm-player--none">
          <span>🎧 No audio source yet. Try a new song from Recent Music.</span>
        </div>
      ) : (
        <div className="mm-player">
          <button className="mm-btn mm-btn--primary mm-player__toggle" onClick={togglePlay} disabled={audioState !== 'ready'}>
            {audioState === 'loading' ? '…' : playing ? '⏸ Pause' : '▶ Play'}
          </button>
          <span className="mm-player__time">
            {fmt(curTime)} / {fmt(duration)}
          </span>
          <input
            className="mm-player__seek"
            type="range"
            min={0}
            max={Math.max(1, Math.floor(duration))}
            value={Math.floor(curTime)}
            onChange={(e) => seek(Number(e.target.value))}
            aria-label="Playback progress"
          />
        </div>
      )}
    </>
  )
}
