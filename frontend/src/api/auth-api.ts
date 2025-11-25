import httpClient from "./http-client"
import type { AuthenticationResponse } from "../types/models"

export const authApi = {
  login: async (email: string, password: string): Promise<string> => {
    const response = await httpClient.post<AuthenticationResponse>("/login", {
      email,
      password,
    })
    return response.data.token
  },

  register: async (email: string, password: string, firstName?: string, lastName?: string) => {
    const response = await httpClient.post<AuthenticationResponse>("/register", {
      email,
      password,
      firstName,
      lastName,
    })
    return response.data.token
  },

  verifyJwt: async (token: string): Promise<boolean> => {
    try {
      await httpClient.post("/verifyJwt", { token })
      return true
    } catch {
      return false
    }
  },
}
