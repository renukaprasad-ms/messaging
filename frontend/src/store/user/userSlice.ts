import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

type UserState = {
  isAuthenticated: boolean
}

const initialState: UserState = {
  isAuthenticated: false,
}

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    setIsAuthenticated: (state, action: PayloadAction<boolean>) => {
      state.isAuthenticated = action.payload
    },
  },
})

export const { setIsAuthenticated } = userSlice.actions
export default userSlice.reducer
