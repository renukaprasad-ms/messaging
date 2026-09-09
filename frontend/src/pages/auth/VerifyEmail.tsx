import { useState, type FormEvent } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Navigate, useNavigate } from 'react-router'
import { authService, getApiErrorMessage } from '../../service/authService'
import { setUser } from '../../store/auth/authSlice'
import type { RootState } from '../../store/store'
import { homePath } from '../../auth/access'

export default function VerifyEmail() {
  const user = useSelector((state: RootState) => state.auth.user)
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const [otp, setOtp] = useState('')
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  if (user?.status === 'ACTIVE') return <Navigate to={homePath(user)} replace />

  async function send() {
    setBusy(true)
    try {
      await authService.sendVerificationEmail()
      setMessage('Code sent. Check your email. Wait one minute before requesting another code.')
    } catch (error) {
      setMessage(getApiErrorMessage(error))
    } finally {
      setBusy(false)
    }
  }

  async function verify(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    try {
      await authService.verifyEmail(otp)
      const response = await authService.me()
      if (response.data) {
        dispatch(setUser(response.data))
        navigate(homePath(response.data), { replace: true })
      }
    } catch (error) {
      setMessage(getApiErrorMessage(error))
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="mx-auto max-w-md space-y-5 p-8">
      <h1 className="text-2xl font-bold">Verify your email</h1>
      <p>Verify {user?.email} to enter your workspace.</p>
      <button disabled={busy} onClick={send} className="rounded border px-4 py-2">
        Send verification code
      </button>
      <form onSubmit={verify} className="space-y-4">
        <label className="block">
          Verification code
          <input
            value={otp}
            onChange={(event) => setOtp(event.target.value)}
            inputMode="numeric"
            autoComplete="one-time-code"
            pattern="[0-9]{4,8}"
            required
            className="mt-2 w-full rounded border p-3"
          />
        </label>
        <button disabled={busy} className="rounded bg-indigo-600 px-4 py-2 text-white">
          {busy ? 'Please wait…' : 'Verify email'}
        </button>
      </form>
      <p role="status">{message}</p>
    </section>
  )
}
