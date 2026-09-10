import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { FiEdit2, FiPlus, FiTrash2, FiX } from 'react-icons/fi'
import { getApiErrorMessage } from '../../service/authService'
import {
  subscriptionService,
  type ChannelPricing,
  type PlanLimit,
  type SubscriptionPlan,
  type SubscriptionPlanRequest,
} from '../../service/subscriptionService'

const freeLimits: PlanLimit[] = [
  { featureKey: 'broadcast.max_recipients', limitValue: 500, unit: 'recipients' },
  { featureKey: 'users.max_count', limitValue: 3, unit: 'users' },
  { featureKey: 'whatsapp.max_accounts', limitValue: 1, unit: 'accounts' },
  { featureKey: 'sms.max_accounts', limitValue: 1, unit: 'accounts' },
  { featureKey: 'email.max_accounts', limitValue: 1, unit: 'accounts' },
  { featureKey: 'ads.max_providers', limitValue: 1, unit: 'providers' },
  { featureKey: 'whatsapp.templates.max_count', limitValue: 1, unit: 'templates' },
  { featureKey: 'email.templates.max_count', limitValue: 1, unit: 'templates' },
  { featureKey: 'email.daily_free_limit', limitValue: 500, unit: 'segments' },
]

const freePricing: ChannelPricing[] = [
  {
    channel: 'EMAIL',
    messageType: 'PROMOTIONAL',
    pricePerSegment: 0.2,
    freeDailySegments: 500,
    currency: 'INR',
    active: true,
  },
  {
    channel: 'EMAIL',
    messageType: 'CONVERSATIONAL',
    pricePerSegment: 0.2,
    freeDailySegments: 500,
    currency: 'INR',
    active: true,
  },
  {
    channel: 'WHATSAPP',
    messageType: 'PROMOTIONAL',
    pricePerSegment: 0.9,
    freeDailySegments: 0,
    currency: 'INR',
    active: true,
  },
  {
    channel: 'WHATSAPP',
    messageType: 'CONVERSATIONAL',
    pricePerSegment: 0.9,
    freeDailySegments: 0,
    currency: 'INR',
    active: true,
  },
  {
    channel: 'SMS',
    messageType: 'PROMOTIONAL',
    pricePerSegment: 0,
    freeDailySegments: 0,
    currency: 'INR',
    active: true,
  },
  {
    channel: 'SMS',
    messageType: 'CONVERSATIONAL',
    pricePerSegment: 0,
    freeDailySegments: 0,
    currency: 'INR',
    active: true,
  },
]

const emptyForm: SubscriptionPlanRequest = {
  code: 'FREE_ORGANIZATION',
  accountType: 'ORGANIZATION',
  name: 'Free Organization',
  monthlyPrice: 0,
  currency: 'INR',
  active: true,
  limits: freeLimits,
  pricing: freePricing,
}

