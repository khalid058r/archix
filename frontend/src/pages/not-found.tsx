"use client"

import Link from "next/link"
import { Button } from "../components/ui/button"

export function NotFoundPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-neutral-50">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-neutral-900 mb-2">404</h1>
        <p className="text-2xl text-neutral-600 mb-6">Page not found</p>
        <Button variant="primary" className="inline-block">
          <Link href="/dashboard">Go to Dashboard</Link>
        </Button>
      </div>
    </div>
  )
}
