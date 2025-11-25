import httpClient from "./http-client"
import type { User } from "../types/models"

export const usersApi = {
  getAll: () => httpClient.get<User[]>("/api/users"),
  getById: (id: number) => httpClient.get<User>(`/api/users/${id}`),
  update: (user: User) => httpClient.put<User>("/api/users", user),
  changeDepartment: (userId: number, departmentId: number) =>
    httpClient.put(`/api/users/change-department`, { userId, departmentId }),
  changePermissions: (userId: number, permissionIds: number[]) =>
    httpClient.put(`/api/users/change-permissions`, { userId, permissionIds }),
  changePassword: (userId: number, oldPassword: string, newPassword: string) =>
    httpClient.put(`/api/users/change-password`, { userId, oldPassword, newPassword }),
  delete: (id: number) => httpClient.delete(`/api/users/${id}`),
  getByDepartment: (departmentId: number) => httpClient.get<User[]>(`/api/users/departments/${departmentId}`),
}
