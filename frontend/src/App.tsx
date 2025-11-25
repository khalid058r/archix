"use client"

import { AuthProvider } from "./auth/auth-context"
import { AppRouter } from "./routing/app-router"

export default function App() {
  return (
    <AuthProvider>
      <AppRouter />
    </AuthProvider>
  )
}
