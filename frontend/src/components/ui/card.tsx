import type { ReactNode } from "react"

export function Card({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={`bg-white rounded-lg border border-neutral-200 shadow-sm p-6 ${className}`}>{children}</div>
}
