import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/auth/AuthContext'
import { musicApi } from '@/api/music'
import { toast } from '@/ui/toast'
import { avatarUrl } from '@/ui/avatar'
import ConfirmDialog from './ConfirmDialog'
import AvatarPickerModal from './AvatarPickerModal'

/**
 * Account menu in the top-right corner.
 * Signed-in users see avatar/name and actions; guests see a login entry.
 * The dropdown uses a fixed overlay so it does not affect the home grid layout.
 */
export default function AccountMenu() {
  const nav = useNavigate()
  const { auth, isLoggedIn, logout, setAvatar } = useAuth()
  const [open, setOpen] = useState(false)
  const [askAdmin, setAskAdmin] = useState(false)
  const [pickAvatar, setPickAvatar] = useState(false)
  const [avatarBusy, setAvatarBusy] = useState(false)

  if (!isLoggedIn || !auth) {
    return (
      <button className="mm-btn mm-btn--ghost" onClick={() => nav('/login')}>
        Log In
      </button>
    )
  }

  const onLogout = async () => {
    setOpen(false)
    try {
      await musicApi.logout(auth.userId)
    } catch {
      /* Clear local session even if backend logout fails. */
    }
    logout()
    toast.info('Logged out')
    nav('/')
  }

  // Change avatar and sync auth after the backend saves it.
  const onPickAvatar = async (n: number) => {
    setAvatarBusy(true)
    try {
      const res = await musicApi.changeAvatar(auth.userId, n)
      if (res.success) {
        setAvatar(n)
        setPickAvatar(false)
        toast.success('Avatar updated!')
      }
      // The interceptor shows the failure message.
    } catch {
      /* Network errors are handled by the interceptor. */
    } finally {
      setAvatarBusy(false)
    }
  }

  return (
    <div className="mm-account">
      <button className="mm-account__trigger" onClick={() => setOpen((v) => !v)}>
        <img className="mm-avatar mm-avatar--img" src={avatarUrl(auth.avatar)} alt="My avatar" />
        <span className="mm-account__name">{auth.userName}</span>
        <span className="mm-account__caret">▾</span>
      </button>

      {open && (
        <>
          {/* Transparent fixed overlay; click blank space to close. */}
          <div className="mm-overlay" onClick={() => setOpen(false)} />
          <div className="mm-menu" role="menu">
            <button
              className="mm-menu__item"
              role="menuitem"
              onClick={() => {
                setOpen(false)
                nav('/upload')
              }}
            >
              ⬆️ Upload Music
            </button>
            <button
              className="mm-menu__item"
              role="menuitem"
              onClick={() => {
                setOpen(false)
                nav('/interests?mode=edit')
              }}
            >
              🎯 Update Interests
            </button>
            <button
              className="mm-menu__item"
              role="menuitem"
              onClick={() => {
                setOpen(false)
                setPickAvatar(true)
              }}
            >
              🖼️ Change Avatar
            </button>
            <button
              className="mm-menu__item"
              role="menuitem"
              onClick={() => {
                setOpen(false)
                setAskAdmin(true)
              }}
            >
              🎛️ Reward Admin
            </button>
            <div className="mm-menu__sep" />
            <button className="mm-menu__item mm-menu__item--danger" role="menuitem" onClick={onLogout}>
              ⎋ Log Out
            </button>
          </div>
        </>
      )}

      {pickAvatar && (
        <AvatarPickerModal
          current={auth.avatar ?? 0}
          busy={avatarBusy}
          onSelect={onPickAvatar}
          onCancel={() => setPickAvatar(false)}
        />
      )}

      {askAdmin && (
        <ConfirmDialog
          title="Switch to Admin?"
          text="You are leaving the user interface and entering reward administration for prizes, rules, music, and records."
          confirmText="Enter Admin"
          onConfirm={() => {
            setAskAdmin(false)
            nav('/admin')
          }}
          onCancel={() => setAskAdmin(false)}
        />
      )}
    </div>
  )
}
