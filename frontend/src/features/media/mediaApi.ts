import axios from 'axios'
import apiClient from '../../lib/api/apiClient'
import type { ApiResponse } from '../../lib/api/types'
import type { InitiateMediaUploadResponse, MediaPurpose, MediaResponse } from './types'

const unwrap = <T>(response: ApiResponse<T>) => {
  if (!response.data) {
    throw new Error(response.error_message ?? 'Empty API response')
  }
  return response.data
}

export const mediaApi = {
  async uploadProfilePicture(file: File) {
    const initiate = await apiClient.post<ApiResponse<InitiateMediaUploadResponse>>(
      '/api/v1/media/uploads',
      {
        fileName: file.name,
        contentType: file.type,
        sizeBytes: file.size,
        purpose: 'USER_PROFILE' satisfies MediaPurpose,
      },
    )
    const upload = unwrap(initiate.data)

    await axios.put(upload.uploadUrl, file, {
      headers: upload.requiredHeaders,
    })

    const complete = await apiClient.post<ApiResponse<MediaResponse>>(
      `/api/v1/media/${upload.mediaId}/complete`,
    )
    const completed = unwrap(complete.data)
    await apiClient.put<ApiResponse<{ userId: string; mediaId: string }>>(
      `/api/v1/users/me/profile-picture/${completed.mediaId}`,
    )
    return completed
  },
}
