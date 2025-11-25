interface BadgeProps {
  text: string
  type: "view" | "edit" | "admin" | "default"
}

export function Badge({ text, type }: BadgeProps) {
  const typeClasses = {
    view: "bg-blue-100 text-blue-800",
    edit: "bg-amber-100 text-amber-800",
    admin: "bg-red-100 text-red-800",
    default: "bg-neutral-100 text-neutral-800",
  }

  return <span className={`inline-block px-2 py-1 rounded text-xs font-medium ${typeClasses[type]}`}>{text}</span>
}
