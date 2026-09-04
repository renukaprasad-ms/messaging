import { useRef, useState, type ChangeEvent, type ClipboardEvent, type FormEvent, type KeyboardEvent } from "react";
import { useLocation, useNavigate } from "react-router";
import { authService, getApiErrorMessage } from "../../service/authService";

interface VerifyOtpLocationState {
  identifier?: string
}

const VerifyOtp = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { identifier = "" } = (location.state as VerifyOtpLocationState | null) ?? {};
  const [digits, setDigits] = useState(Array<string>(6).fill(""));
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(identifier ? "" : "Please request a password reset code first.");
  const inputRefs = useRef<Array<HTMLInputElement | null>>([]);

  const otp = digits.join("");

  const updateDigit = (index: number, event: ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value.replace(/\D/g, "");
    if (!value) {
      setDigits((current) => current.map((digit, digitIndex) => digitIndex === index ? "" : digit));
      return;
    }

    const nextDigits = value.slice(0, 6 - index).split("");
    setDigits((current) => current.map((digit, digitIndex) => {
      if (digitIndex < index || digitIndex >= index + nextDigits.length) {
        return digit;
      }

      return nextDigits[digitIndex - index];
    }));

    const nextIndex = Math.min(index + nextDigits.length, digits.length - 1);
    inputRefs.current[nextIndex]?.focus();
  };

  const handleKeyDown = (index: number, event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key !== "Backspace" || digits[index]) {
      return;
    }

    inputRefs.current[index - 1]?.focus();
  };

  const handlePaste = (index: number, event: ClipboardEvent<HTMLInputElement>) => {
    event.preventDefault();
    const pastedDigits = event.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6 - index);
    if (!pastedDigits) {
      return;
    }

    setDigits((current) => current.map((digit, digitIndex) => {
      if (digitIndex < index || digitIndex >= index + pastedDigits.length) {
        return digit;
      }

      return pastedDigits[digitIndex - index];
    }));

    const nextIndex = Math.min(index + pastedDigits.length, digits.length - 1);
    inputRefs.current[nextIndex]?.focus();
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");

    if (!identifier) {
      setError("Please request a password reset code first.");
      return;
    }

    if (otp.length !== 6) {
      setError("Enter the 6-digit verification code.");
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await authService.verifyResetOtp({ identifier, otp });
      navigate("/reset-password", { state: { resetToken: response.data?.resetToken } });
    } catch (err) {
      setError(getApiErrorMessage(err, "Unable to verify reset code. Please try again."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <h1 className="text-3xl font-bold tracking-tight text-slate-950">Verify your code</h1>
      <p className="mt-2 text-sm leading-6 text-slate-500">We sent a 6-digit verification code to your email or phone.</p>

      <form onSubmit={handleSubmit}>
        <div className="mt-8 flex gap-2">
          {digits.map((digit, index) => (
            <input
              key={index}
              ref={(element) => {
                inputRefs.current[index] = element;
              }}
              type="text"
              inputMode="numeric"
              value={digit}
              onChange={(event) => updateDigit(index, event)}
              onKeyDown={(event) => handleKeyDown(index, event)}
              onPaste={(event) => handlePaste(index, event)}
              maxLength={6}
              required
              className="h-14 min-w-0 flex-1 rounded-xl border border-slate-200 text-center text-lg font-semibold outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50"
            />
          ))}
        </div>

        {error ? <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-600">{error}</p> : null}

        <button type="submit" disabled={isSubmitting || !identifier} className="mt-6 h-12 w-full rounded-xl bg-indigo-500 text-sm font-semibold text-white hover:bg-indigo-600 disabled:cursor-not-allowed disabled:bg-indigo-300">
          {isSubmitting ? "Verifying..." : "Verify code"}
        </button>
      </form>
    </div>
  );
};

export default VerifyOtp;
