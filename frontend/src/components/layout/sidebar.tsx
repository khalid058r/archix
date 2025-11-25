"use client"

import Link from "next/link"
import { useAuth } from "../../auth/auth-context"
import { Menu, Users, Folder, Lock, Building, LogOut } from "lucide-react"
import { useState } from "react"

export function Sidebar() {
  const { logout } = useAuth()
  const [isOpen, setIsOpen] = useState(true)

  const menuItems = [
    { icon: Menu, label: "Dashboard", path: "/dashboard" },
    { icon: Users, label: "Users", path: "/users" },
    { icon: Building, label: "Departments", path: "/departments" },
    { icon: Building, label: "Organizations", path: "/organizations" },
    { icon: Folder, label: "Namespaces", path: "/namespaces" },
    { icon: Folder, label: "Documents", path: "/documents" },
    { icon: Lock, label: "Permissions", path: "/permissions" },
  ]

  return (
    <aside
      className={`bg-neutral-900 text-white transition-all duration-300 ${
        isOpen ? "w-64" : "w-20"
      } min-h-screen flex flex-col`}
    >
      <div className="p-4 border-b border-neutral-700">
        <h1 className={`font-bold text-lg ${!isOpen && "hidden"}`}>Archix</h1>
      </div>

      <nav className="flex-1 p-4 space-y-2">
        {menuItems.map((item) => (
          <Link
            key={item.path}
            href={item.path}
            className="flex items-center gap-3 px-4 py-2 rounded-lg hover:bg-neutral-800 transition-colors"
          >
            <item.icon size={20} />
            {isOpen && <span className="text-sm">{item.label}</span>}
          </Link>
        ))}
      </nav>

      <div className="p-4 border-t border-neutral-700">
        <button
          onClick={logout}
          className="flex items-center gap-3 w-full px-4 py-2 rounded-lg hover:bg-neutral-800 transition-colors text-red-400"
        >
          <LogOut size={20} />
          {isOpen && <span className="text-sm">Logout</span>}
        </button>
      </div>
    </aside>
  )
}
