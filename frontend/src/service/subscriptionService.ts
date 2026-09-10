import apiClient from './apiClient'
import type { AccountType, ApiResponse } from './authService'

export type BillingChannel = 'EMAIL' | 'WHATSAPP' | 'SMS'
export type MessageType = 'PROMOTIONAL' | 'CONVERSATIONAL' | 'TRANSACTIONAL'
export type SubscriptionStatus = 'ACTIVE' | 'TRIALING' | 'PAST_DUE' | 'CANCELLED'

export interface PlanLimit {
  featureKey: string
  limitValue: number
  unit: string
}

export interface ChannelPricing {
  channel: BillingChannel
  messageType: MessageType
  pricePerSegment: number
  freeDailySegments: number
  currency: string
  active: boolean
}

export interface SubscriptionPlan {
  id: number
  code: string
  accountType: AccountType
  name: string
  monthlyPrice: number
  currency: string
  active: boolean
  limits: PlanLimit[]
  pricing: ChannelPricing[]
}

export interface CompanySubscription {
  id: number
  companyId: number
  status: SubscriptionStatus
  startsAt: string
  endsAt?: string
  renewsAt?: string
  plan: SubscriptionPlan
}

export interface SubscriptionPlanRequest {
  code: string
  accountType: AccountType
  name: string
  monthlyPrice: number
  currency: string
  active: boolean
  limits: PlanLimit[]
  pricing: ChannelPricing[]
}

export const subscriptionService = {
  async listPlans(accountType?: AccountType, signal?: AbortSignal) {
    const response = await apiClient.get<ApiResponse<SubscriptionPlan[]>>(
      '/api/subscription-plans',
      { params: accountType ? { accountType } : undefined, signal },
    )
    return response.data.data ?? []
  },

  async listAdminPlans(signal?: AbortSignal) {
    const response = await apiClient.get<ApiResponse<SubscriptionPlan[]>>(
      '/api/admin/subscription-plans',
      { signal },
    )
    return response.data.data ?? []
  },

  async createPlan(payload: SubscriptionPlanRequest) {
    const response = await apiClient.post<ApiResponse<SubscriptionPlan>>(
      '/api/admin/subscription-plans',
      payload,
    )
    return response.data.data
  },

  async updatePlan(planId: number, payload: SubscriptionPlanRequest) {
    const response = await apiClient.put<ApiResponse<SubscriptionPlan>>(
      `/api/admin/subscription-plans/${planId}`,
      payload,
    )
    return response.data.data
  },

  async deletePlan(planId: number) {
    await apiClient.delete(`/api/admin/subscription-plans/${planId}`)
  },

  async getCompanySubscription(companyId: string, signal?: AbortSignal) {
    const response = await apiClient.get<ApiResponse<CompanySubscription>>(
      `/api/companies/${companyId}/subscription`,
      { signal },
    )
    return response.data.data
  },

  async selectCompanyPlan(companyId: string, planCode: string) {
    const response = await apiClient.post<ApiResponse<CompanySubscription>>(
      `/api/companies/${companyId}/subscription/select`,
      { planCode },
    )
    return response.data.data
  },
}

export default subscriptionService
