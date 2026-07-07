import { useEffect } from 'react'
import type { RewardRecord } from '@/api/types'
import { prizeLabel, prizeEmoji } from '@/ui/reward'

/** Celebration modal shown when playback crosses a reward threshold. */
export default function PrizeUnlockedModal({ record, onClose }: { record: RewardRecord; onClose: () => void }) {
  useEffect(() => {
    const t = window.setTimeout(onClose, 6000)
    return () => window.clearTimeout(t)
  }, [onClose])

  return (
    <div className="mm-unlock" onClick={onClose}>
      <div className="mm-unlock__card" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        <div className="mm-unlock__burst">🎉</div>
        <div className="mm-unlock__eyebrow">Reward Unlocked</div>
        <div className="mm-unlock__prize">
          {prizeEmoji(record.prizeCode)} {prizeLabel(record.prizeCode)} ×{record.prizeAmount}
        </div>
        <div className="mm-unlock__stage">Stage {record.prizeStage}</div>
        <button className="mm-btn mm-btn--primary mm-btn--block" onClick={onClose}>
          Awesome
        </button>
      </div>
    </div>
  )
}
