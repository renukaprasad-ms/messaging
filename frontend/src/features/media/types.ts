export type MediaPurpose = 'USER_PROFILE'

export type InitiateMediaUploadResponse = {
  mediaId: string
  uploadUrl: string
  expiresAt: string
  requiredHeaders: Record<string, string>
}

export type MediaResponse = {
  mediaId: string
  status: 'PENDING' | 'ACTIVE' | 'FAILED' | 'DELETED'
}
