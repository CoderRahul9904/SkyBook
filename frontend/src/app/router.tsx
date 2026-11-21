import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from '../components/layout/AppShell'
import { ProtectedRoute } from '../components/layout/ProtectedRoute'
import { AdminPage } from '../pages/AdminPage'
import { AeroMatePage } from '../pages/AeroMatePage'
import { BookingDetailPage } from '../pages/BookingDetailPage'
import { BookingFormPage } from '../pages/BookingFormPage'
import { FlightsPage } from '../pages/FlightsPage'
import { HomePage } from '../pages/HomePage'
import { LoginPage } from '../pages/LoginPage'
import { MyBookingsPage } from '../pages/MyBookingsPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { RegisterPage } from '../pages/RegisterPage'

export function AppRouter() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/flights" element={<FlightsPage />} />

        <Route
          path="/bookings/new"
          element={
            <ProtectedRoute>
              <BookingFormPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/bookings/my"
          element={
            <ProtectedRoute>
              <MyBookingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/bookings/:pnr"
          element={
            <ProtectedRoute>
              <BookingDetailPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin"
          element={
            <ProtectedRoute role="ADMIN">
              <AdminPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/aeromate"
          element={
            <ProtectedRoute>
              <AeroMatePage />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<NotFoundPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
