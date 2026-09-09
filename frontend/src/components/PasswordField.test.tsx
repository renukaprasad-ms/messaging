import { fireEvent, render, screen } from '@testing-library/react'
import { useState } from 'react'
import { expect, it } from 'vitest'
import PasswordField from './PasswordField'

function Form() {
  const [value, setValue] = useState('')
  return <PasswordField value={value} onChange={setValue} />
}

it('updates the accessible strength bar and supports showing the password', () => {
  render(<Form />)
  const input = screen.getByLabelText('Password')
  expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '0')
  fireEvent.change(input, { target: { value: 'A-long-password-1!' } })
  expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuetext', 'Strong')
  expect(input).toHaveAttribute('maxlength', '72')
  expect(input).toHaveAttribute('type', 'password')
  fireEvent.click(screen.getByRole('button', { name: 'Show password' }))
  expect(input).toHaveAttribute('type', 'text')
})
