import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

/** Start page with a welcome message and login/guest entry points. */
export default function StartPage() {
  const nav = useNavigate()
  const [playing, setPlaying] = useState(true)
  return (
    <div className="mm-start">
      <button
        type="button"
        className={`mm-start__turntable${playing ? ' is-playing' : ''}`}
        aria-label={playing ? 'Pause record animation' : 'Play record animation'}
        aria-pressed={playing}
        onClick={() => setPlaying((current) => !current)}
      >
        <span className="mm-start__record" aria-hidden="true">
          <span className="mm-start__record-label">
            M
            <span className="mm-start__spindle" />
          </span>
        </span>
        <span className="mm-start__tonearm" aria-hidden="true" />
        <span className="mm-start__play-state">
          <span className="mm-start__play-dot" aria-hidden="true" />
          {playing ? 'Playing' : 'Paused'}
        </span>
      </button>
      <div className="mm-start__card">
        <p className="mm-start__eyebrow">Listen · Discover · Earn</p>
        <div className="mm-brand mm-brand--lg">
          <span className="mm-brand__logo" aria-hidden="true">M</span>
          <span className="mm-brand__name">Music Map</span>
        </div>
        <h1 className="mm-start__title">Your next favorite track should give something back.</h1>
        <p className="mm-start__sub">
          Explore new releases, share your sound, and unlock rewards as your listening time grows.
        </p>
        <div className="mm-start__actions">
          <button className="mm-btn mm-btn--primary mm-btn--lg" onClick={() => nav('/login')}>
            Log In
          </button>
          <button className="mm-btn mm-btn--ghost mm-btn--lg" onClick={() => nav('/home')}>
            Continue as Guest
          </button>
        </div>
      </div>
    </div>
  )
}
