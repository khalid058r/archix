"use client"

import { createContext, useContext, useState, type ReactNode, useCallback } from "react"
import { authStore } from "../api/http-client"
import { authApi } from "../api/auth-api"

interface AuthContextType {
  token: string | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, firstName?: string, lastName?: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(authStore.getToken())

  const login = useCallback(async (email: string, password: string) => {
    try {
      const newToken = await authApi.login(email, password)
      authStore.setToken(newToken)
      setToken(newToken)
    } catch (error) {
      throw error
    }
  }, [])

  const register = useCallback(async (email: string, password: string, firstName?: string, lastName?: string) => {
    try {
      const newToken = await authApi.register(email, password, firstName, lastName)
      authStore.setToken(newToken)
      setToken(newToken)
    } catch (error) {
      throw error
    }
  }, [])

  const logout = useCallback(() => {
    authStore.clearToken()
    setToken(null)
  }, [])

  return (
    <AuthContext.Provider value={{ token, isAuthenticated: !!token, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider")
  }
  return context
}
