// Pick a stable matte color from a small, earthy palette for placeholder art.
export function coverColor(seed: string): string {
  const palette = ['#6c3b2a', '#425044', '#51483f', '#765c2f', '#5d3f45', '#3e4b47']
  let hash = 0
  for (let i = 0; i < seed.length; i++) hash = (hash * 31 + seed.charCodeAt(i)) >>> 0
  return palette[hash % palette.length]
}

/** Tags are comma-separated; the first one is used as the primary genre label. */
export function firstTag(tags?: string | null): string | null {
  if (!tags) return null
  const t = tags.split(',')[0]?.trim()
  return t || null
}
