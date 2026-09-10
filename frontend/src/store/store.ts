import { configureStore } from '@reduxjs/toolkit'
import authReducer from './auth/authSlice'
import { writeStoredUser } from './auth/authStorage'

export const store = configureStore({
  reducer: {
    auth: authReducer,
  },
})

let previousUser = store.getState().auth.user
store.subscribe(() => {
  const user = store.getState().auth.user
  if (user === previousUser) return
  previousUser = user
  writeStoredUser(user)
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
