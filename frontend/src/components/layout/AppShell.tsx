import { Link, NavLink, Outlet } from 'react-router-dom'
import { Plane, ShieldCheck, Ticket, MessageCircle, LogOut } from 'lucide-react'
import { useAuth } from '../../app/AuthContext'
import { classNames } from '../../utils/format'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  classNames(
    'rounded-full px-4 py-2 text-sm font-semibold transition-colors',
    isActive ? 'bg-sky-700 text-white' : 'text-slate-700 hover:bg-sky-100',
  )

export function AppShell() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth()

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top,_#e0f2fe_0%,_#f8fafc_40%,_#f1f5f9_100%)] text-slate-900">
      <header className="sticky top-0 z-20 border-b border-sky-100/70 bg-white/90 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3">
          <Link to="/" className="flex items-center gap-2 text-lg font-extrabold tracking-tight text-sky-900">
            <Plane className="h-5 w-5" />
            SkyBook
          </Link>

          <nav className="flex flex-wrap items-center gap-2">
            <NavLink to="/flights" className={navLinkClass}>
              Flights
            </NavLink>
            {isAuthenticated && (
              <>
                <NavLink to="/bookings/my" className={navLinkClass}>
                  <span className="inline-flex items-center gap-1">
                    <Ticket className="h-4 w-4" />
                    My Bookings
                  </span>
                </NavLink>
                <NavLink to="/aeromate" className={navLinkClass}>
                  <span className="inline-flex items-center gap-1">
                    <MessageCircle className="h-4 w-4" />
                    AeroMate
                  </span>
                </NavLink>
              </>
            )}
            {isAdmin && (
              <NavLink to="/admin" className={navLinkClass}>
                <span className="inline-flex items-center gap-1">
                  <ShieldCheck className="h-4 w-4" />
                  Admin
                </span>
              </NavLink>
            )}
          </nav>

          <div className="flex items-center gap-2">
            {isAuthenticated ? (
              <>
                <span className="hidden text-sm font-medium text-slate-600 md:inline">{user?.fullName}</span>
                <button
                  type="button"
                  onClick={logout}
                  className="inline-flex items-center gap-1 rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-700"
                >
                  <LogOut className="h-4 w-4" />
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link className="rounded-full px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100" to="/login">
                  Login
                </Link>
                <Link className="rounded-full bg-sky-700 px-4 py-2 text-sm font-semibold text-white hover:bg-sky-600" to="/register">
                  Register
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
