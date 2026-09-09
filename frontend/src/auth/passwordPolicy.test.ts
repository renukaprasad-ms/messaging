import { describe, expect, it } from 'vitest'
import { isStrongPassword, passwordStrength } from './passwordPolicy'

describe('password policy', () => {
  it.each(['', 'Aa1!abc', 'abcdefgh1!', 'ABCDEFGH1!', 'Abcdefgh!', 'Abcdefgh1', 'Abcdef1 '])(
    'rejects incomplete requirements: %s',
    (password) => {
      expect(isStrongPassword(password)).toBe(false)
    },
  )
  it('accepts the boundaries and rejects excessive characters or UTF-8 bytes', () => {
    expect(isStrongPassword('Abcdef1!')).toBe(true)
    expect(isStrongPassword('Aa1!' + 'x'.repeat(68))).toBe(true)
    expect(isStrongPassword('Aa1!' + 'x'.repeat(69))).toBe(false)
    expect(isStrongPassword('Aa1!' + 'é'.repeat(35))).toBe(false)
  })
  it('does not call an invalid password strong', () => {
    expect(passwordStrength('a'.repeat(30)).label).toBe('Weak')
    expect(passwordStrength('Abcdef1!').label).toBe('Good')
    expect(passwordStrength('A-long-password-1!').label).toBe('Strong')
  })
})
