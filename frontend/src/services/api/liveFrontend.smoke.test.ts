import { afterAll, describe, expect, test } from 'vitest'
import { login, register } from './authApi'
import { sendChat } from './aiApi'
import { createBooking, getBookingByPnr } from './bookingApi'
import { getAdminBookings, getFlights } from './adminApi'
import { searchFlights } from './flightApi'
import { storage } from '../../utils/storage'

function createLocalStorageMock() {
  const store = new Map<string, string>()
  return {
    getItem: (key: string) => store.get(key) ?? null,
    setItem: (key: string, value: string) => {
      store.set(key, String(value))
    },
    removeItem: (key: string) => {
      store.delete(key)
    },
    clear: () => {
      store.clear()
    },
  }
}

if (!('localStorage' in globalThis)) {
  Object.defineProperty(globalThis, 'localStorage', {
    value: createLocalStorageMock(),
    configurable: true,
  })
}

describe('frontend live smoke via service layer', () => {
  afterAll(() => {
    storage.clearAuth()
  })

  test('customer, ai, and admin flows work against live backend', async () => {
    const email = `frontend.live.${Date.now()}@example.com`
    const password = 'Test@1234'

    const registered = await register({
      fullName: 'Frontend Live User',
      email,
      password,
    })
    expect(registered.user.email).toBe(email)

    const customerAuth = await login({ email, password })
    storage.setToken(customerAuth.accessToken)

    const date = new Date(Date.now() + 86400000).toISOString().slice(0, 10)
    const flights = await searchFlights({
      origin: 'DEL',
      destination: 'BOM',
      travelDate: date,
      passengerCount: 1,
      cabinClass: 'ECONOMY',
    })
    expect(flights.length).toBeGreaterThan(0)

    const booking = await createBooking({
      flightId: flights[0].id,
      cabinClass: 'ECONOMY',
      passengers: [{ fullName: 'Passenger One', birthDate: '1995-01-15' }],
    })
    expect(booking.pnr).toBeTruthy()

    const byPnr = await getBookingByPnr(booking.pnr)
    expect(byPnr.pnr).toBe(booking.pnr)

    const chat = await sendChat({ message: 'Hello from frontend smoke test.' })
    expect(chat.response).toBeTruthy()

    const adminEmail = process.env.SKYBOOK_ADMIN_EMAIL ?? 'mouryarahul543@gmail.com'
    const adminPassword = process.env.SKYBOOK_ADMIN_PASSWORD ?? 'Rahul@1234'

    const adminAuth = await login({
      email: adminEmail,
      password: adminPassword,
    })
    storage.setToken(adminAuth.accessToken)

    const adminFlights = await getFlights()
    expect(adminFlights.length).toBeGreaterThan(0)

    const adminBookings = await getAdminBookings()
    expect(adminBookings.length).toBeGreaterThan(0)
  }, 120000)
})
