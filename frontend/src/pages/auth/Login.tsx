import { useState, type FormEvent } from "react";
import { useDispatch } from "react-redux";
import { Link, useNavigate } from "react-router";
import { authService, getApiErrorMessage } from "../../service/authService";
import { setLoading, setUser } from "../../store/auth/authSlice";

const Login = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);
    dispatch(setLoading(true));

    try {
      const response = await authService.login({ identifier, password, rememberMe });

      if (response.data) {
        dispatch(setUser(response.data));
      }

      navigate("/", { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, "Unable to sign in. Please check your details."));
    } finally {
      setIsSubmitting(false);
      dispatch(setLoading(false));
    }
  };

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold tracking-tight text-slate-950">Welcome back</h1>
        <p className="mt-2 text-sm text-slate-500">Sign in to continue to your workspace.</p>
      </div>

      <form className="space-y-5" onSubmit={handleSubmit}>
        <label className="block">
          <span className="mb-2 block text-sm font-medium text-slate-800">Email address</span>
          <input type="email" value={identifier} onChange={(event) => setIdentifier(event.target.value)} placeholder="you@example.com" autoComplete="email" required className="h-12 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" />
        </label>
        <label className="block">
          <span className="mb-2 block text-sm font-medium text-slate-800">Password</span>
          <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Password" autoComplete="current-password" required className="h-12 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" />
        </label>

        <div className="flex items-center justify-between text-xs">
          <label className="flex items-center gap-2 text-slate-500"><input type="checkbox" checked={rememberMe} onChange={(event) => setRememberMe(event.target.checked)} className="accent-indigo-500" /> Remember me</label>
          <Link to="/forgot-password" className="font-semibold text-indigo-500 hover:text-indigo-600">Forgot password?</Link>
        </div>

        {error ? <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-600">{error}</p> : null}

        <button type="submit" disabled={isSubmitting} className="h-12 w-full rounded-xl bg-indigo-500 text-sm font-semibold text-white shadow-lg shadow-indigo-100 transition hover:bg-indigo-600 disabled:cursor-not-allowed disabled:bg-indigo-300">
          {isSubmitting ? "Signing in..." : "Sign in"}
        </button>
      </form>

      <p className="mt-7 text-center text-xs text-slate-500">
        Don&apos;t have an account? <Link to="/register" className="font-semibold text-indigo-500">Create account</Link>
      </p>
    </div>
  );
};

export default Login;
