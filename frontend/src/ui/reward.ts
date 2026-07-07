// Reward thresholds and display helpers, aligned with backend STAGE_RULE.
export const REWARD_THRESHOLDS = [160, 320, 640, 960, 1280, 1600, 3200, 6400]

export function prizeLabel(code: string): string {
  switch (code) {
    case 'coin':
      return 'Coins'
    case 'gem':
      return 'Gems'
    case 'coupon':
      return 'Coupons'
    default:
      return code
  }
}

export function prizeEmoji(code: string): string {
  switch (code) {
    case 'coin':
      return '🪙'
    case 'gem':
      return '💎'
    case 'coupon':
      return '🎟️'
    default:
      return '🎁'
  }
}

/** Progress to the next tier for today's accumulated total seconds. */
export function rewardProgress(total: number) {
  const last = REWARD_THRESHOLDS[REWARD_THRESHOLDS.length - 1]
  if (total >= last) return { next: last, remaining: 0, pct: 100, maxed: true }
  const nextIdx = REWARD_THRESHOLDS.findIndex((t) => t > total)
  const next = REWARD_THRESHOLDS[nextIdx]
  const prev = nextIdx > 0 ? REWARD_THRESHOLDS[nextIdx - 1] : 0
  const pct = Math.min(100, Math.max(0, ((total - prev) / (next - prev)) * 100))
  return { next, remaining: next - total, pct, maxed: false }
}
