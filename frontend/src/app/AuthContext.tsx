/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import type { AuthResponse, UserResponse, UserRole } from '../types/domain'
import { storage } from '../utils/storage'

interface AuthContextValue {
  token: string | null
  user: UserResponse | null
  isAuthenticated: boolean
  isAdmin: boolean
  saveAuth: (auth: AuthResponse) => void
  logout: () => void
  hasRole: (role: UserRole) => boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => storage.getToken())
  const [user, setUser] = useState<UserResponse | null>(() => storage.getUser<UserResponse>())

  const saveAuth = useCallback((auth: AuthResponse) => {
    setToken(auth.accessToken)
    setUser(auth.user)
    storage.setToken(auth.accessToken)
    storage.setUser(auth.user)
  }, [])

  const logout = useCallback(() => {
    setToken(null)
    setUser(null)
    storage.clearAuth()
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token && user),
      isAdmin: user?.role === 'ADMIN',
      saveAuth,
      logout,
      hasRole: (role) => user?.role === role,
    }),
    [saveAuth, token, user, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
