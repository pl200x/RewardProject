import { useNavigate } from 'react-router-dom'

/** Start page with a welcome message and login/guest entry points. */
export default function StartPage() {
  const nav = useNavigate()
  return (
    <div className="mm-start">
      <div className="mm-start__glow" />
      <div className="mm-start__card">
        <div className="mm-brand mm-brand--lg">
          <span className="mm-brand__logo">🎵</span>
          <span className="mm-brand__name">Music Map</span>
        </div>
        <h1 className="mm-start__title">
          Discover new sounds, <br />
          share your tracks, <br />
          and connect with the world!
        </h1>
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
