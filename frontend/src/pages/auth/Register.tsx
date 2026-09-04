import { useState, type FormEvent } from "react";
import { useDispatch } from "react-redux";
import { Link, useNavigate } from "react-router";
import { authService, getApiErrorMessage } from "../../service/authService";
import { setLoading, setUser } from "../../store/auth/authSlice";

const Register = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const updateField = (field: keyof typeof form, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");

    if (form.password !== form.confirmPassword) {
      setError("Password and confirm password must match.");
      return;
    }

    setIsSubmitting(true);
    dispatch(setLoading(true));

    try {
      const response = await authService.register(form);

      if (response.data) {
        dispatch(setUser(response.data));
      }

      navigate(response.data?.hasCompany ? "/" : "/company/create", { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, "Unable to create account. Please try again."));
    } finally {
      setIsSubmitting(false);
      dispatch(setLoading(false));
    }
  };

  return (
    <div>
      <h1 className="text-3xl font-bold tracking-tight text-slate-950">Create your account</h1>
      <p className="mt-2 text-sm leading-6 text-slate-500">Start managing customer conversations and campaigns from one place.</p>

      <form className="mt-7 space-y-4" onSubmit={handleSubmit}>
        <label className="block"><span className="mb-2 block text-sm font-medium text-slate-800">Full name</span><input type="text" value={form.name} onChange={(event) => updateField("name", event.target.value)} placeholder="Your name" autoComplete="name" required className="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" /></label>
        <label className="block"><span className="mb-2 block text-sm font-medium text-slate-800">Email address</span><input type="email" value={form.email} onChange={(event) => updateField("email", event.target.value)} placeholder="you@example.com" autoComplete="email" required className="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" /></label>
        <label className="block"><span className="mb-2 block text-sm font-medium text-slate-800">Phone number</span><input type="tel" value={form.phone} onChange={(event) => updateField("phone", event.target.value)} placeholder="+91 98765 43210" autoComplete="tel" required className="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" /></label>
        <label className="block"><span className="mb-2 block text-sm font-medium text-slate-800">Password</span><input type="password" value={form.password} onChange={(event) => updateField("password", event.target.value)} placeholder="Create a password" autoComplete="new-password" required className="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" /></label>
        <label className="block"><span className="mb-2 block text-sm font-medium text-slate-800">Confirm password</span><input type="password" value={form.confirmPassword} onChange={(event) => updateField("confirmPassword", event.target.value)} placeholder="Repeat your password" autoComplete="new-password" required className="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" /></label>

        {error ? <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-600">{error}</p> : null}

        <button type="submit" disabled={isSubmitting} className="mt-2 h-12 w-full rounded-xl bg-indigo-500 text-sm font-semibold text-white shadow-lg shadow-indigo-100 hover:bg-indigo-600 disabled:cursor-not-allowed disabled:bg-indigo-300">
          {isSubmitting ? "Creating account..." : "Create account"}
        </button>
      </form>

      <p className="mt-6 text-center text-xs text-slate-500">Already have an account? <Link to="/login" className="font-semibold text-indigo-500">Sign in</Link></p>
    </div>
  );
};

export default Register;
