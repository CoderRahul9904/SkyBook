import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../app/AuthContext'
import type { UserRole } from '../../types/domain'

export function ProtectedRoute({ children, role }: { children: ReactNode; role?: UserRole }) {
  const { isAuthenticated, hasRole } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (role && !hasRole(role)) {
    return <Navigate to="/" replace />
  }

  return <>{children}</>
}
