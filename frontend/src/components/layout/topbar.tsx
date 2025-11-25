import { User, Bell } from "lucide-react"

export function Topbar() {
  return (
    <header className="bg-white border-b border-neutral-200 px-6 py-4 flex items-center justify-between">
      <h2 className="text-xl font-semibold text-neutral-900">Dashboard</h2>
      <div className="flex items-center gap-4">
        <button className="p-2 hover:bg-neutral-100 rounded-lg">
          <Bell size={20} className="text-neutral-600" />
        </button>
        <button className="p-2 hover:bg-neutral-100 rounded-lg">
          <User size={20} className="text-neutral-600" />
        </button>
      </div>
    </header>
  )
}
