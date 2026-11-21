import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { EmptyBlock, ErrorBlock, LoadingBlock } from '../components/common/Feedback'
import { cancelBooking, getMyBookings } from '../services/api/bookingApi'
import { getApiErrorMessage } from '../services/api/client'
import type { BookingResponse } from '../types/domain'
import { formatCurrency, formatDateTime } from '../utils/format'

export function MyBookingsPage() {
  const [bookings, setBookings] = useState<BookingResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [pnrInput, setPnrInput] = useState('')

  const bookingByPnrPath = useMemo(() => `/bookings/${pnrInput.trim()}`, [pnrInput])

  const refreshBookings = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setBookings(await getMyBookings())
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void refreshBookings()
  }, [refreshBookings])

  async function onCancel(pnr: string) {
    const confirmed = window.confirm(`Cancel booking ${pnr}? This action cannot be reversed.`)
    if (!confirmed) {
      return
    }

    try {
      await cancelBooking(pnr)
      await refreshBookings()
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  return (
    <section className="space-y-6">
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h1 className="text-2xl font-black text-slate-900">My Bookings</h1>
        <p className="mt-1 text-sm text-slate-600">View booking history, open details by PNR, and cancel eligible bookings.</p>

        <div className="mt-4 flex flex-wrap gap-2">
          <input
            className="input w-64"
            placeholder="Enter PNR"
            value={pnrInput}
            onChange={(event) => setPnrInput(event.target.value.toUpperCase())}
          />
          <Link
            className={`rounded-lg px-4 py-2 text-sm font-semibold ${pnrInput.trim() ? 'bg-slate-900 text-white hover:bg-slate-700' : 'cursor-not-allowed bg-slate-300 text-slate-500'}`}
            to={pnrInput.trim() ? bookingByPnrPath : '#'}
            onClick={(event) => {
              if (!pnrInput.trim()) {
                event.preventDefault()
              }
            }}
          >
            Lookup PNR
          </Link>
        </div>
      </div>

      {error ? <ErrorBlock message={error} /> : null}
      {loading ? <LoadingBlock label="Loading bookings..." /> : null}

      {!loading && bookings.length === 0 ? <EmptyBlock title="No bookings yet" subtitle="Book a flight to see it here." /> : null}

      {!loading && bookings.length > 0 ? (
        <div className="grid gap-4">
          {bookings.map((booking) => (
            <article key={booking.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <h2 className="text-lg font-bold text-slate-900">PNR {booking.pnr}</h2>
                  <p className="text-sm text-slate-600">
                    {booking.flight.airlineName} {booking.flight.flightNumber} | {booking.flight.originCode} to {booking.flight.destinationCode}
                  </p>
                  <p className="text-sm text-slate-500">Departure: {formatDateTime(booking.flight.departureTime)}</p>
                </div>

                <div className="text-right">
                  <p className="text-sm text-slate-600">Status</p>
                  <p className={`text-sm font-bold ${booking.status === 'CANCELLED' ? 'text-rose-700' : 'text-emerald-700'}`}>{booking.status}</p>
                  <p className="text-sm text-slate-600">{formatCurrency(booking.fareSnapshot)}</p>
                </div>
              </div>

              <div className="mt-4 flex flex-wrap gap-2">
                <Link className="rounded-lg border border-sky-200 px-3 py-2 text-sm font-semibold text-sky-800 hover:bg-sky-50" to={`/bookings/${booking.pnr}`}>
                  View details
                </Link>
                {booking.status === 'CONFIRMED' ? (
                  <button
                    type="button"
                    onClick={() => onCancel(booking.pnr)}
                    className="rounded-lg border border-rose-300 px-3 py-2 text-sm font-semibold text-rose-700 hover:bg-rose-50"
                  >
                    Cancel booking
                  </button>
                ) : null}
              </div>
            </article>
          ))}
        </div>
      ) : null}
    </section>
  )
}
