"use client"

import type { ReactNode } from "react"
import { Button } from "./button"

interface ModalProps {
  isOpen: boolean
  title: string
  children: ReactNode
  onClose: () => void
  onConfirm?: () => void
  confirmText?: string
}

export function Modal({ isOpen, title, children, onClose, onConfirm, confirmText = "Confirm" }: ModalProps) {
  if (!isOpen) return null

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-lg p-6 max-w-md w-full">
        <h2 className="text-lg font-semibold text-neutral-900 mb-4">{title}</h2>
        <div className="mb-6">{children}</div>
        <div className="flex gap-2 justify-end">
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          {onConfirm && (
            <Button variant="primary" onClick={onConfirm}>
              {confirmText}
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}
