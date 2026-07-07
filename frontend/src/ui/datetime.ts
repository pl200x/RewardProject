// Local time formatting for play syncTime and today's date.
function pad(n: number): string {
  return String(n).padStart(2, '0')
}

/** "yyyy-MM-dd HH:mm:ss" for play syncTime. */
export function nowDateTime(): string {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(
    d.getMinutes(),
  )}:${pad(d.getSeconds())}`
}

/** "yyyy-MM-dd" for analysis/prize record queries. */
export function todayDate(): string {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
