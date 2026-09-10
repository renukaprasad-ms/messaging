import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { AuthUser } from '../../service/authService'
import { readStoredUser } from './authStorage'

interface AuthState {
  user: AuthUser | null
  loading: boolean
  initialized: boolean
}

const initialState: AuthState = { user: readStoredUser(), loading: false, initialized: false }

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setUser(state, action: PayloadAction<AuthUser>) {
      state.user = action.payload
    },
    logout(state) {
      state.user = null
    },
    setLoading(state, action: PayloadAction<boolean>) {
      state.loading = action.payload
    },
    sessionReady(state) {
      state.initialized = true
    },
  },
})

export const { setUser, logout, setLoading, sessionReady } = authSlice.actions
export default authSlice.reducer
