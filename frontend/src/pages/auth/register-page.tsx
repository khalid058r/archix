"use client"

import { Card } from "../../components/ui/card"
import { RegisterForm } from "../../components/forms/register-form"

export function RegisterPage() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-neutral-900 to-neutral-800 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-white mb-2">Archix</h1>
          <p className="text-neutral-400">Create Your Account</p>
        </div>

        <Card className="bg-white">
          <h2 className="text-2xl font-bold text-neutral-900 mb-6">Sign Up</h2>
          <RegisterForm />
        </Card>
      </div>
    </div>
  )
}
