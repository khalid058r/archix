"use client"

import { Card } from "../../components/ui/card"
import { LoginForm } from "../../components/forms/login-form"
import Link from "next/link"

export function LoginPage() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-neutral-900 to-neutral-800 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-white mb-2">Archix</h1>
          <p className="text-neutral-400">Document Management System</p>
        </div>

        <Card className="bg-white">
          <h2 className="text-2xl font-bold text-neutral-900 mb-6">Sign In</h2>
          <LoginForm />
        </Card>

        <p className="text-center text-neutral-400 text-sm mt-4">
          Don't have an account?{" "}
          <Link href="/register" className="text-blue-400 hover:underline">
            Create one
          </Link>
        </p>
      </div>
    </div>
  )
}