export default function SubscriptionPlansAdmin() {
  const [plans, setPlans] = useState<SubscriptionPlan[]>([])
  const [form, setForm] = useState<SubscriptionPlanRequest>(emptyForm)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    subscriptionService
      .listAdminPlans(controller.signal)
      .then((data) => {
        setPlans(data)
        setError('')
      })
      .catch((error) => {
        if (!controller.signal.aborted) setError(getApiErrorMessage(error))
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [])

  const activePlanCount = useMemo(() => plans.filter((plan) => plan.active).length, [plans])

  function edit(plan: SubscriptionPlan) {
    setEditingId(plan.id)
    setMessage('')
    setError('')
    setForm({
      code: plan.code,
      accountType: plan.accountType,
      name: plan.name,
      monthlyPrice: Number(plan.monthlyPrice),
      currency: plan.currency,
      active: plan.active,
      limits: plan.limits.map((limit) => ({
        ...limit,
        limitValue: Number(limit.limitValue),
      })),
      pricing: plan.pricing.map((pricing) => ({
        ...pricing,
        pricePerSegment: Number(pricing.pricePerSegment),
      })),
    })
  }

  function resetForm() {
    setEditingId(null)
    setForm(emptyForm)
  }

  async function save(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setMessage('')
    setError('')
    try {
      const saved = editingId
        ? await subscriptionService.updatePlan(editingId, form)
        : await subscriptionService.createPlan(form)
      if (saved) {
        setPlans((current) => {
          const withoutSaved = current.filter((plan) => plan.id !== saved.id)
          return [...withoutSaved, saved].sort((a, b) => a.monthlyPrice - b.monthlyPrice)
        })
      }
      setMessage(editingId ? 'Subscription plan updated.' : 'Subscription plan created.')
      resetForm()
    } catch (error) {
      setError(getApiErrorMessage(error))
    } finally {
      setBusy(false)
    }
  }

  async function remove(plan: SubscriptionPlan) {
    setBusy(true)
    setMessage('')
    setError('')
    try {
      await subscriptionService.deletePlan(plan.id)
      setPlans((current) =>
        current.map((item) => (item.id === plan.id ? { ...item, active: false } : item)),
      )
      setMessage('Subscription plan disabled.')
    } catch (error) {
      setError(getApiErrorMessage(error))
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="space-y-6 p-5 lg:p-7">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="eyebrow">PLATFORM</p>
          <h1 className="mt-2 text-2xl font-bold tracking-tight">All subscription plans</h1>
          <p className="mt-2 text-sm text-slate-500">
            Review every plan, edit limits, and update channel pricing from one place.
          </p>
        </div>
        <div className="panel px-4 py-3 text-sm">
          <span className="font-semibold">{activePlanCount}</span>{' '}
          <span className="text-slate-500">active plans</span>
        </div>
      </div>

      {(message || error) && (
        <p
          role={error ? 'alert' : 'status'}
          className={`text-sm ${error ? 'text-red-600' : 'text-emerald-700'}`}
        >
          {error || message}
        </p>
      )}

      <div className="grid gap-5 xl:grid-cols-[1.35fr_0.65fr]">
        <article className="panel overflow-hidden">
          {loading ? (
            <p role="status" className="p-5 text-sm text-slate-500">
              Loading subscription plans...
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-slate-100 bg-slate-50 text-xs text-slate-500">
                  <tr>
                    <th className="p-4">Plan</th>
                    <th className="p-4">Price</th>
                    <th className="p-4">Included limits</th>
                    <th className="p-4">Segment pricing</th>
                    <th className="p-4">Status</th>
                    <th className="p-4">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {plans.map((plan) => (
                    <tr key={plan.id} className="border-b border-slate-100 align-top">
                      <td className="p-4">
                        <p className="font-semibold">{plan.name}</p>
                        <p className="mt-1 text-xs text-slate-500">{plan.code}</p>
                        <p className="mt-1 text-xs font-semibold text-indigo-600">
                          {plan.accountType}
                        </p>
                      </td>
                      <td className="p-4 font-medium">
                        {plan.currency} {Number(plan.monthlyPrice).toFixed(2)}
                      </td>
                      <td className="p-4">
                        <div className="flex max-w-sm flex-wrap gap-2">
                          {plan.limits.map((limit) => (
                            <span
                              key={limit.featureKey}
                              className="rounded-md bg-slate-50 px-2 py-1 text-[11px] font-medium text-slate-600"
                            >
                              {limit.featureKey}: {Number(limit.limitValue)} {limit.unit}
                            </span>
                          ))}
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="grid gap-1 text-xs text-slate-600">
                          {plan.pricing.map((pricing) => (
                            <span key={`${pricing.channel}-${pricing.messageType}`}>
                              {pricing.channel} {pricing.messageType.toLowerCase()}:{' '}
                              {pricing.currency} {Number(pricing.pricePerSegment).toFixed(2)}
                              {pricing.freeDailySegments
                                ? `, ${pricing.freeDailySegments} free/day`
                                : ''}
                            </span>
                          ))}
                        </div>
                      </td>
                      <td className="p-4">
                        <span className="rounded-md bg-indigo-50 px-2 py-1 text-xs font-semibold text-indigo-700">
                          {plan.active ? 'ACTIVE' : 'DISABLED'}
                        </span>
                      </td>
                      <td className="p-4">
                        <div className="flex gap-2">
                          <button
                            type="button"
                            className="secondary-button"
                            onClick={() => edit(plan)}
                            disabled={busy}
                            aria-label={`Edit ${plan.name}`}
                          >
                            <FiEdit2 aria-hidden="true" />
                            Edit
                          </button>
                          <button
                            type="button"
                            className="secondary-button"
                            onClick={() => remove(plan)}
                            disabled={busy || !plan.active}
                            aria-label={`Disable ${plan.name}`}
                          >
                            <FiTrash2 aria-hidden="true" />
                            Disable
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {plans.length === 0 && <p className="p-5 text-sm text-slate-500">No plans found.</p>}
            </div>
          )}
        </article>

        <form onSubmit={save} className="panel space-y-5 p-5">
          <div className="flex items-center justify-between gap-3">
            <h2 className="font-semibold">{editingId ? 'Edit plan' : 'Create plan'}</h2>
            {editingId ? (
              <button type="button" className="secondary-button" onClick={resetForm}>
                <FiX aria-hidden="true" />
              </button>
            ) : (
              <FiPlus className="text-indigo-500" aria-hidden="true" />
            )}
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <Field
              label="Code"
              value={form.code}
              onChange={(value) => setForm({ ...form, code: value })}
            />
            <Field
              label="Name"
              value={form.name}
              onChange={(value) => setForm({ ...form, name: value })}
            />
            <label className="block text-sm font-medium">
              Account type
              <select
                className="form-input mt-2"
                value={form.accountType}
                onChange={(event) =>
                  setForm({
                    ...form,
                    accountType: event.target.value as SubscriptionPlanRequest['accountType'],
                  })
                }
              >
                <option value="ORGANIZATION">Organization</option>
                <option value="INDIVIDUAL">Individual</option>
              </select>
            </label>
            <NumberField
              label="Monthly price"
              value={form.monthlyPrice}
              onChange={(value) => setForm({ ...form, monthlyPrice: value })}
            />
            <Field
              label="Currency"
              value={form.currency}
              onChange={(value) => setForm({ ...form, currency: value.toUpperCase() })}
              maxLength={3}
            />
          </div>

          <label className="flex items-center gap-2 text-sm font-medium">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(event) => setForm({ ...form, active: event.target.checked })}
              className="accent-indigo-500"
            />
            Active
          </label>

          <PlanCollections form={form} onChange={setForm} />

          <button className="primary-button w-full" disabled={busy}>
            {busy ? 'Saving...' : editingId ? 'Update plan' : 'Create plan'}
          </button>
        </form>
      </div>
    </section>
  )
}

function Field({
  label,
  value,
  onChange,
  maxLength = 100,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  maxLength?: number
}) {
  return (
    <label className="block text-sm font-medium">
      {label}
      <input
        className="form-input mt-2"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        maxLength={maxLength}
        required
      />
    </label>
  )
}

function NumberField({
  label,
  value,
  onChange,
}: {
  label: string
  value: number
  onChange: (value: number) => void
}) {
  return (
    <label className="block text-sm font-medium">
      {label}
      <input
        className="form-input mt-2"
        type="number"
        min="0"
        step="0.01"
        value={value}
        onChange={(event) => onChange(Number(event.target.value))}
        required
      />
    </label>
  )
}

function PlanCollections({
  form,
  onChange,
}: {
  form: SubscriptionPlanRequest
  onChange: (form: SubscriptionPlanRequest) => void
}) {
  return (
    <div className="space-y-4 border-t border-slate-100 pt-5">
      <div>
        <h3 className="text-sm font-semibold">Limits</h3>
        <div className="mt-3 max-h-56 space-y-2 overflow-y-auto pr-1">
          {form.limits.map((limit, index) => (
            <div key={limit.featureKey} className="grid grid-cols-[1fr_82px_92px] gap-2">
              <input className="form-input" value={limit.featureKey} readOnly />
              <input
                className="form-input"
                type="number"
                min="0"
                step="0.01"
                value={limit.limitValue}
                onChange={(event) => {
                  const next = [...form.limits]
                  next[index] = { ...limit, limitValue: Number(event.target.value) }
                  onChange({ ...form, limits: next })
                }}
              />
              <input className="form-input" value={limit.unit} readOnly />
            </div>
          ))}
        </div>
      </div>

      <div>
        <h3 className="text-sm font-semibold">Channel pricing</h3>
        <div className="mt-3 max-h-56 space-y-2 overflow-y-auto pr-1">
          {form.pricing.map((pricing, index) => (
            <div
              key={`${pricing.channel}-${pricing.messageType}`}
              className="grid grid-cols-[1fr_1fr_82px_82px] gap-2"
            >
              <input className="form-input" value={pricing.channel} readOnly />
              <input className="form-input" value={pricing.messageType} readOnly />
              <input
                className="form-input"
                type="number"
                min="0"
                step="0.0001"
                value={pricing.pricePerSegment}
                onChange={(event) => {
                  const next = [...form.pricing]
                  next[index] = { ...pricing, pricePerSegment: Number(event.target.value) }
                  onChange({ ...form, pricing: next })
                }}
              />
              <input
                className="form-input"
                type="number"
                min="0"
                value={pricing.freeDailySegments}
                onChange={(event) => {
                  const next = [...form.pricing]
                  next[index] = { ...pricing, freeDailySegments: Number(event.target.value) }
                  onChange({ ...form, pricing: next })
                }}
              />
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
