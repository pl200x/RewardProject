import { useCallback, useEffect, useState } from 'react'
import { prizeApi } from '@/api/prize'
import type { PrizeFlowRecord } from '@/api/types'
import AdminShell from './AdminShell'
import { PRIZE_KINDS } from './prizeMeta'

/** Record filter tabs: all plus prize kinds, filtered client-side by code. */
const FILTER_TABS = [{ k: 'all', label: 'All', emoji: '📒' }, ...PRIZE_KINDS]

function fmtTime(s?: string | null): string {
  if (!s) return '—'
  const d = new Date(s)
  if (Number.isNaN(d.getTime())) return s
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/** Read-only reward records from PrizeCenter, filterable by prize kind. */
export default function AdminRecordsPage() {
  const [kind, setKind] = useState('all')
  const [records, setRecords] = useState<PrizeFlowRecord[]>([])
  const [count, setCount] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [r, c] = await Promise.all([prizeApi.records(), prizeApi.count().catch(() => null)])
      // Newest first.
      setRecords((r.prizeRecordVOList ?? []).slice().sort((a, b) => b.id - a.id))
      setCount(c?.count ?? null)
    } catch {
      setRecords([])
      setCount(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const shown = kind === 'all' ? records : records.filter((r) => r.code === kind)
  const meta = FILTER_TABS.find((m) => m.k === kind)!

  return (
    <AdminShell>
      <h2 className="ad-title">📒 Reward Records</h2>

      <div className="ad-tabs">
        {FILTER_TABS.map((m) => (
          <button key={m.k} className={`ad-tab${kind === m.k ? ' ad-tab--on' : ''}`} onClick={() => setKind(m.k)}>
            {m.emoji} {m.label}
          </button>
        ))}
        <span className="ad-tabs__count">
          {kind === 'all'
            ? count != null
              ? `Total rewards: ${count}`
              : ''
            : `${meta.label}: ${shown.length}`}
        </span>
      </div>

      <div className="ad-card">
        {loading ? (
          <div className="ad-empty">Loading...</div>
        ) : shown.length === 0 ? (
          <div className="ad-empty">No reward records yet</div>
        ) : (
          <table className="ad-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>code</th>
                <th>Amount</th>
                <th>outbizno (idempotency key: scene_user_type_date_stage)</th>
                <th>Time</th>
              </tr>
            </thead>
            <tbody>
              {shown.map((r) => (
                <tr key={r.id}>
                  <td>{r.id}</td>
                  <td>
                    <span className="ad-pill">{r.code}</span>
                  </td>
                  <td>×{r.amount}</td>
                  <td className="ad-td-mono">{r.outbizno}</td>
                  <td className="ad-td-muted">{fmtTime(r.createTime)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </AdminShell>
  )
}
