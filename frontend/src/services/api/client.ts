import axios, { AxiosError } from 'axios'
import type { ApiErrorResponse, ApiResponse } from '../../types/api'
import { storage } from '../../utils/storage'

const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export const apiClient = axios.create({
  baseURL,
  timeout: 15000,
})

apiClient.interceptors.request.use((config) => {
  const token = storage.getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorResponse>) => {
    if (error.response?.status === 401) {
      storage.clearAuth()
    }
    return Promise.reject(error)
  },
)

export async function unwrapData<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise
  return response.data.data
}

export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const payload = error.response?.data
    if (payload?.fieldErrors?.length) {
      return payload.fieldErrors.map((fieldError) => `${fieldError.field}: ${fieldError.message}`).join(', ')
    }
    if (payload?.message) {
      return payload.message
    }
    if (error.response?.status === 401) {
      return 'Your session has expired. Please log in again.'
    }
    return error.message
  }

  return 'Something went wrong. Please try again.'
}
