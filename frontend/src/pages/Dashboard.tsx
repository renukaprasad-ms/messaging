import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { companyService, type CompanyResponse } from '../service/companyService'
import { getApiErrorMessage } from '../service/authService'

export default function Dashboard() {
  const [companies, setCompanies] = useState<CompanyResponse[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [hasNext, setHasNext] = useState(false)
  useEffect(() => {
    let active = true
    companyService
      .list(page)
      .then((data) => {
        if (active) {
          setCompanies(data.companies)
          setHasNext(data.hasNext)
        }
      })
      .catch((error) => {
        if (active) setError(getApiErrorMessage(error))
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [page])
  function go(next: number) {
    setLoading(true)
    setError('')
    setPage(next)
  }
  return (
    <section className="space-y-6 p-5 lg:p-7">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {['Conversations', 'Messages sent', 'Broadcast reach', 'Ad spend'].map((label) => (
          <article className="panel p-5" key={label}>
            <p className="text-xs text-slate-500">{label}</p>
            <p className="my-3 text-[26px] font-bold">—</p>
            <p className="text-xs text-slate-400">No activity data yet</p>
          </article>
        ))}
      </div>
      <div className="grid gap-5 xl:grid-cols-[1.9fr_1fr]">
        <article className="panel p-5">
          <div className="flex items-center justify-between gap-3">
            <h1 className="font-semibold">Your workspaces</h1>
            <Link className="text-xs font-semibold text-indigo-600" to="/company/create">
              Create company
            </Link>
          </div>
          {error && (
            <p role="alert" className="mt-5 text-sm text-red-600">
              {error}
            </p>
          )}
          {loading ? (
            <p role="status" className="py-8 text-sm text-slate-500">
              Loading workspaces…
            </p>
          ) : companies.length ? (
            <ul className="mt-5 divide-y divide-slate-100">
              {companies.map((company) => (
                <li key={company.id}>
                  <Link
                    to={`/companies/${company.id}`}
                    className="flex items-center justify-between gap-3 py-4"
                  >
                    <span className="text-sm font-medium">
                      {company.displayName || company.name}
                    </span>
                    <span className="rounded-md bg-indigo-50 px-2 py-1 text-xs text-indigo-600">
                      {company.role}
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          ) : (
            <div className="py-10">
              <p className="text-sm text-slate-500">
                Create your first company to set up a workspace.
              </p>
              <Link className="primary-button mt-5 inline-flex" to="/company/create">
                Create company
              </Link>
            </div>
          )}
          <div className="mt-4 flex items-center justify-between text-xs">
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
              disabled={!hasNext || loading}
              onClick={() => go(page + 1)}
            >
              Next
            </button>
          </div>
        </article>
        <article className="panel p-5">
          <h2 className="font-semibold">Account security</h2>
          <p className="mt-5 text-sm leading-6 text-slate-500">
            Your company role controls the workspaces and settings you can access.
          </p>
          <Link to="/change-password" className="secondary-button mt-6 inline-flex">
            Update password
          </Link>
        </article>
      </div>
    </section>
  )
}
