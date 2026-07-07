import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { rewardApi } from '@/api/reward'
import type { Configuration } from '@/api/types'
import { toast } from '@/ui/toast'
import AdminShell from './AdminShell'
import ConfirmDialog from '@/components/ConfirmDialog'

/** Reward rule management for JEXL rules stored in the database. */
export default function AdminRulesPage() {
  const [rules, setRules] = useState<Configuration[]>([])
  const [loading, setLoading] = useState(true)
  // code -> in-progress rule/description edits
  const [edits, setEdits] = useState<Record<string, { rule: string; description: string }>>({})
  const [pendingDel, setPendingDel] = useState<Configuration | null>(null)

  // Add form
  const [nCode, setNCode] = useState('')
  const [nRule, setNRule] = useState('')
  const [nDesc, setNDesc] = useState('')
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setEdits({})
    try {
      const r = await rewardApi.allConfig()
      setRules(r.configurationVoList ?? [])
    } catch {
      setRules([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const editOf = (c: Configuration) => edits[c.code] ?? { rule: c.rule ?? '', description: c.description ?? '' }
  const dirty = (c: Configuration) => {
    const e = edits[c.code]
    return !!e && (e.rule !== (c.rule ?? '') || e.description !== (c.description ?? ''))
  }

  const onSave = async (c: Configuration) => {
    const e = editOf(c)
    if (!e.rule.trim()) return void toast.error('Rule expression cannot be empty.')
    const r = await rewardApi.updateConfig(c.code, e.rule, e.description)
    if (r.success) {
      toast.success(`Rule ${c.code} updated and active`)
      load()
    }
  }

  const onAdd = async (e: FormEvent) => {
    e.preventDefault()
    if (!nCode.trim() || !nRule.trim()) return void toast.error('Code and rule expression are required.')
    setBusy(true)
    try {
      const r = await rewardApi.addConfig({ code: nCode.trim(), rule: nRule, description: nDesc })
      if (r.success) {
        toast.success(`Rule ${nCode.trim()} added`)
        setNCode('')
        setNRule('')
        setNDesc('')
        load()
      }
    } finally {
      setBusy(false)
    }
  }

  const onDelete = async () => {
    if (!pendingDel) return
    const r = await rewardApi.deleteConfig(pendingDel.code)
    if (r.success) toast.success(`Rule ${pendingDel.code} deleted`)
    setPendingDel(null)
    load()
  }

  return (
    <AdminShell>
      <h2 className="ad-title">📜 Reward Rule Management</h2>
      <p className="ad-note">
        STAGE_RULE decides which tier unlocks after a listening duration, and AMOUNT_RULE decides how many rewards that tier grants. Changes apply to the next playback report without restart.
      </p>

      <form className="ad-card ad-addform" onSubmit={onAdd}>
        <div className="ad-addform__title">Add Rule</div>
        <div className="ad-row">
          <input className="ad-input" placeholder="code (for example: STAGE_RULE)" value={nCode} onChange={(e) => setNCode(e.target.value)} />
          <input className="ad-input ad-input--wide" placeholder="JEXL expression" value={nRule} onChange={(e) => setNRule(e.target.value)} />
          <input className="ad-input" placeholder="Description" value={nDesc} onChange={(e) => setNDesc(e.target.value)} />
          <button className="ad-btn ad-btn--primary" type="submit" disabled={busy}>
            {busy ? '...' : '+ Add'}
          </button>
        </div>
      </form>

      {loading ? (
        <div className="ad-card ad-empty">Loading...</div>
      ) : (
        rules.map((c) => (
          <div key={c.code} className="ad-card ad-rule">
            <div className="ad-rule__head">
              <span className="ad-pill">{c.code}</span>
              <input
                className="ad-input ad-rule__desc"
                value={editOf(c).description}
                placeholder="Description"
                onChange={(e) => setEdits((prev) => ({ ...prev, [c.code]: { ...editOf(c), description: e.target.value } }))}
              />
              <button className="ad-btn ad-btn--primary" disabled={!dirty(c)} onClick={() => onSave(c)}>
                Save
              </button>
              <button className="ad-btn ad-btn--danger" onClick={() => setPendingDel(c)}>
                Delete
              </button>
            </div>
            <textarea
              className="ad-input ad-rule__rule"
              rows={3}
              value={editOf(c).rule}
              onChange={(e) => setEdits((prev) => ({ ...prev, [c.code]: { ...editOf(c), rule: e.target.value } }))}
            />
          </div>
        ))
      )}

      {pendingDel && (
        <ConfirmDialog
          title={`Delete rule ${pendingDel.code}?`}
          text="Deleting core rules such as STAGE_RULE or AMOUNT_RULE may break reward calculation. Confirm that you understand the impact."
          confirmText="Delete"
          danger
          onConfirm={onDelete}
          onCancel={() => setPendingDel(null)}
        />
      )}
    </AdminShell>
  )
}
