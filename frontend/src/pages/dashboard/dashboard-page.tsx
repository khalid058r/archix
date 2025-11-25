"use client"

import { Card } from "../../components/ui/card"
import { AppLayout } from "../../components/layout/app-layout"
import { Users, Folder, Lock, Building } from "lucide-react"

export function DashboardPage() {
  const stats = [
    { icon: Users, label: "Total Users", value: "24", color: "blue" },
    { icon: Building, label: "Departments", value: "6", color: "green" },
    { icon: Folder, label: "Documents", value: "342", color: "purple" },
    { icon: Lock, label: "Permissions", value: "18", color: "amber" },
  ]

  return (
    <AppLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-neutral-900">Dashboard</h1>
          <p className="text-neutral-600 mt-1">Welcome back to Archix</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {stats.map((stat, i) => (
            <Card key={i} className="flex items-center gap-4">
              <div className={`p-3 rounded-lg bg-${stat.color}-100`}>
                <stat.icon className={`text-${stat.color}-600`} size={24} />
              </div>
              <div>
                <p className="text-neutral-600 text-sm">{stat.label}</p>
                <p className="text-2xl font-bold text-neutral-900">{stat.value}</p>
              </div>
            </Card>
          ))}
        </div>

        <Card>
          <h2 className="text-lg font-semibold text-neutral-900 mb-4">Recent Activity</h2>
          <p className="text-neutral-600">No recent activity to display</p>
        </Card>
      </div>
    </AppLayout>
  )
}
