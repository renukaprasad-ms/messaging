import { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { Link } from 'react-router'
import { companyService, type CompanySummary } from '../service/companyService'
import { getApiErrorMessage } from '../service/authService'
import {
  subscriptionService,
  type CompanySubscription,
} from '../service/subscriptionService'
import type { RootState } from '../store/store'

export default function Dashboard() {
  const user = useSelector((state: RootState) => state.auth.user)
  const [companies, setCompanies] = useState<CompanySummary[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [hasNext, setHasNext] = useState(false)
  const [subscriptionState, setSubscriptionState] = useState<{
    companyId: number
    subscription: CompanySubscription
  }>()
  const pendingVerification = user?.status === 'PENDING_VERIFICATION'

  useEffect(() => {
    if (pendingVerification) return
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
  }, [page, pendingVerification])

  function go(next: number) {
    setLoading(true)
    setError('')
    setPage(next)
  }
  const visibleCompanies = pendingVerification ? [] : companies
  const isLoading = pendingVerification ? false : loading
  const canCreateCompany = !pendingVerification
  const primaryCompany = visibleCompanies[0]
  const primaryCompanyId = primaryCompany?.id
  const subscription =
    subscriptionState?.companyId === primaryCompanyId ? subscriptionState.subscription : undefined

  useEffect(() => {
    if (!primaryCompanyId || pendingVerification) return
    const controller = new AbortController()
    subscriptionService
      .getCompanySubscription(String(primaryCompanyId), controller.signal)
      .then((data) => {
        if (data) setSubscriptionState({ companyId: primaryCompanyId, subscription: data })
      })
      .catch(() => {
        return
      })
    return () => controller.abort()
  }, [primaryCompanyId, pendingVerification])

  return (
    <section className="space-y-6 p-5 lg:p-7">
      {pendingVerification && (
        <div className="panel border-amber-200 bg-amber-50 p-5">
          <p className="text-sm font-semibold text-amber-900">Verify your email</p>
          <p className="mt-2 max-w-3xl text-sm leading-6 text-amber-800">
            Verify {user.email} to create workspaces and use messaging features.
          </p>
          <Link className="secondary-button mt-4 inline-flex bg-white" to="/verify-email">
            Verify email
          </Link>
        </div>
      )}
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {['Conversations', 'Messages sent', 'Broadcast reach', 'Ad spend'].map((label) => (
          <article className="panel p-5" key={label}>
            <p className="text-xs text-slate-500">{label}</p>
            <p className="my-3 text-[26px] font-bold">-</p>
            <p className="text-xs text-slate-400">No activity data yet</p>
          </article>
        ))}
      </div>
      <div className="grid gap-5 xl:grid-cols-[1.9fr_1fr]">
        <article className="panel p-5">
          <div className="flex items-center justify-between gap-3">
            <h1 className="font-semibold">Your workspaces</h1>
            {canCreateCompany && (
              <Link className="text-xs font-semibold text-indigo-600" to="/company/create">
                Create company
              </Link>
            )}
          </div>
          {error && (
            <p role="alert" className="mt-5 text-sm text-red-600">
              {error}
            </p>
          )}
          {isLoading ? (
            <p role="status" className="py-8 text-sm text-slate-500">
              Loading workspaces...
            </p>
          ) : pendingVerification ? (
            <div className="py-10">
              <p className="text-sm text-slate-500">
                Your workspace list will appear after email verification.
              </p>
            </div>
          ) : visibleCompanies.length ? (
            <ul className="mt-5 divide-y divide-slate-100">
              {visibleCompanies.map((company) => (
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
              disabled={page === 0 || isLoading || pendingVerification}
              onClick={() => go(page - 1)}
            >
              Previous
            </button>
            <span>Page {page + 1}</span>
            <button
              className="secondary-button"
              disabled={!hasNext || isLoading || pendingVerification}
              onClick={() => go(page + 1)}
            >
              Next
            </button>
          </div>
        </article>
        <article className="panel p-5">
          <h2 className="font-semibold">Subscription</h2>
          {primaryCompany ? (
            <>
              <p className="mt-5 text-sm text-slate-500">Current plan</p>
              <p className="mt-2 text-2xl font-bold">
                {subscription?.plan.name ?? 'Loading...'}
              </p>
              <p className="mt-2 text-sm leading-6 text-slate-500">
                Manage limits, channel pricing and plan selection for{' '}
                {primaryCompany.displayName || primaryCompany.name}.
              </p>
              <Link
                to={`/companies/${primaryCompany.id}/subscription`}
                className="secondary-button mt-6 inline-flex"
              >
                Manage subscription
              </Link>
            </>
          ) : pendingVerification ? (
            <p className="mt-5 text-sm leading-6 text-slate-500">
              Verify your email before selecting a subscription.
            </p>
          ) : (
            <>
              <p className="mt-5 text-sm leading-6 text-slate-500">
                Your individual workspace is ready. Organization subscriptions appear here after
                you create or join an organization.
              </p>
              {user?.accountType === 'ORGANIZATION' && (
                <Link to="/company/create" className="secondary-button mt-6 inline-flex">
                  Create company
                </Link>
              )}
            </>
          )}
        </article>
      </div>
    </section>
  )
}
