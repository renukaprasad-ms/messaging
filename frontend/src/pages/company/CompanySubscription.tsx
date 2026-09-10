import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router'
import { canManageCompany } from '../../auth/access'
import { getApiErrorMessage } from '../../service/authService'
import { companyService, type CompanyResponse } from '../../service/companyService'
import {
  subscriptionService,
  type CompanySubscription,
  type SubscriptionPlan,
} from '../../service/subscriptionService'

export default function CompanySubscriptionPage() {
  const { companyId = '' } = useParams()
  const [company, setCompany] = useState<CompanyResponse>()
  const [subscription, setSubscription] = useState<CompanySubscription>()
  const [plans, setPlans] = useState<SubscriptionPlan[]>([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([
      companyService.get(companyId, controller.signal),
      subscriptionService.getCompanySubscription(companyId, controller.signal),
      subscriptionService.listPlans('ORGANIZATION', controller.signal),
    ])
      .then(([company, subscription, plans]) => {
        setCompany(company)
        setSubscription(subscription)
        setPlans(plans)
        setMessage('')
      })
      .catch((error) => {
        if (!controller.signal.aborted) setMessage(getApiErrorMessage(error))
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [companyId])

  const editable = Boolean(company && canManageCompany(company.role))
  const currentCode = subscription?.plan.code
  const selectedPlan = useMemo(
    () => plans.find((plan) => plan.code === currentCode) ?? subscription?.plan,
    [currentCode, plans, subscription],
  )

  async function select(plan: SubscriptionPlan) {
    setBusy(true)
    setMessage('')
    try {
      const next = await subscriptionService.selectCompanyPlan(companyId, plan.code)
      setSubscription(next)
      setMessage(`${plan.name} selected.`)
    } catch (error) {
      setMessage(getApiErrorMessage(error))
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="space-y-6 p-5 lg:p-7">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="eyebrow">COMPANY</p>
          <h1 className="mt-2 text-2xl font-bold tracking-tight">Subscription</h1>
          <p className="mt-2 text-sm text-slate-500">
            View limits, pricing and the active plan for this workspace.
          </p>
        </div>
        {company && (
          <Link className="secondary-button" to={`/companies/${company.id}`}>
            Company profile
          </Link>
        )}
      </div>

      {message && (
        <p role="status" className="text-sm text-slate-600">
          {message}
        </p>
      )}

      {loading ? (
        <p role="status" className="text-sm text-slate-500">
          Loading subscription...
        </p>
      ) : (
        <div className="grid gap-5 xl:grid-cols-[0.85fr_1.15fr]">
          <article className="panel p-5">
            <p className="text-xs font-semibold text-slate-400">CURRENT PLAN</p>
            <h2 className="mt-3 text-3xl font-bold">{selectedPlan?.name ?? '-'}</h2>
            <p className="mt-2 text-sm text-slate-500">
              {selectedPlan
                ? `${selectedPlan.currency} ${Number(selectedPlan.monthlyPrice).toFixed(2)} / month`
                : 'No subscription selected'}
            </p>
            <span className="mt-5 inline-flex rounded-md bg-indigo-50 px-2 py-1 text-xs font-semibold text-indigo-700">
              {subscription?.status ?? 'NOT SELECTED'}
            </span>
          </article>

          <article className="panel overflow-hidden">
            <div className="border-b border-slate-100 p-5">
              <h2 className="font-semibold">Available plans</h2>
              {!editable && (
                <p className="mt-2 text-sm text-slate-500">
                  Contact a company owner or administrator to change the subscription.
                </p>
              )}
            </div>
            <div className="grid gap-4 p-5 lg:grid-cols-2">
              {plans.map((plan) => (
                <div key={plan.id} className="rounded-lg border border-slate-200 p-4">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h3 className="font-semibold">{plan.name}</h3>
                      <p className="mt-1 text-xs text-slate-500">{plan.code}</p>
                    </div>
                    <span className="text-sm font-semibold">
                      {plan.currency} {Number(plan.monthlyPrice).toFixed(2)}
                    </span>
                  </div>
                  <PlanSummary plan={plan} />
                  <button
                    type="button"
                    className="primary-button mt-5 w-full"
                    disabled={!editable || busy || currentCode === plan.code}
                    onClick={() => select(plan)}
                  >
                    {currentCode === plan.code ? 'Current plan' : 'Select plan'}
                  </button>
                </div>
              ))}
            </div>
          </article>
        </div>
      )}
    </section>
  )
}

function PlanSummary({ plan }: { plan: SubscriptionPlan }) {
  return (
    <div className="mt-4 space-y-4">
      <dl className="grid gap-2 text-xs">
        {plan.limits.map((limit) => (
          <div key={limit.featureKey} className="flex justify-between gap-3">
            <dt className="text-slate-500">{limit.featureKey.replaceAll('.', ' ')}</dt>
            <dd className="font-semibold text-slate-800">
              {Number(limit.limitValue)} {limit.unit}
            </dd>
          </div>
        ))}
      </dl>
      <div className="border-t border-slate-100 pt-4">
        <p className="text-xs font-semibold text-slate-400">SEGMENT PRICING</p>
        <dl className="mt-2 grid gap-2 text-xs">
          {plan.pricing.map((pricing) => (
            <div
              key={`${pricing.channel}-${pricing.messageType}`}
              className="flex justify-between gap-3"
            >
              <dt className="text-slate-500">
                {pricing.channel} {pricing.messageType.toLowerCase()}
              </dt>
              <dd className="font-semibold text-slate-800">
                {pricing.currency} {Number(pricing.pricePerSegment).toFixed(2)}
              </dd>
            </div>
          ))}
        </dl>
      </div>
    </div>
  )
}
