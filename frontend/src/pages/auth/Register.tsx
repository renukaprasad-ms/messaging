import { Link } from 'react-router'

const Register = () => {
  return (
    <div className="panel p-6 shadow-sm">
      <p className="eyebrow">Get started</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight text-slate-950">Register</h1>
      <p className="mt-2 text-sm leading-6 text-slate-500">
        Create your account and verify it with an OTP.
      </p>

      <form className="mt-6 space-y-4">
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold text-slate-700">Name</span>
          <input className="form-input" type="text" placeholder="Your name" />
        </label>
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold text-slate-700">Email</span>
          <input className="form-input" type="email" placeholder="you@example.com" />
        </label>
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold text-slate-700">Password</span>
          <input className="form-input" type="password" placeholder="Create password" />
        </label>
        <button type="button" className="primary-button flex w-full">
          Register
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
