import { Sidebar } from "./sidebar"
import { Topbar } from "./topbar"
import type { ReactNode } from "react"

export function AppLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex h-screen bg-neutral-50">
      <Sidebar />
      <div className="flex-1 flex flex-col">
        <Topbar />
        <main className="flex-1 overflow-auto p-6">{children}</main>
      </div>
    </div>
  )
}
