import { useState, type FormEvent } from 'react'
import { useDispatch } from 'react-redux'
import { useNavigate } from 'react-router'
import PasswordField from '../../components/PasswordField'
import { isStrongPassword } from '../../auth/passwordPolicy'
import { authService, getApiErrorMessage } from '../../service/authService'
import { logout } from '../../store/auth/authSlice'

export default function ChangePassword() {
  const [currentPassword, setCurrentPassword] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const dispatch = useDispatch()
  const navigate = useNavigate()

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (!isStrongPassword(password) || password !== confirmPassword) {
      setError('Meet all password requirements and confirm your password.')
      return
    }
    setBusy(true)
    try {
      await authService.changePassword({ currentPassword, password, confirmPassword })
      dispatch(logout())
      navigate('/login', {
        replace: true,
        state: { message: 'Password changed. Sign in with your new password.' },
      })
    } catch (error) {
      setError(getApiErrorMessage(error))
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="mx-auto max-w-lg p-6 sm:p-8">
      <div className="panel space-y-6 p-6">
        <div>
          <h1 className="text-2xl font-bold">Change password</h1>
          <p className="mt-2 text-sm text-slate-500">
            Choose a new password to secure your account. You’ll sign in again after saving.
          </p>
        </div>
        <form onSubmit={submit} className="space-y-5">
          <label className="block text-sm font-medium">
            Current password
            <input
              type="password"
              autoComplete="current-password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              required
              maxLength={72}
              className="form-input mt-2"
            />
          </label>
          <PasswordField label="New password" value={password} onChange={setPassword} />
          <label className="block text-sm font-medium">
            Confirm password
            <input
              type="password"
              autoComplete="new-password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              maxLength={72}
              className="form-input mt-2"
            />
          </label>
          {error && (
            <p role="alert" className="text-sm text-red-600">
              {error}
            </p>
          )}
          <button
            disabled={busy || !isStrongPassword(password) || password !== confirmPassword}
            className="primary-button w-full"
          >
            {busy ? 'Saving…' : 'Change password'}
          </button>
        </form>
      </div>
    </section>
  )
}
