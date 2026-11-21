export type UserRole = 'CUSTOMER' | 'ADMIN'

export type CabinClass = 'ECONOMY' | 'BUSINESS'
export type FlightStatus = 'SCHEDULED' | 'UPDATED' | 'CANCELLED'
export type BookingStatus = 'CONFIRMED' | 'CANCELLED'

export interface UserResponse {
  id: number
  fullName: string
  email: string
  role: UserRole
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
  user: UserResponse
}

export interface AirportResponse {
  id: number
  code: string
  name: string
  city: string
  country: string
}

export interface CabinAvailabilityResponse {
  cabinClass: CabinClass
  fare: number
  totalSeats: number
  availableSeats: number
}

export interface FlightResponse {
  id: number
  airlineName: string
  airlineCode: string
  flightNumber: string
  origin: AirportResponse
  destination: AirportResponse
  departureTime: string
  arrivalTime: string
  durationMinutes: number
  stops: number
  cabinClass: CabinClass | null
  fare: number | null
  availableSeats: number
  status: FlightStatus
  cabins: CabinAvailabilityResponse[]
}

export interface BookingFlightResponse {
  id: number
  airlineName: string
  airlineCode: string
  flightNumber: string
  originCode: string
  destinationCode: string
  departureTime: string
  arrivalTime: string
}

export interface PassengerRequest {
  fullName: string
  birthDate: string
}

export interface PassengerResponse {
  fullName: string
  birthDate: string
}

export interface CreateBookingRequest {
  flightId: number
  cabinClass: CabinClass
  passengers: PassengerRequest[]
}

export interface BookingResponse {
  id: number
  pnr: string
  flight: BookingFlightResponse
  cabinClass: CabinClass
  passengerCount: number
  fareSnapshot: number
  status: BookingStatus
  createdAt: string
  updatedAt: string
  passengers: PassengerResponse[]
}

export interface AdminAirportResponse {
  id: number
  code: string
  name: string
  city: string
  country: string
  createdAt: string
  updatedAt: string
}

export interface CabinManagementResponse {
  cabinClass: CabinClass
  fare: number | null
  totalSeats: number | null
  availableSeats: number | null
}

export interface AdminFlightResponse {
  id: number
  airlineName: string
  airlineCode: string
  flightNumber: string
  origin: AdminAirportResponse
  destination: AdminAirportResponse
  departureTime: string
  arrivalTime: string
  status: FlightStatus
  cabins: CabinManagementResponse[]
}

export interface AiChatResponse {
  conversationId: string
  response: string
  ragApplied: boolean
}

export interface AiConversationSummaryResponse {
  conversationId: string
  createdAt: string
  updatedAt: string
}

export interface AiConversationMessageResponse {
  role: string
  content: string
}

export interface AiConversationDetailResponse {
  conversationId: string
  createdAt: string
  updatedAt: string
  messages: AiConversationMessageResponse[]
}
