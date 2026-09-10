import { useEffect, useState } from 'react'
import apiClient from '../service/apiClient'
import { getApiErrorMessage, type ApiResponse, type UserStatus } from '../service/authService'

interface UserPage {
  users: Array<{ id: number; name: string; email: string; status: UserStatus; accountType: string }>
  hasNext: boolean
}

export default function PlatformAdmin() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<UserPage>()
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    const controller = new AbortController()
    apiClient
      .get<ApiResponse<UserPage>>('/api/admin/users', {
        params: { page },
        signal: controller.signal,
      })
      .then((response) => {
        setResult(response.data.data)
        setError('')
      })
      .catch((error) => {
        if (!controller.signal.aborted) setError(getApiErrorMessage(error))
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [page])
  function go(next: number) {
    setLoading(true)
    setPage(next)
  }
  return (
    <section className="space-y-6 p-6 lg:p-7">
      <div>
        <p className="eyebrow">PLATFORM</p>
        <h1 className="mt-2 text-2xl font-bold">User administration</h1>
        <p className="mt-2 text-sm text-slate-500">
          Review registered accounts and their verification status.
        </p>
      </div>
      <div className="panel overflow-hidden">
        {error && (
          <p role="alert" className="p-5 text-red-600">
            {error}
          </p>
        )}
        {loading ? (
          <p role="status" className="p-5">
            Loading users…
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-100 bg-slate-50 text-xs text-slate-500">
                <tr>
                  <th className="p-4">Name</th>
                  <th className="p-4">Email</th>
                  <th className="p-4">Account</th>
                  <th className="p-4">Status</th>
                </tr>
              </thead>
              <tbody>
                {result?.users.map((user) => (
                  <tr key={user.id} className="border-b border-slate-100">
                    <td className="p-4 font-medium">{user.name}</td>
                    <td className="p-4">{user.email}</td>
                    <td className="p-4">{user.accountType.replaceAll('_', ' ')}</td>
                    <td className="p-4">
                      <span className="rounded-md bg-indigo-50 px-2 py-1 text-xs text-indigo-700">
                        {user.status.replaceAll('_', ' ')}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {result?.users.length === 0 && <p className="p-5">No users found.</p>}
          </div>
        )}
        <div className="flex items-center justify-between p-4 text-sm">
          <button
            className="secondary-button"
            disabled={page === 0 || loading}
            onClick={() => go(page - 1)}
          >
            Previous
          </button>
          <span>Page {page + 1}</span>
          <button
            className="secondary-button"
            disabled={!result?.hasNext || loading}
            onClick={() => go(page + 1)}
          >
            Next
          </button>
        </div>
      </div>
    </section>
  )
}
