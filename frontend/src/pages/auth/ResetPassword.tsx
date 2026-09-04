import { useState, type FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router";
import { authService, getApiErrorMessage } from "../../service/authService";

interface ResetPasswordLocationState {
  resetToken?: string
}

const ResetPassword = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { resetToken = "" } = (location.state as ResetPasswordLocationState | null) ?? {};
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(resetToken ? "" : "Please verify your reset code first.");

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");

    if (!resetToken) {
      setError("Please verify your reset code first.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Password and confirm password must match.");
      return;
    }

    setIsSubmitting(true);

    try {
      await authService.resetPassword({ resetToken, password, confirmPassword });
      navigate("/login", { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, "Unable to reset password. Please try again."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <h1 className="text-3xl font-bold tracking-tight text-slate-950">Create a new password</h1>
      <p className="mt-2 text-sm text-slate-500">Choose a strong password to secure your account.</p>

      <form className="mt-8 space-y-5" onSubmit={handleSubmit}>
        <label className="block">
          <span className="mb-2 block text-sm font-medium text-slate-800">New password</span>
          <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Create a new password" autoComplete="new-password" required minLength={8} className="h-12 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" />
        </label>
        <label className="block">
          <span className="mb-2 block text-sm font-medium text-slate-800">Confirm password</span>
          <input type="password" value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} placeholder="Repeat your new password" autoComplete="new-password" required minLength={8} className="h-12 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" />
        </label>

        {error ? <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-600">{error}</p> : null}

        <button type="submit" disabled={isSubmitting || !resetToken} className="h-12 w-full rounded-xl bg-indigo-500 text-sm font-semibold text-white hover:bg-indigo-600 disabled:cursor-not-allowed disabled:bg-indigo-300">
          {isSubmitting ? "Updating..." : "Update password"}
        </button>
      </form>

      {!resetToken ? <Link to="/forgot-password" className="mt-6 block text-center text-xs font-semibold text-indigo-500">Request a new code</Link> : null}
    </div>
  );
};

export default ResetPassword;
