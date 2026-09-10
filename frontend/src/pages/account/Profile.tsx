import { useState, type FormEvent } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { getApiErrorMessage } from '../../service/authService'
import { userService } from '../../service/userService'
import { setUser } from '../../store/auth/authSlice'
import type { RootState } from '../../store/store'

export default function Profile() {
  const dispatch = useDispatch()
  const user = useSelector((state: RootState) => state.auth.user)
  const [name, setName] = useState(user?.name ?? '')
  const [phone, setPhone] = useState(user?.phone ?? '')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function saveProfile(event: FormEvent) {
    event.preventDefault()
    setMessage('')
    setError('')
    setBusy(true)
    try {
      const response = await userService.updateMe({ name, phone })
      if (response.data) dispatch(setUser(response.data))
      setMessage('Profile updated.')
    } catch (error) {
      setError(getApiErrorMessage(error, 'Unable to update profile. Please try again.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="space-y-6 p-5 lg:p-7">
      <div>
        <p className="eyebrow">ACCOUNT</p>
        <h1 className="mt-2 text-2xl font-bold tracking-tight">Profile</h1>
      </div>

      <form onSubmit={saveProfile} className="panel max-w-5xl p-6">
        <div className="grid gap-5 md:grid-cols-2">
          <label className="block text-sm font-medium">
            Name
            <input
              className="form-input mt-2"
              value={name}
              onChange={(event) => setName(event.target.value)}
              required
              maxLength={150}
            />
          </label>
          <label className="block text-sm font-medium">
            Phone
            <input
              className="form-input mt-2"
              value={phone}
              onChange={(event) => setPhone(event.target.value)}
              maxLength={30}
            />
          </label>
          <label className="block text-sm font-medium md:col-span-2">
            Email
            <input className="form-input mt-2 bg-slate-50" value={user?.email ?? ''} readOnly />
          </label>
        </div>
        {(message || error) && (
          <p
            role={error ? 'alert' : 'status'}
            className={`mt-5 text-sm ${error ? 'text-red-600' : 'text-emerald-700'}`}
          >
            {error || message}
          </p>
        )}
        <button className="primary-button mt-6" disabled={busy}>
          {busy ? 'Saving...' : 'Save profile'}
        </button>
      </form>
    </section>
  )
}
