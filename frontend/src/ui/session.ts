// Global session-expired signal. Axios interceptors trigger it imperatively,
// and SessionExpiredOverlay subscribes to show the centered prompt.

let expired = false
let listeners: Array<(v: boolean) => void> = []

function emit() {
  for (const l of listeners) l(expired)
}

/** Called when the session expires through userToken TTL or JWT/401. Idempotent. */
export function expireSession() {
  if (!expired) {
    expired = true
    emit()
  }
}

/** Clear the expired marker after the user returns to the welcome page. */
export function clearSessionExpired() {
  if (expired) {
    expired = false
    emit()
  }
}

export function subscribeSession(l: (v: boolean) => void) {
  listeners.push(l)
  return () => {
    listeners = listeners.filter((x) => x !== l)
  }
}

export function isSessionExpired() {
  return expired
}
