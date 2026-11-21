import { zodResolver } from '@hookform/resolvers/zod'
import { useFieldArray, useForm } from 'react-hook-form'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { z } from 'zod'
import { ErrorBlock, LoadingBlock } from '../components/common/Feedback'
import { createBooking } from '../services/api/bookingApi'
import { getFlightDetails } from '../services/api/flightApi'
import { getApiErrorMessage } from '../services/api/client'
import type { CabinClass, FlightResponse } from '../types/domain'
import { formatCurrency, formatDateTime } from '../utils/format'
import { useEffect, useState } from 'react'

const passengerSchema = z.object({
  fullName: z.string().min(1, 'Full name is required').max(120),
  birthDate: z.string().min(1, 'Birth date is required'),
})

const bookingSchema = z.object({
  passengers: z.array(passengerSchema).min(1),
})

type BookingForm = z.infer<typeof bookingSchema>

export function BookingFormPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [flight, setFlight] = useState<FlightResponse | null>(null)
  const [loadingFlight, setLoadingFlight] = useState(false)
  const [error, setError] = useState('')
  const [submitError, setSubmitError] = useState('')

  const flightId = Number(searchParams.get('flightId') ?? 0)
  const hasValidFlightId = flightId > 0
  const cabinClass = (searchParams.get('cabinClass') ?? 'ECONOMY') as CabinClass

  const {
    control,
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<BookingForm>({
    resolver: zodResolver(bookingSchema),
    defaultValues: {
      passengers: [{ fullName: '', birthDate: '' }],
    },
  })

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'passengers',
  })

  useEffect(() => {
    if (!hasValidFlightId) {
      return
    }

    async function loadFlight() {
      setLoadingFlight(true)
      setError('')
      try {
        const response = await getFlightDetails(flightId)
        setFlight(response)
      } catch (requestError) {
        setError(getApiErrorMessage(requestError))
      } finally {
        setLoadingFlight(false)
      }
    }

    void loadFlight()
  }, [flightId, hasValidFlightId])

  if (!hasValidFlightId) {
    return <ErrorBlock message="Missing flight selection. Go back and choose a flight." />
  }

  async function onSubmit(values: BookingForm) {
    setSubmitError('')

    try {
      const booking = await createBooking({
        flightId,
        cabinClass,
        passengers: values.passengers,
      })
      navigate(`/bookings/${booking.pnr}`)
    } catch (requestError) {
      setSubmitError(getApiErrorMessage(requestError))
    }
  }

  if (loadingFlight) {
    return <LoadingBlock label="Loading selected flight..." />
  }

  if (error) {
    return <ErrorBlock message={error} />
  }

  return (
    <section className="space-y-6">
      <h1 className="text-2xl font-black text-slate-900">Passenger and Booking Details</h1>

      {flight ? (
        <article className="rounded-2xl border border-sky-100 bg-white p-5 shadow-sm">
          <h2 className="text-lg font-bold text-slate-900">{flight.airlineName} {flight.flightNumber}</h2>
          <p className="text-sm text-slate-600">
            {flight.origin.code} to {flight.destination.code} | {formatDateTime(flight.departureTime)}
          </p>
          <p className="mt-2 text-sm text-slate-700">Cabin: {cabinClass}</p>
          <p className="text-sm font-semibold text-sky-800">Current fare: {formatCurrency(flight.fare ?? undefined)}</p>
        </article>
      ) : null}

      <form className="space-y-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm" onSubmit={handleSubmit(onSubmit)}>
        {submitError ? <ErrorBlock message={submitError} /> : null}

        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold text-slate-900">Passengers</h2>
          <button
            type="button"
            onClick={() => append({ fullName: '', birthDate: '' })}
            className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-semibold hover:bg-slate-50"
          >
            Add Passenger
          </button>
        </div>

        {fields.map((field, index) => (
          <div key={field.id} className="grid gap-3 rounded-xl border border-slate-200 p-4 md:grid-cols-2">
            <label className="space-y-1">
              <span className="text-sm font-semibold">Full Name</span>
              <input className="input" {...register(`passengers.${index}.fullName`)} />
              {errors.passengers?.[index]?.fullName ? (
                <p className="error">{errors.passengers[index]?.fullName?.message}</p>
              ) : null}
            </label>

            <label className="space-y-1">
              <span className="text-sm font-semibold">Birth Date</span>
              <input type="date" className="input" {...register(`passengers.${index}.birthDate`)} />
              {errors.passengers?.[index]?.birthDate ? (
                <p className="error">{errors.passengers[index]?.birthDate?.message}</p>
              ) : null}
            </label>

            {fields.length > 1 ? (
              <div className="md:col-span-2">
                <button type="button" onClick={() => remove(index)} className="text-sm font-semibold text-rose-700 hover:text-rose-500">
                  Remove passenger
                </button>
              </div>
            ) : null}
          </div>
        ))}

        <button
          type="submit"
          disabled={isSubmitting}
          className="rounded-xl bg-sky-700 px-4 py-2.5 font-semibold text-white hover:bg-sky-600 disabled:opacity-70"
        >
          {isSubmitting ? 'Confirming booking...' : 'Confirm Booking'}
        </button>
      </form>
    </section>
  )
}
