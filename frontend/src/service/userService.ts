import apiClient from './apiClient'
import type { ApiResponse, AuthUser } from './authService'

export interface AccountUpdateRequest {
  name: string
  phone: string
  profilePhotoUrl?: string
}

export const userService = {
  async me() {
    const response = await apiClient.get<ApiResponse<AuthUser>>('/api/users/me')
    return response.data
  },

  async updateMe(payload: AccountUpdateRequest) {
    const response = await apiClient.patch<ApiResponse<AuthUser>>('/api/users/me', payload)
    return response.data
  },
}

export default userService
