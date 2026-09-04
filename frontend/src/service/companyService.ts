import apiClient from './apiClient'
import type { ApiResponse } from './authService'

export interface CompanyCreateRequest {
  name: string
  displayName: string
  legalName: string
  website: string
  businessEmail: string
  businessPhone: string
  industry: string
  registrationNumber: string
  taxId: string
  addressLine1: string
  addressLine2: string
  city: string
  state: string
  postalCode: string
  country: string
}

export interface CompanyResponse {
  id: number
  name: string
  displayName?: string
  status: string
  role: string
}

export const companyService = {
  async createCompany(payload: CompanyCreateRequest) {
    const response = await apiClient.post<ApiResponse<CompanyResponse>>('/api/companies', payload)
    return response.data
  },
}

export default companyService
