import apiClient from './apiClient'
import type { ApiResponse } from './authService'

type MediaUploadPurpose = 'PROFILE_PHOTO' | 'COMPANY_LOGO' | 'MESSAGE_MEDIA' | 'TEMPLATE_MEDIA'

interface MediaUploadResponse {
  bucket: string
  path: string
  token: string
  signedUrl: string
  publicUrl: string
  expiresInSeconds: number
}

export async function uploadImage(purpose: Extract<MediaUploadPurpose, 'PROFILE_PHOTO' | 'COMPANY_LOGO'>, file?: File | null) {
  if (!file) return ''

  const response = await apiClient.post<ApiResponse<MediaUploadResponse>>(
    '/api/media/uploads/signed-url',
    {
      purpose,
      fileName: file.name,
      contentType: file.type || 'application/octet-stream',
      sizeBytes: file.size,
    },
  )
  const upload = response.data.data
  if (!upload) throw new Error('Unable to prepare media upload.')

  const uploadResponse = await fetch(upload.signedUrl, {
    method: 'PUT',
    headers: {
      'Content-Type': file.type,
      'Cache-Control': 'max-age=3600',
    },
    body: file,
  })

  if (!uploadResponse.ok) {
    const message = await uploadResponse.text()
    throw new Error(message || 'Unable to upload media.')
  }
  return upload.publicUrl
}
