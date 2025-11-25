"use client"

import type React from "react"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "../../auth/auth-context"
import { Button } from "../ui/button"
import { Input } from "../ui/input"
import { z } from "zod"
import Link from "next/link"

const registerSchema = z.object({
  email: z.string().email("Invalid email"),
  password: z.string().min(8, "Password must be at least 8 characters"),
  firstName: z.string().min(1, "First name is required"),
  lastName: z.string().min(1, "Last name is required"),
})

export function RegisterForm() {
  const router = useRouter()
  const { register } = useAuth()
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    firstName: "",
    lastName: "",
  })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrors({})

    try {
      registerSchema.parse(formData)
      setLoading(true)
      await register(formData.email, formData.password, formData.firstName, formData.lastName)
      router.push("/dashboard")
    } catch (error) {
      if (error instanceof z.ZodError) {
        const fieldErrors: Record<string, string> = {}
        error.errors.forEach((err) => {
          if (err.path) {
            fieldErrors[err.path[0]] = err.message
          }
        })
        setErrors(fieldErrors)
      } else {
        setErrors({ submit: "Registration failed" })
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input
        label="First Name"
        value={formData.firstName}
        onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
        error={errors.firstName}
        placeholder="John"
      />
      <Input
        label="Last Name"
        value={formData.lastName}
        onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
        error={errors.lastName}
        placeholder="Doe"
      />
      <Input
        label="Email"
        type="email"
        value={formData.email}
        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
        error={errors.email}
        placeholder="you@example.com"
      />
      <Input
        label="Password"
        type="password"
        value={formData.password}
        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
        error={errors.password}
        placeholder="••••••••"
      />
      {errors.submit && <p className="text-red-600 text-sm">{errors.submit}</p>}
      <Button type="submit" disabled={loading} className="w-full">
        {loading ? "Creating account..." : "Sign Up"}
      </Button>
      <p className="text-sm text-center text-neutral-600">
        Already have an account?{" "}
        <Link href="/login" className="text-blue-600 hover:underline">
          Sign in
        </Link>
      </p>
    </form>
  )
}
