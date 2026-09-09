import { useId, useState } from 'react'
import { passwordRequirements, passwordStrength } from '../auth/passwordPolicy'

interface PasswordFieldProps {
  label?: string
  value: string
  onChange: (value: string) => void
}

export default function PasswordField({ label = 'Password', value, onChange }: PasswordFieldProps) {
  const id = useId()
  const [visible, setVisible] = useState(false)
  const strength = passwordStrength(value)
  const colors = [
    'bg-slate-200',
    'bg-red-500',
    'bg-orange-500',
    'bg-amber-500',
    'bg-indigo-500',
    'bg-emerald-500',
  ]
  return (
    <div className="space-y-3">
      <label htmlFor={id} className="block text-sm font-medium text-slate-800">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          type={visible ? 'text' : 'password'}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          minLength={8}
          maxLength={72}
          required
          autoComplete="new-password"
          aria-describedby={`${id}-rules ${id}-strength`}
          className="h-12 w-full rounded-xl border border-slate-200 px-4 pr-16 text-sm outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50"
        />
        <button
          type="button"
          onClick={() => setVisible(!visible)}
          aria-label={visible ? 'Hide password' : 'Show password'}
          className="absolute inset-y-0 right-3 text-xs font-semibold text-indigo-600"
        >
          {visible ? 'Hide' : 'Show'}
        </button>
      </div>
      <div
        role="progressbar"
        aria-label="Password strength"
        aria-valuemin={0}
        aria-valuemax={5}
        aria-valuenow={strength.score}
        aria-valuetext={strength.label}
        className="h-1.5 overflow-hidden rounded-full bg-slate-100"
      >
        <div
          className={`h-full rounded-full transition-all duration-200 ${colors[strength.score]}`}
          style={{ width: `${strength.score * 20}%` }}
        />
      </div>
      <p id={`${id}-strength`} aria-live="polite" className="text-xs font-medium text-slate-600">
        {strength.label}
      </p>
      <ul id={`${id}-rules`} className="grid gap-1 text-xs sm:grid-cols-2">
        {passwordRequirements(value).map((rule) => (
          <li key={rule.label} className={rule.met ? 'text-emerald-700' : 'text-slate-500'}>
            <span aria-hidden="true">{rule.met ? '✓' : '○'}</span> {rule.label}
            <span className="sr-only">{rule.met ? ': met' : ': not met'}</span>
          </li>
        ))}
      </ul>
      {new TextEncoder().encode(value).length > 72 && (
        <p role="alert" className="text-xs text-red-600">
          Use fewer characters: this password exceeds the 72-byte limit.
        </p>
      )}
    </div>
  )
}
