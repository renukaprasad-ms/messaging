import { Link } from 'react-router'

const VerifyOtp = () => {
  return (
    <div className="panel p-6 shadow-sm">
      <p className="eyebrow">Verification</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight text-slate-950">Verify OTP</h1>
      <p className="mt-2 text-sm leading-6 text-slate-500">
        Enter the one-time password sent to your email or phone.
      </p>

      <form className="mt-6 space-y-4">
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold text-slate-700">OTP</span>
          <input className="form-input text-center tracking-[0.35em]" inputMode="numeric" maxLength={6} />
        </label>
        <button type="button" className="primary-button flex w-full">
          Verify OTP
        </button>
      </form>

      <p className="mt-5 text-center text-sm text-slate-500">
        Need to start over?{' '}
        <Link className="font-semibold text-indigo-500" to="/login">
          Back to login
        </Link>
      </p>
    </div>
  )
}

export default VerifyOtp
