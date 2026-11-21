import { apiClient, unwrapData } from './client'
import type { AuthResponse } from '../../types/domain'

export interface RegisterPayload {
  fullName: string
  email: string
  password: string
}

export interface LoginPayload {
  email: string
  password: string
}

export function register(payload: RegisterPayload): Promise<AuthResponse> {
  return unwrapData(apiClient.post('/api/v1/auth/register', payload))
}

export function login(payload: LoginPayload): Promise<AuthResponse> {
  return unwrapData(apiClient.post('/api/v1/auth/login', payload))
}
