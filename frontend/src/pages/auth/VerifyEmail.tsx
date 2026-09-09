import { useState, type FormEvent } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Navigate, useNavigate } from 'react-router'
import { homePath } from '../../auth/access'
import { authService, getApiErrorMessage } from '../../service/authService'
import { userService } from '../../service/userService'
import { setUser } from '../../store/auth/authSlice'
import type { RootState } from '../../store/store'

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
      const response = await userService.me()
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
    <section>
      <div className="mb-8">
        <h1 className="text-3xl font-bold tracking-tight text-slate-950">Verify your email</h1>
        <p className="mt-2 break-words text-sm leading-6 text-slate-500">
          Verify {user?.email} to enter your workspace.
        </p>
      </div>

      <button
        type="button"
        disabled={busy}
        onClick={send}
        className="mb-5 h-12 w-full rounded-xl border border-slate-200 text-sm font-semibold text-slate-700 transition hover:border-indigo-200 hover:bg-indigo-50 hover:text-indigo-600 disabled:cursor-not-allowed disabled:bg-slate-50 disabled:text-slate-400"
      >
        Send verification code
      </button>

      <form onSubmit={verify} className="space-y-5">
        <label className="block">
          <span className="mb-2 block text-sm font-medium text-slate-800">Verification code</span>
          <input
            value={otp}
            onChange={(event) => setOtp(event.target.value)}
            inputMode="numeric"
            autoComplete="one-time-code"
            pattern="[0-9]{4,8}"
            required
            className="h-12 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50"
          />
        </label>

        <button
          type="submit"
          disabled={busy}
          className="h-12 w-full rounded-xl bg-indigo-500 text-sm font-semibold text-white shadow-lg shadow-indigo-100 transition hover:bg-indigo-600 disabled:cursor-not-allowed disabled:bg-indigo-300"
        >
          {busy ? 'Please wait...' : 'Verify email'}
        </button>
      </form>

      {message ? (
        <p role="status" className="mt-5 rounded-xl bg-slate-50 px-4 py-3 text-sm text-slate-600">
          {message}
        </p>
      ) : null}
    </section>
  )
}
