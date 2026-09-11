import { Link } from 'react-router'

const Login = () => {
  return (
    <div className="panel p-6 shadow-sm">
      <p className="eyebrow">Welcome back</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight text-slate-950">Login</h1>
      <p className="mt-2 text-sm leading-6 text-slate-500">
        Sign in to continue to your messaging workspace.
      </p>

      <form className="mt-6 space-y-4">
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold text-slate-700">Email</span>
          <input className="form-input" type="email" placeholder="you@example.com" />
        </label>
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold text-slate-700">Password</span>
          <input className="form-input" type="password" placeholder="Enter password" />
        </label>
        <button type="button" className="primary-button flex w-full">
          Login
        </button>
      </form>

      <p className="mt-5 text-center text-sm text-slate-500">
        New here?{' '}
        <Link className="font-semibold text-indigo-500" to="/register">
          Create account
        </Link>
      </p>
    </div>
  )
}

export default Login
