import { Link } from "react-router";

const Login = () => {
  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold tracking-tight text-slate-950">Welcome back</h1>
        <p className="mt-2 text-sm text-slate-500">Sign in to continue to your workspace.</p>
      </div>

      <form className="space-y-5">
        <label className="block">
          <span className="mb-2 block text-sm font-medium text-slate-800">Email address</span>
          <input type="email" placeholder="you@example.com" className="h-12 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" />
        </label>
        <label className="block">
          <span className="mb-2 block text-sm font-medium text-slate-800">Password</span>
          <input type="password" placeholder="••••••••" className="h-12 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" />
        </label>

        <div className="flex items-center justify-between text-xs">
          <label className="flex items-center gap-2 text-slate-500"><input type="checkbox" className="accent-indigo-500" /> Remember me</label>
          <Link to="/forgot-password" className="font-semibold text-indigo-500 hover:text-indigo-600">Forgot password?</Link>
        </div>

        <button type="submit" className="h-12 w-full rounded-xl bg-indigo-500 text-sm font-semibold text-white shadow-lg shadow-indigo-100 transition hover:bg-indigo-600">Sign in</button>
      </form>

      <p className="mt-7 text-center text-xs text-slate-500">
        Don&apos;t have an account? <Link to="/register" className="font-semibold text-indigo-500">Create account</Link>
      </p>
    </div>
  );
};

export default Login;
