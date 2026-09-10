import apiClient from './apiClient'
import type { ApiResponse } from './authService'

export interface CompanyCreateRequest {
  name: string
  displayName: string
  logoUrl: string
  legalName: string
  website: string
  businessEmail: string
  businessPhone: string
  industry: string
  addressLine1: string
  addressLine2: string
  city: string
  state: string
  postalCode: string
  country: string
}

export interface CompanySummary {
  id: number
  name: string
  displayName?: string
  logoUrl?: string
  status: string
  role: string
}

export interface CompanyResponse extends CompanySummary {
  profile?: {
    legalName: string
    website?: string
    businessEmail?: string
    businessPhone?: string
    industry?: string
  }
  address?: {
    addressLine1: string
    addressLine2?: string
    city: string
    state?: string
    postalCode?: string
    country: string
  }
}

export const companyService = {
  async list(page = 0) {
    const response = await apiClient.get<
      ApiResponse<{ companies: CompanySummary[]; hasNext: boolean }>
    >('/api/companies', { params: { page } })
    return response.data.data ?? { companies: [], hasNext: false }
  },
  async get(id: string, signal?: AbortSignal) {
    const response = await apiClient.get<ApiResponse<CompanyResponse>>(`/api/companies/${id}`, {
      signal,
    })
    return response.data.data
  },
  async update(id: string, payload: { name: string; displayName: string }) {
    const response = await apiClient.patch<ApiResponse<CompanyResponse>>(
      `/api/companies/${id}`,
      payload,
    )
    return response.data.data
  },
  async createCompany(payload: CompanyCreateRequest) {
    const response = await apiClient.post<ApiResponse<CompanyResponse>>('/api/companies', payload)
    return response.data
  },
}

export default companyService
