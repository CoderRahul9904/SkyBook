import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { EmptyBlock, ErrorBlock, LoadingBlock } from '../components/common/Feedback'
import { cancelBooking, getBookingByPnr } from '../services/api/bookingApi'
import { getApiErrorMessage } from '../services/api/client'
import type { BookingResponse } from '../types/domain'
import { formatCurrency, formatDate, formatDateTime } from '../utils/format'

export function BookingDetailPage() {
  const { pnr = '' } = useParams()
  const normalizedPnr = pnr.trim()
  const [booking, setBooking] = useState<BookingResponse | null>(null)
  const [loading, setLoading] = useState(Boolean(normalizedPnr))
  const [error, setError] = useState('')

  useEffect(() => {
    if (!normalizedPnr) {
      return
    }

    async function loadBooking() {
      setLoading(true)
      setError('')

      try {
        const response = await getBookingByPnr(normalizedPnr)
        setBooking(response)
      } catch (requestError) {
        setBooking(null)
        setError(getApiErrorMessage(requestError))
      } finally {
        setLoading(false)
      }
    }

    void loadBooking()
  }, [normalizedPnr])

  if (!normalizedPnr) {
    return <ErrorBlock message="PNR is required." />
  }

  async function onCancel() {
    if (!booking) {
      return
    }

    const confirmed = window.confirm(`Cancel booking ${booking.pnr}?`)
    if (!confirmed) {
      return
    }

    try {
      const cancelled = await cancelBooking(booking.pnr)
      setBooking(cancelled)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  if (loading) {
    return <LoadingBlock label="Loading booking details..." />
  }

  if (error) {
    return <ErrorBlock message={error} />
  }

  if (!booking) {
    return <EmptyBlock title="Booking not found" subtitle="Check the PNR and try again." />
  }

  return (
    <section className="space-y-5">
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h1 className="text-2xl font-black text-slate-900">Booking {booking.pnr}</h1>
            <p className="mt-1 text-sm text-slate-600">
              {booking.flight.airlineName} {booking.flight.flightNumber} | {booking.flight.originCode} to {booking.flight.destinationCode}
            </p>
          </div>
          <span className={`rounded-full px-3 py-1 text-xs font-semibold ${booking.status === 'CANCELLED' ? 'bg-rose-100 text-rose-700' : 'bg-emerald-100 text-emerald-700'}`}>
            {booking.status}
          </span>
        </div>

        <dl className="mt-4 grid gap-3 text-sm text-slate-700 md:grid-cols-2">
          <Detail label="Departure" value={formatDateTime(booking.flight.departureTime)} />
          <Detail label="Arrival" value={formatDateTime(booking.flight.arrivalTime)} />
          <Detail label="Cabin" value={booking.cabinClass} />
          <Detail label="Passengers" value={String(booking.passengerCount)} />
          <Detail label="Fare Snapshot" value={formatCurrency(booking.fareSnapshot)} />
          <Detail label="Created" value={formatDateTime(booking.createdAt)} />
        </dl>

        <div className="mt-4 flex gap-2">
          <Link className="rounded-lg border border-slate-300 px-3 py-2 text-sm font-semibold hover:bg-slate-50" to="/bookings/my">
            Back to My Bookings
          </Link>
          {booking.status === 'CONFIRMED' ? (
            <button
              type="button"
              onClick={onCancel}
              className="rounded-lg border border-rose-300 px-3 py-2 text-sm font-semibold text-rose-700 hover:bg-rose-50"
            >
              Cancel Booking
            </button>
          ) : null}
        </div>
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="text-lg font-bold text-slate-900">Passengers</h2>
        <ul className="mt-3 grid gap-2">
          {booking.passengers.map((passenger, index) => (
            <li key={`${passenger.fullName}-${index}`} className="rounded-lg border border-slate-100 bg-slate-50 p-3 text-sm">
              <p className="font-semibold text-slate-800">{passenger.fullName}</p>
              <p className="text-slate-600">DOB: {formatDate(passenger.birthDate)}</p>
            </li>
          ))}
        </ul>
      </div>
    </section>
  )
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="font-semibold text-slate-500">{label}</dt>
      <dd className="text-slate-900">{value}</dd>
    </div>
  )
}
