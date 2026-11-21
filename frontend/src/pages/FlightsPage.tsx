import { zodResolver } from '@hookform/resolvers/zod'
import { Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { EmptyBlock, ErrorBlock, LoadingBlock } from '../components/common/Feedback'
import { searchFlights } from '../services/api/flightApi'
import { getApiErrorMessage } from '../services/api/client'
import type { CabinClass, FlightResponse } from '../types/domain'
import { formatCurrency, formatDateTime } from '../utils/format'

const searchSchema = z.object({
  origin: z.string().min(3).max(10),
  destination: z.string().min(3).max(10),
  travelDate: z.string().min(1, 'Travel date is required'),
  passengerCount: z.number().int().min(1).max(9),
  cabinClass: z.enum(['ECONOMY', 'BUSINESS']),
})

type SearchForm = z.infer<typeof searchSchema>

export function FlightsPage() {
  const [results, setResults] = useState<FlightResponse[]>([])
  const [error, setError] = useState<string>('')
  const [isLoading, setIsLoading] = useState(false)
  const [hasSearched, setHasSearched] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SearchForm>({
    resolver: zodResolver(searchSchema),
    defaultValues: {
      origin: 'DEL',
      destination: 'BOM',
      travelDate: new Date(Date.now() + 86400000).toISOString().slice(0, 10),
      passengerCount: 1,
      cabinClass: 'ECONOMY',
    },
  })

  useEffect(() => {
    void handleSubmit(onSubmit)()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function onSubmit(values: SearchForm) {
    setError('')
    setIsLoading(true)
    setHasSearched(true)

    try {
      const response = await searchFlights({
        origin: values.origin.trim().toUpperCase(),
        destination: values.destination.trim().toUpperCase(),
        travelDate: values.travelDate,
        passengerCount: values.passengerCount,
        cabinClass: values.cabinClass as CabinClass,
      })
      setResults(response)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
      setResults([])
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <section className="space-y-6">
      <div className="rounded-3xl border border-sky-100 bg-white p-6 shadow-sm">
        <h1 className="text-2xl font-black text-slate-900">Search Flights</h1>
        <form className="mt-4 grid gap-4 md:grid-cols-2 xl:grid-cols-6" onSubmit={handleSubmit(onSubmit)}>
          <Field label="Origin">
            <input className="input" {...register('origin')} />
            {errors.origin ? <p className="error">{errors.origin.message}</p> : null}
          </Field>

          <Field label="Destination">
            <input className="input" {...register('destination')} />
            {errors.destination ? <p className="error">{errors.destination.message}</p> : null}
          </Field>

          <Field label="Departure Date">
            <input type="date" className="input" {...register('travelDate')} />
            {errors.travelDate ? <p className="error">{errors.travelDate.message}</p> : null}
          </Field>

          <Field label="Passengers">
            <input type="number" min={1} max={9} className="input" {...register('passengerCount', { valueAsNumber: true })} />
            {errors.passengerCount ? <p className="error">{errors.passengerCount.message}</p> : null}
          </Field>

          <Field label="Cabin Class">
            <select className="input" {...register('cabinClass')}>
              <option value="ECONOMY">Economy</option>
              <option value="BUSINESS">Business</option>
            </select>
            {errors.cabinClass ? <p className="error">{errors.cabinClass.message}</p> : null}
          </Field>

          <div className="self-end">
            <button className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-sky-700 px-4 py-2.5 font-semibold text-white hover:bg-sky-600">
              <Search className="h-4 w-4" />
              Search
            </button>
          </div>
        </form>
      </div>

      {error ? <ErrorBlock message={error} /> : null}
      {isLoading ? <LoadingBlock label="Fetching available flights..." /> : null}

      {!isLoading && hasSearched && results.length === 0 && !error ? (
        <EmptyBlock title="No flights found" subtitle="Try changing route, date, or cabin class." />
      ) : null}

      {!isLoading && results.length > 0 ? (
        <div className="grid gap-4">
          {results.map((flight) => (
            <article key={`${flight.id}-${flight.cabinClass ?? 'ALL'}`} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <h2 className="text-lg font-bold text-slate-900">
                    {flight.airlineName} ({flight.airlineCode}) {flight.flightNumber}
                  </h2>
                  <p className="text-sm text-slate-600">
                    {flight.origin.code} to {flight.destination.code} | {formatDateTime(flight.departureTime)} {'->'} {formatDateTime(flight.arrivalTime)}
                  </p>
                  <p className="mt-1 text-sm text-slate-500">
                    Duration: {flight.durationMinutes} min | Stops: {flight.stops} | Status: {flight.status}
                  </p>
                </div>

                <div className="text-right">
                  <p className="text-sm text-slate-600">Fare</p>
                  <p className="text-xl font-black text-sky-800">{formatCurrency(flight.fare ?? undefined)}</p>
                  <p className="text-sm text-slate-500">Seats: {flight.availableSeats}</p>
                </div>
              </div>

              <div className="mt-4 flex flex-wrap gap-2">
                <Link className="rounded-lg border border-sky-200 px-3 py-2 text-sm font-semibold text-sky-800 hover:bg-sky-50" to={`/bookings/new?flightId=${flight.id}&cabinClass=${flight.cabinClass ?? 'ECONOMY'}`}>
                  Select Flight
                </Link>
              </div>
            </article>
          ))}
        </div>
      ) : null}
    </section>
  )
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="space-y-1">
      <span className="text-sm font-semibold text-slate-700">{label}</span>
      {children}
    </label>
  )
}
