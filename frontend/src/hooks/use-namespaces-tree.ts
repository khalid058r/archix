"use client"

import { useState, useEffect } from "react"
import type { Namespace, Document } from "../types/models"
import { namespacesApi } from "../api/namespaces-api"
import { documentsApi } from "../api/documents-api"

export function useNamespacesTree() {
  const [namespaces, setNamespaces] = useState<Namespace[]>([])
  const [documents, setDocuments] = useState<{ [key: number]: Document[] }>({})
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadData = async () => {
      try {
        const nsRes = await namespacesApi.getAll()
        setNamespaces(nsRes.data)
      } catch (error) {
        console.error("Failed to load namespaces:", error)
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [])

  const loadDocumentsForNamespace = async (namespaceId: number) => {
    try {
      const docsRes = await documentsApi.getByNamespace(namespaceId)
      setDocuments((prev) => ({ ...prev, [namespaceId]: docsRes.data }))
    } catch (error) {
      console.error("Failed to load documents:", error)
    }
  }

  return { namespaces, documents, loading, loadDocumentsForNamespace }
}
