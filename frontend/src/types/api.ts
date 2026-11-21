export interface ApiResponse<T> {
  timestamp: string
  status: number
  message: string
  data: T
}

export interface FieldErrorResponse {
  field: string
  message: string
}

export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors: FieldErrorResponse[]
}
