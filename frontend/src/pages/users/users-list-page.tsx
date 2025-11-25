"use client"

import { AppLayout } from "../../components/layout/app-layout"
import { Card } from "../../components/ui/card"
import { Button } from "../../components/ui/button"
import { Loader } from "../../components/ui/loader"
import { usersApi } from "../../api/users-api"
import { useState, useEffect } from "react"
import type { User } from "../../types/models"
import { useRouter } from "next/navigation"

export function UsersListPage() {
  const router = useRouter()
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadUsers = async () => {
      try {
        const response = await usersApi.getAll()
        setUsers(response.data)
      } catch (error) {
        console.error("Failed to load users:", error)
      } finally {
        setLoading(false)
      }
    }

    loadUsers()
  }, [])

  if (loading) return <Loader />

  return (
    <AppLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-neutral-900">Users</h1>
            <p className="text-neutral-600 mt-1">Manage system users</p>
          </div>
          <Button variant="primary" onClick={() => router.push("/users/new")}>
            Add User
          </Button>
        </div>

        <Card>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-neutral-200">
                  <th className="text-left py-3 px-4 font-semibold text-neutral-700">Name</th>
                  <th className="text-left py-3 px-4 font-semibold text-neutral-700">Email</th>
                  <th className="text-left py-3 px-4 font-semibold text-neutral-700">Department</th>
                  <th className="text-left py-3 px-4 font-semibold text-neutral-700">Status</th>
                  <th className="text-right py-3 px-4 font-semibold text-neutral-700">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id} className="border-b border-neutral-100 hover:bg-neutral-50">
                    <td className="py-3 px-4">
                      {user.firstName} {user.lastName}
                    </td>
                    <td className="py-3 px-4">{user.email}</td>
                    <td className="py-3 px-4">{user.department?.name || "-"}</td>
                    <td className="py-3 px-4">
                      <span
                        className={`inline-block px-2 py-1 rounded text-xs font-medium ${
                          user.isActive ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"
                        }`}
                      >
                        {user.isActive ? "Active" : "Inactive"}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      <Button variant="secondary" size="sm" onClick={() => router.push(`/users/${user.id}`)}>
                        View
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    </AppLayout>
  )
}
