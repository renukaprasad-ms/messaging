import { AxiosError } from 'axios'
import apiClient from './apiClient'

export interface ApiResponse<T = void> {
  status: boolean
  status_code: number
  data?: T
  message?: string
  error_message?: string
}

export type UserStatus = 'PENDING_VERIFICATION' | 'ACTIVE' | 'SUSPENDED' | 'DISABLED'

export interface AuthUser {
  name: string
  email: string
  phone: string
  hasCompany: boolean
  status: UserStatus
  platformRoles: Array<'ADMIN' | 'SUPERADMIN'>
  passwordChangeRequired: boolean
}

export interface LoginRequest {
  identifier: string
  password: string
  rememberMe: boolean
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
  confirmPassword: string
  phone: string
}

export interface ForgotPasswordRequest {
  identifier: string
}

export interface VerifyResetOtpRequest {
  identifier: string
  otp: string
}

export interface VerifyResetOtpResponse {
  resetToken: string
}

export interface ResetPasswordRequest {
  resetToken: string
  password: string
  confirmPassword: string
}

export const authService = {
  async changePassword(payload: {
    currentPassword: string
    password: string
    confirmPassword: string
  }) {
    await apiClient.post('/api/auth/change-password', payload)
  },
  async me() {
    const response = await apiClient.get<ApiResponse<AuthUser>>('/api/auth/me')
    return response.data
  },

  async sendVerificationEmail() {
    await apiClient.post('/api/users/verification/email')
  },

  async verifyEmail(otp: string) {
    await apiClient.post('/api/users/verification/email/verify', { otp })
  },

  async register(payload: RegisterRequest) {
    const response = await apiClient.post<ApiResponse<AuthUser>>('/api/auth/register', payload)
    return response.data
  },

  async login(payload: LoginRequest) {
    const response = await apiClient.post<ApiResponse<AuthUser>>('/api/auth/login', payload)
    return response.data
  },

  async refresh() {
    const response = await apiClient.post<ApiResponse<AuthUser>>('/api/auth/refresh')
    return response.data
  },

  async logout() {
    const response = await apiClient.post<ApiResponse>('/api/auth/logout')
    return response.data
  },

  async forgotPassword(payload: ForgotPasswordRequest) {
    const response = await apiClient.post<ApiResponse>('/api/auth/forgot-password', payload)
    return response.data
  },

  async verifyResetOtp(payload: VerifyResetOtpRequest) {
    const response = await apiClient.post<ApiResponse<VerifyResetOtpResponse>>(
      '/api/auth/verify-reset-otp',
      payload,
    )
    return response.data
  },

  async resetPassword(payload: ResetPasswordRequest) {
    const response = await apiClient.post<ApiResponse>('/api/auth/reset-password', payload)
    return response.data
  },
}

export const getApiErrorMessage = (
  error: unknown,
  fallback = 'Something went wrong. Please try again.',
) => {
  if (error instanceof AxiosError) {
    const response = error.response?.data as ApiResponse | undefined
    return response?.error_message ?? response?.message ?? error.message ?? fallback
  }

  if (error instanceof Error) {
    return error.message
  }

  return fallback
}

export default authService
