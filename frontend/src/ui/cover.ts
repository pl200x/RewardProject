// Generate a stable gradient from a string for placeholder album art.
export function coverGradient(seed: string): string {
  let h = 0
  for (let i = 0; i < seed.length; i++) h = (h * 31 + seed.charCodeAt(i)) % 360
  return `linear-gradient(135deg, hsl(${h},70%,60%), hsl(${(h + 48) % 360},72%,46%))`
}

/** Tags are comma-separated; the first one is used as the primary genre label. */
export function firstTag(tags?: string | null): string | null {
  if (!tags) return null
  const t = tags.split(',')[0]?.trim()
  return t || null
}
