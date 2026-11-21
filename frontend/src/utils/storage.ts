const AUTH_TOKEN_KEY = 'skybook.auth.token'
const AUTH_USER_KEY = 'skybook.auth.user'

export const storage = {
  getToken: (): string | null => localStorage.getItem(AUTH_TOKEN_KEY),
  setToken: (token: string) => localStorage.setItem(AUTH_TOKEN_KEY, token),
  clearToken: () => localStorage.removeItem(AUTH_TOKEN_KEY),

  getUser: <T>(): T | null => {
    const raw = localStorage.getItem(AUTH_USER_KEY)
    if (!raw) {
      return null
    }

    try {
      return JSON.parse(raw) as T
    } catch {
      localStorage.removeItem(AUTH_USER_KEY)
      return null
    }
  },

  setUser: <T>(user: T) => localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user)),
  clearUser: () => localStorage.removeItem(AUTH_USER_KEY),

  clearAuth: () => {
    localStorage.removeItem(AUTH_TOKEN_KEY)
    localStorage.removeItem(AUTH_USER_KEY)
  },
}
