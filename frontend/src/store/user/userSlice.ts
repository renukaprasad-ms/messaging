import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { AuthUser } from '../../features/auth/types'

type UserState = {
  user: AuthUser | null
  isAuthenticated: boolean
  bootstrapped: boolean
}

const initialState: UserState = {
  user: null,
  isAuthenticated: false,
  bootstrapped: false,
}

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    setAuthUser: (state, action: PayloadAction<AuthUser>) => {
      state.user = action.payload
      state.isAuthenticated = true
      state.bootstrapped = true
    },
    clearAuthUser: (state) => {
      state.user = null
      state.isAuthenticated = false
      state.bootstrapped = true
    },
    setBootstrapped: (state) => {
      state.bootstrapped = true
    },
  },
})

export const { setAuthUser, clearAuthUser, setBootstrapped } = userSlice.actions
export default userSlice.reducer
