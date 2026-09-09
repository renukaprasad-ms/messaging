export const PASSWORD_MIN_LENGTH = 8
export const PASSWORD_MAX_LENGTH = 72

export function passwordRequirements(password: string) {
  const bytes = new TextEncoder().encode(password).length
  return [
    { label: '8–72 characters', met: password.length >= 8 && password.length <= 72 && bytes <= 72 },
    { label: 'One uppercase letter', met: /[A-Z]/.test(password) },
    { label: 'One lowercase letter', met: /[a-z]/.test(password) },
    { label: 'One number', met: /[0-9]/.test(password) },
    { label: 'One special character', met: /[!-/:-@[-`{-~]/.test(password) },
  ]
}

export function isStrongPassword(password: string): boolean {
  return passwordRequirements(password).every((rule) => rule.met)
}

export function passwordStrength(password: string) {
  const requirements = passwordRequirements(password)
  const met = requirements.filter((rule) => rule.met).length
  const valid = requirements.every((rule) => rule.met)
  if (!password) return { score: 0, label: 'Enter a password', valid }
  if (!valid)
    return { score: Math.min(met, 3), label: met < 3 ? 'Weak' : 'Needs improvement', valid }
  return {
    score: password.length >= 16 ? 5 : 4,
    label: password.length >= 16 ? 'Strong' : 'Good',
    valid,
  }
}
