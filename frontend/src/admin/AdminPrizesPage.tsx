import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { prizeApi } from '@/api/prize'
import type { Prize } from '@/api/types'
import { toast } from '@/ui/toast'
import AdminShell from './AdminShell'
import ConfirmDialog from '@/components/ConfirmDialog'

/** Prize management for one PrizeCenter. Prize kind is distinguished by code. */
export default function AdminPrizesPage() {
  const [prizes, setPrizes] = useState<Prize[]>([])
  const [loading, setLoading] = useState(true)
  const [edits, setEdits] = useState<Record<number, number>>({}) // id -> pending storage value
  const [pendingDel, setPendingDel] = useState<Prize | null>(null)

  // Add form
  const [code, setCode] = useState('')
  const [name, setName] = useState('')
  const [desc, setDesc] = useState('')
  const [storage, setStorage] = useState(100)
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setEdits({})
    try {
      const r = await prizeApi.all()
      setPrizes(r.prizeVOList ?? [])
    } catch {
      setPrizes([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const onAdd = async (e: FormEvent) => {
    e.preventDefault()
    if (!code.trim() || !name.trim()) return void toast.error('Code and name are required.')
    setBusy(true)
    try {
      const r = await prizeApi.add({ code: code.trim(), name: name.trim(), description: desc.trim(), storage })
      if (r.success) {
        toast.success(`Prize "${name.trim()}" added`)
        setCode('')
        setName('')
        setDesc('')
        setStorage(100)
        load()
      }
    } finally {
      setBusy(false)
    }
  }

  const onSaveStorage = async (p: Prize) => {
    const next = edits[p.id]
    if (next == null || next === p.storage) return
    if (next < 0) return void toast.error('Storage cannot be negative.')
    const r = await prizeApi.update(p.id, next, p.name, p.description)
    if (r.success) {
      toast.success(`"${p.name}" storage changed to ${next}`)
      load()
    }
  }

  const onDelete = async () => {
    if (!pendingDel) return
    const r = await prizeApi.remove(pendingDel.id)
    if (r.success) toast.success(`Prize "${pendingDel.name}" deleted`)
    setPendingDel(null)
    load()
  }

  return (
    <AdminShell>
      <h2 className="ad-title">🎁 Prize Management</h2>

      <form className="ad-card ad-addform" onSubmit={onAdd}>
        <div className="ad-addform__title">Add Prize</div>
        <div className="ad-row">
          <input className="ad-input" placeholder="code (for example: coin)" value={code} onChange={(e) => setCode(e.target.value)} />
          <input className="ad-input" placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} />
          <input className="ad-input" placeholder="Description" value={desc} onChange={(e) => setDesc(e.target.value)} />
          <input
            className="ad-input ad-input--num"
            type="number"
            min={0}
            placeholder="Storage"
            value={storage}
            onChange={(e) => setStorage(Number(e.target.value))}
          />
          <button className="ad-btn ad-btn--primary" type="submit" disabled={busy}>
            {busy ? '...' : '+ Add'}
          </button>
        </div>
      </form>

      <div className="ad-card">
        {loading ? (
          <div className="ad-empty">Loading...</div>
        ) : prizes.length === 0 ? (
          <div className="ad-empty">No prizes yet</div>
        ) : (
          <table className="ad-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>code</th>
                <th>Name</th>
                <th>Description</th>
                <th>Storage</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {prizes.map((p) => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  <td>
                    <span className="ad-pill">{p.code}</span>
                  </td>
                  <td>{p.name}</td>
                  <td className="ad-td-muted">{p.description}</td>
                  <td>
                    <span className="ad-storage">
                      <input
                        className="ad-input ad-input--num"
                        type="number"
                        min={0}
                        value={edits[p.id] ?? p.storage}
                        onChange={(e) => setEdits((prev) => ({ ...prev, [p.id]: Number(e.target.value) }))}
                      />
                      <button
                        className="ad-btn"
                        disabled={edits[p.id] == null || edits[p.id] === p.storage}
                        onClick={() => onSaveStorage(p)}
                      >
                        Save
                      </button>
                    </span>
                  </td>
                  <td>
                    <button className="ad-btn ad-btn--danger" onClick={() => setPendingDel(p)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {pendingDel && (
        <ConfirmDialog
          title={`Delete prize "${pendingDel.name}"?`}
          text="This will remove the prize from PrizeCenter. Existing issued reward records will not be affected."
          confirmText="Delete"
          danger
          onConfirm={onDelete}
          onCancel={() => setPendingDel(null)}
        />
      )}
    </AdminShell>
  )
}
