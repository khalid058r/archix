import axios, { type AxiosError } from "axios"
import type { ErrorResponse } from "../types/models"

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"

class AuthStore {
  private token: string | null = null

  constructor() {
    this.token = localStorage.getItem("auth_token")
  }

  setToken(token: string) {
    this.token = token
    localStorage.setItem("auth_token", token)
  }

  getToken(): string | null {
    return this.token
  }

  clearToken() {
    this.token = null
    localStorage.removeItem("auth_token")
  }

  isAuthenticated(): boolean {
    return !!this.token
  }
}

export const authStore = new AuthStore()

const httpClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
})

httpClient.interceptors.request.use(
  (config) => {
    const token = authStore.getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

httpClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    if (error.response?.status === 401) {
      authStore.clearToken()
      window.location.href = "/login"
    }
    return Promise.reject(error)
  },
)

export default httpClient
