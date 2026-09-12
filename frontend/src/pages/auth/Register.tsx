import { Link } from 'react-router'
import { useState } from 'react'
import { useDispatch } from 'react-redux'
import { useNavigate } from 'react-router'
import { authApi } from '../../features/auth/authApi'
import { mediaApi } from '../../features/media/mediaApi'
import { apiErrorMessage } from '../../lib/api/apiClient'
import { setAuthUser } from '../../store/user/userSlice'
import type { AppDispatch } from '../../store/store'

const Register = () => {
  const dispatch = useDispatch<AppDispatch>()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [profilePicture, setProfilePicture] = useState<File | null>(null)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const onSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const user = await authApi.register({
        email,
        name,
        username,
        password,
        profilePicture: profilePicture?.name ?? null,
      })
      dispatch(setAuthUser(user))
      let profilePictureWarning: string | undefined
      if (profilePicture) {
        try {
          await mediaApi.uploadProfilePicture(profilePicture)
        } catch (uploadError) {
          profilePictureWarning = apiErrorMessage(uploadError, 'Profile picture upload failed')
        }
      }
      navigate('/verifyotp', {
        replace: true,
        state: { email: user.email, profilePictureWarning },
      })
    } catch (submitError) {
      setError(apiErrorMessage(submitError, 'Unable to register'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="panel p-6 shadow-sm">
      <p className="eyebrow">Get started</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight text-slate-950">Register</h1>
      <p className="mt-2 text-sm leading-6 text-slate-500">
        Create your account and verify it with an OTP.
      </p>

      <form className="mt-6 space-y-4" onSubmit={onSubmit}>
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold text-slate-700">Name</span>
          <input
            className="form-input"
            type="text"
            placeholder="Your name"
            value={name}
            onChange={(event) => setName(event.target.value)}
            autoComplete="name"
            required
          />
        </label>
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold text-slate-700">Email</span>
          <input
            className="form-input"
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
            required
          />
        </label>
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold text-slate-700">Username</span>
          <input
            className="form-input"
            type="text"
            placeholder="yourname"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            autoComplete="username"
            minLength={3}
            maxLength={50}
            required
          />
        </label>
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold text-slate-700">Password</span>
          <input
            className="form-input"
            type="password"
            placeholder="Create password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="new-password"
            minLength={8}
            required
          />
        </label>
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold text-slate-700">
            Profile picture
          </span>
          <input
            className="form-input py-2"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            onChange={(event) => setProfilePicture(event.target.files?.[0] ?? null)}
          />
        </label>
        {error && <p className="rounded-lg bg-rose-50 p-3 text-sm text-rose-600">{error}</p>}
        <button type="submit" className="primary-button flex w-full" disabled={submitting}>
          {submitting ? 'Creating account...' : 'Register'}
        </button>
      </form>

      <p className="mt-5 text-center text-sm text-slate-500">
        Already registered?{' '}
        <Link className="font-semibold text-indigo-500" to="/login">
          Login
        </Link>
      </p>
    </div>
  )
}

export default Register
