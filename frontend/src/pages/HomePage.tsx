import { Link } from 'react-router-dom'
import { MessageCircle, PlaneTakeoff, ShieldCheck, Ticket } from 'lucide-react'
import type { ReactNode } from 'react'

export function HomePage() {
  return (
    <section className="space-y-8">
      <div className="overflow-hidden rounded-3xl border border-sky-100 bg-gradient-to-br from-sky-900 via-sky-700 to-cyan-600 p-8 text-white shadow-xl">
        <p className="text-xs uppercase tracking-[0.22em] text-cyan-200">AI-assisted airline booking</p>
        <h1 className="mt-3 max-w-2xl text-4xl font-black leading-tight md:text-5xl">Book flights with confidence, manage journeys in one place.</h1>
        <p className="mt-4 max-w-2xl text-sm text-cyan-100 md:text-base">
          SkyBook connects live flight search, secure booking, admin operations, and AeroMate AI assistance in one integrated platform.
        </p>
        <div className="mt-6 flex flex-wrap gap-3">
          <Link className="rounded-full bg-white px-6 py-3 text-sm font-bold text-sky-900 hover:bg-cyan-50" to="/flights">
            Search Flights
          </Link>
          <Link className="rounded-full border border-cyan-200 bg-transparent px-6 py-3 text-sm font-bold text-white hover:bg-white/10" to="/aeromate">
            Chat with AeroMate
          </Link>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <FeatureCard icon={<PlaneTakeoff className="h-5 w-5" />} title="Flight Search" text="Search direct flights by date, route, passenger count, and cabin class." />
        <FeatureCard icon={<Ticket className="h-5 w-5" />} title="Booking Journey" text="Select flights, add passengers, confirm bookings, and track PNR status." />
        <FeatureCard icon={<ShieldCheck className="h-5 w-5" />} title="Admin Console" text="Manage airports, flights, fares, inventory, and booking operations." />
        <FeatureCard icon={<MessageCircle className="h-5 w-5" />} title="AeroMate AI" text="Continue authenticated AI conversations with persistent chat memory." />
      </div>
    </section>
  )
}

function FeatureCard({ icon, title, text }: { icon: ReactNode; title: string; text: string }) {
  return (
    <article className="rounded-2xl border border-sky-100 bg-white p-5 shadow-sm">
      <div className="mb-3 inline-flex rounded-xl bg-sky-100 p-2 text-sky-700">{icon}</div>
      <h2 className="text-lg font-bold text-slate-900">{title}</h2>
      <p className="mt-1 text-sm text-slate-600">{text}</p>
    </article>
  )
}
