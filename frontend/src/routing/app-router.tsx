"use client"

import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom"
import { ProtectedRoute } from "../auth/protected-route"
import { LoginPage } from "../pages/auth/login-page"
import { RegisterPage } from "../pages/auth/register-page"
import { DashboardPage } from "../pages/dashboard/dashboard-page"
import { UsersListPage } from "../pages/users/users-list-page"
import { NotFoundPage } from "../pages/not-found"

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/users"
          element={
            <ProtectedRoute>
              <UsersListPage />
            </ProtectedRoute>
          }
        />

        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  )
}
