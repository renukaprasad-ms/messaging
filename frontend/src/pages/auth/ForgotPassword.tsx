import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router";
import { authService, getApiErrorMessage } from "../../service/authService";

const ForgotPassword = () => {
  const navigate = useNavigate();
  const [identifier, setIdentifier] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      await authService.forgotPassword({ identifier });
      navigate("/verify-otp", { state: { identifier } });
    } catch (err) {
      setError(getApiErrorMessage(err, "Unable to send reset code. Please try again."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <h1 className="text-3xl font-bold tracking-tight text-slate-950">Forgot your password?</h1>
      <p className="mt-2 text-sm leading-6 text-slate-500">Enter your email address or phone number and we&apos;ll send instructions to reset your password.</p>

      <form className="mt-8 space-y-5" onSubmit={handleSubmit}>
        <label className="block">
          <span className="mb-2 block text-sm font-medium text-slate-800">Email address or phone number</span>
          <input type="text" value={identifier} onChange={(event) => setIdentifier(event.target.value)} placeholder="you@example.com or 7022129610" autoComplete="username" required className="h-12 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" />
        </label>

        {error ? <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-600">{error}</p> : null}

        <button type="submit" disabled={isSubmitting} className="h-12 w-full rounded-xl bg-indigo-500 text-sm font-semibold text-white hover:bg-indigo-600 disabled:cursor-not-allowed disabled:bg-indigo-300">
          {isSubmitting ? "Sending..." : "Continue"}
        </button>
      </form>

      <Link to="/login" className="mt-6 block text-center text-xs font-semibold text-indigo-500">Back to sign in</Link>
    </div>
  );
};

export default ForgotPassword;
