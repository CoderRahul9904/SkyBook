import { useEffect, useMemo, useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import {
  cancelAdminBooking,
  createAirport,
  createFlight,
  getAdminBookings,
  getAirports,
  getCabins,
  getFlights,
  updateAirport,
  updateFlight,
  upsertFare,
  upsertInventory,
} from '../services/api/adminApi'
import type {
  AdminAirportResponse,
  AdminFlightResponse,
  BookingResponse,
  CabinClass,
  CabinManagementResponse,
  FlightStatus,
} from '../types/domain'
import { formatCurrency, formatDateTime } from '../utils/format'
import { ErrorBlock, LoadingBlock } from '../components/common/Feedback'
import { getApiErrorMessage } from '../services/api/client'

const CABIN_CLASSES: CabinClass[] = ['ECONOMY', 'BUSINESS']
const FLIGHT_STATUSES: FlightStatus[] = ['SCHEDULED', 'UPDATED', 'CANCELLED']

export function AdminPage() {
  const [airports, setAirports] = useState<AdminAirportResponse[]>([])
  const [flights, setFlights] = useState<AdminFlightResponse[]>([])
  const [bookings, setBookings] = useState<BookingResponse[]>([])
  const [selectedFlightId, setSelectedFlightId] = useState<number | null>(null)
  const [cabinDetails, setCabinDetails] = useState<CabinManagementResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [airportForm, setAirportForm] = useState({ code: '', name: '', city: '', country: '', updateId: '' })
  const [flightForm, setFlightForm] = useState({
    updateId: '',
    airlineName: '',
    airlineCode: '',
    flightNumber: '',
    originAirportId: '',
    destinationAirportId: '',
    departureTime: '',
    arrivalTime: '',
    status: 'SCHEDULED' as FlightStatus,
  })
  const [fareForm, setFareForm] = useState({ flightId: '', cabinClass: 'ECONOMY' as CabinClass, amount: '' })
  const [inventoryForm, setInventoryForm] = useState({
    flightId: '',
    cabinClass: 'ECONOMY' as CabinClass,
    totalSeats: '',
    availableSeats: '',
  })

  const selectedFlight = useMemo(
    () => flights.find((flight) => flight.id === selectedFlightId) ?? null,
    [flights, selectedFlightId],
  )

  useEffect(() => {
    void refreshAll()
  }, [])

  useEffect(() => {
    if (!selectedFlightId) {
      setCabinDetails([])
      return
    }

    void getCabins(selectedFlightId)
      .then((response) => setCabinDetails(response))
      .catch((requestError) => setError(getApiErrorMessage(requestError)))
  }, [selectedFlightId])

  async function refreshAll() {
    setLoading(true)
    setError('')

    try {
      const [airportsData, flightsData, bookingsData] = await Promise.all([getAirports(), getFlights(), getAdminBookings()])
      setAirports(airportsData)
      setFlights(flightsData)
      setBookings(bookingsData)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }

  async function handleAirportSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    try {
      if (airportForm.updateId) {
        await updateAirport(Number(airportForm.updateId), {
          code: airportForm.code,
          name: airportForm.name,
          city: airportForm.city,
          country: airportForm.country,
        })
      } else {
        await createAirport({
          code: airportForm.code,
          name: airportForm.name,
          city: airportForm.city,
          country: airportForm.country,
        })
      }
      setAirportForm({ code: '', name: '', city: '', country: '', updateId: '' })
      await refreshAll()
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  async function handleFlightSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    try {
      const payload = {
        airlineName: flightForm.airlineName,
        airlineCode: flightForm.airlineCode,
        flightNumber: flightForm.flightNumber,
        originAirportId: Number(flightForm.originAirportId),
        destinationAirportId: Number(flightForm.destinationAirportId),
        departureTime: new Date(flightForm.departureTime).toISOString(),
        arrivalTime: new Date(flightForm.arrivalTime).toISOString(),
        status: flightForm.status,
      }

      if (flightForm.updateId) {
        await updateFlight(Number(flightForm.updateId), payload)
      } else {
        await createFlight(payload)
      }

      setFlightForm({
        updateId: '',
        airlineName: '',
        airlineCode: '',
        flightNumber: '',
        originAirportId: '',
        destinationAirportId: '',
        departureTime: '',
        arrivalTime: '',
        status: 'SCHEDULED',
      })
      await refreshAll()
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  async function handleFareSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    try {
      await upsertFare(Number(fareForm.flightId), fareForm.cabinClass, Number(fareForm.amount))
      await refreshAll()
      setSelectedFlightId(Number(fareForm.flightId))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  async function handleInventorySubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    try {
      await upsertInventory(Number(inventoryForm.flightId), inventoryForm.cabinClass, {
        totalSeats: Number(inventoryForm.totalSeats),
        availableSeats: Number(inventoryForm.availableSeats),
      })
      await refreshAll()
      setSelectedFlightId(Number(inventoryForm.flightId))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  async function onAdminCancelBooking(pnr: string) {
    const confirmed = window.confirm(`Cancel booking ${pnr} as admin?`)
    if (!confirmed) {
      return
    }

    try {
      await cancelAdminBooking(pnr)
      await refreshAll()
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  return (
    <section className="space-y-6">
      <h1 className="text-2xl font-black text-slate-900">Admin Dashboard</h1>
      {error ? <ErrorBlock message={error} /> : null}
      {loading ? <LoadingBlock label="Loading admin data..." /> : null}

      {!loading ? (
        <>
          <div className="grid gap-6 xl:grid-cols-2">
            <FormCard title="Airport Management" subtitle="Create or update airports">
              <form className="grid gap-3" onSubmit={handleAirportSubmit}>
                <label className="space-y-1">
                  <span className="text-xs font-semibold text-slate-600">Update Airport ID (optional)</span>
                  <input className="input" value={airportForm.updateId} onChange={(event) => setAirportForm((prev) => ({ ...prev, updateId: event.target.value }))} />
                </label>
                <div className="grid gap-3 md:grid-cols-2">
                  <input className="input" placeholder="Code" value={airportForm.code} onChange={(event) => setAirportForm((prev) => ({ ...prev, code: event.target.value.toUpperCase() }))} required />
                  <input className="input" placeholder="Name" value={airportForm.name} onChange={(event) => setAirportForm((prev) => ({ ...prev, name: event.target.value }))} required />
                  <input className="input" placeholder="City" value={airportForm.city} onChange={(event) => setAirportForm((prev) => ({ ...prev, city: event.target.value }))} required />
                  <input className="input" placeholder="Country" value={airportForm.country} onChange={(event) => setAirportForm((prev) => ({ ...prev, country: event.target.value }))} required />
                </div>
                <button className="rounded-lg bg-sky-700 px-4 py-2 font-semibold text-white hover:bg-sky-600">Save Airport</button>
              </form>

              <ul className="mt-4 grid gap-2 text-sm">
                {airports.map((airport) => (
                  <li key={airport.id} className="rounded-lg border border-slate-100 bg-slate-50 p-2">
                    #{airport.id} {airport.code} - {airport.city}, {airport.country}
                  </li>
                ))}
              </ul>
            </FormCard>

            <FormCard title="Flight Management" subtitle="Create or update flights">
              <form className="grid gap-3" onSubmit={handleFlightSubmit}>
                <label className="space-y-1">
                  <span className="text-xs font-semibold text-slate-600">Update Flight ID (optional)</span>
                  <input className="input" value={flightForm.updateId} onChange={(event) => setFlightForm((prev) => ({ ...prev, updateId: event.target.value }))} />
                </label>

                <div className="grid gap-3 md:grid-cols-2">
                  <input className="input" placeholder="Airline Name" value={flightForm.airlineName} onChange={(event) => setFlightForm((prev) => ({ ...prev, airlineName: event.target.value }))} required />
                  <input className="input" placeholder="Airline Code" value={flightForm.airlineCode} onChange={(event) => setFlightForm((prev) => ({ ...prev, airlineCode: event.target.value.toUpperCase() }))} required />
                  <input className="input" placeholder="Flight Number" value={flightForm.flightNumber} onChange={(event) => setFlightForm((prev) => ({ ...prev, flightNumber: event.target.value.toUpperCase() }))} required />

                  <select className="input" value={flightForm.originAirportId} onChange={(event) => setFlightForm((prev) => ({ ...prev, originAirportId: event.target.value }))} required>
                    <option value="">Origin Airport</option>
                    {airports.map((airport) => (
                      <option key={airport.id} value={airport.id}>
                        {airport.code}
                      </option>
                    ))}
                  </select>

                  <select className="input" value={flightForm.destinationAirportId} onChange={(event) => setFlightForm((prev) => ({ ...prev, destinationAirportId: event.target.value }))} required>
                    <option value="">Destination Airport</option>
                    {airports.map((airport) => (
                      <option key={airport.id} value={airport.id}>
                        {airport.code}
                      </option>
                    ))}
                  </select>

                  <select className="input" value={flightForm.status} onChange={(event) => setFlightForm((prev) => ({ ...prev, status: event.target.value as FlightStatus }))}>
                    {FLIGHT_STATUSES.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>

                  <input type="datetime-local" className="input" value={flightForm.departureTime} onChange={(event) => setFlightForm((prev) => ({ ...prev, departureTime: event.target.value }))} required />
                  <input type="datetime-local" className="input" value={flightForm.arrivalTime} onChange={(event) => setFlightForm((prev) => ({ ...prev, arrivalTime: event.target.value }))} required />
                </div>

                <button className="rounded-lg bg-sky-700 px-4 py-2 font-semibold text-white hover:bg-sky-600">Save Flight</button>
              </form>
            </FormCard>
          </div>

          <div className="grid gap-6 xl:grid-cols-2">
            <FormCard title="Fare Management" subtitle="Update cabin fare by flight">
              <form className="grid gap-3 md:grid-cols-3" onSubmit={handleFareSubmit}>
                <input className="input" type="number" placeholder="Flight ID" value={fareForm.flightId} onChange={(event) => setFareForm((prev) => ({ ...prev, flightId: event.target.value }))} required />
                <select className="input" value={fareForm.cabinClass} onChange={(event) => setFareForm((prev) => ({ ...prev, cabinClass: event.target.value as CabinClass }))}>
                  {CABIN_CLASSES.map((cabin) => (
                    <option key={cabin} value={cabin}>
                      {cabin}
                    </option>
                  ))}
                </select>
                <input className="input" type="number" min="0.01" step="0.01" placeholder="Amount" value={fareForm.amount} onChange={(event) => setFareForm((prev) => ({ ...prev, amount: event.target.value }))} required />
                <button className="rounded-lg bg-slate-900 px-4 py-2 font-semibold text-white hover:bg-slate-700 md:col-span-3">Update Fare</button>
              </form>
            </FormCard>

            <FormCard title="Inventory Management" subtitle="Update cabin inventory by flight">
              <form className="grid gap-3 md:grid-cols-4" onSubmit={handleInventorySubmit}>
                <input className="input" type="number" placeholder="Flight ID" value={inventoryForm.flightId} onChange={(event) => setInventoryForm((prev) => ({ ...prev, flightId: event.target.value }))} required />
                <select className="input" value={inventoryForm.cabinClass} onChange={(event) => setInventoryForm((prev) => ({ ...prev, cabinClass: event.target.value as CabinClass }))}>
                  {CABIN_CLASSES.map((cabin) => (
                    <option key={cabin} value={cabin}>
                      {cabin}
                    </option>
                  ))}
                </select>
                <input className="input" type="number" min="0" placeholder="Total Seats" value={inventoryForm.totalSeats} onChange={(event) => setInventoryForm((prev) => ({ ...prev, totalSeats: event.target.value }))} required />
                <input className="input" type="number" min="0" placeholder="Available Seats" value={inventoryForm.availableSeats} onChange={(event) => setInventoryForm((prev) => ({ ...prev, availableSeats: event.target.value }))} required />
                <button className="rounded-lg bg-slate-900 px-4 py-2 font-semibold text-white hover:bg-slate-700 md:col-span-4">Update Inventory</button>
              </form>
            </FormCard>
          </div>

          <div className="grid gap-6 xl:grid-cols-2">
            <FormCard title="Flight List" subtitle="Select a flight to inspect cabin data">
              <ul className="grid gap-2 text-sm">
                {flights.map((flight) => (
                  <li key={flight.id} className="rounded-lg border border-slate-100 bg-slate-50 p-3">
                    <div className="flex items-center justify-between gap-2">
                      <div>
                        <p className="font-semibold text-slate-900">#{flight.id} {flight.airlineCode} {flight.flightNumber}</p>
                        <p className="text-slate-600">{flight.origin.code} {'->'} {flight.destination.code}</p>
                        <p className="text-slate-500">{formatDateTime(flight.departureTime)}</p>
                      </div>
                      <button
                        type="button"
                        onClick={() => setSelectedFlightId(flight.id)}
                        className="rounded-lg border border-sky-200 px-3 py-1.5 text-xs font-semibold text-sky-700 hover:bg-sky-50"
                      >
                        View cabins
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            </FormCard>

            <FormCard title="Cabin Snapshot" subtitle={selectedFlight ? `Flight #${selectedFlight.id}` : 'Select a flight'}>
              {selectedFlight ? (
                <ul className="grid gap-2 text-sm">
                  {cabinDetails.length ? (
                    cabinDetails.map((cabin) => (
                      <li key={cabin.cabinClass} className="rounded-lg border border-slate-100 bg-slate-50 p-3">
                        <p className="font-semibold text-slate-900">{cabin.cabinClass}</p>
                        <p className="text-slate-600">Fare: {formatCurrency(cabin.fare ?? undefined)}</p>
                        <p className="text-slate-600">Total: {cabin.totalSeats ?? 'N/A'} | Available: {cabin.availableSeats ?? 'N/A'}</p>
                      </li>
                    ))
                  ) : (
                    <p className="text-sm text-slate-500">No cabin data configured yet.</p>
                  )}
                </ul>
              ) : (
                <p className="text-sm text-slate-500">Choose a flight from the list to see cabin details.</p>
              )}
            </FormCard>
          </div>

          <FormCard title="Booking Management" subtitle="Review and cancel bookings">
            <div className="grid gap-2">
              {bookings.map((booking) => (
                <article key={booking.id} className="rounded-lg border border-slate-100 bg-slate-50 p-3 text-sm">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <p className="font-semibold text-slate-900">PNR {booking.pnr}</p>
                      <p className="text-slate-600">{booking.flight.originCode} {'->'} {booking.flight.destinationCode} | {booking.status}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-slate-700">{formatCurrency(booking.fareSnapshot)}</span>
                      {booking.status === 'CONFIRMED' ? (
                        <button
                          type="button"
                          className="rounded-lg border border-rose-300 px-3 py-1.5 text-xs font-semibold text-rose-700 hover:bg-rose-50"
                          onClick={() => onAdminCancelBooking(booking.pnr)}
                        >
                          Cancel
                        </button>
                      ) : null}
                    </div>
                  </div>
                </article>
              ))}
            </div>
          </FormCard>
        </>
      ) : null}
    </section>
  )
}

function FormCard({ title, subtitle, children }: { title: string; subtitle: string; children: ReactNode }) {
  return (
    <article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="text-lg font-bold text-slate-900">{title}</h2>
      <p className="mb-4 text-sm text-slate-600">{subtitle}</p>
      {children}
    </article>
  )
}
