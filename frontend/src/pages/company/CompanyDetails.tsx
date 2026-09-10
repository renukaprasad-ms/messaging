import { useEffect, useState, type FormEvent } from 'react'
import { useParams } from 'react-router'
import { canManageCompany } from '../../auth/access'
import { getApiErrorMessage } from '../../service/authService'
import { companyService, type CompanyResponse } from '../../service/companyService'

export default function CompanyDetails() {
  const { companyId = '' } = useParams()
  const [company, setCompany] = useState<CompanyResponse>()
  const [name, setName] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    const controller = new AbortController()
    companyService
      .get(companyId, controller.signal)
      .then((data) => {
        setCompany(data)
        setName(data?.name ?? '')
        setDisplayName(data?.displayName ?? '')
      })
      .catch((error) => {
        if (!controller.signal.aborted) setMessage(getApiErrorMessage(error))
      })
    return () => controller.abort()
  }, [companyId])

  async function save(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setMessage('')
    try {
      setCompany(await companyService.update(companyId, { name, displayName }))
      setMessage('Company updated.')
    } catch (error) {
      setMessage(getApiErrorMessage(error))
    } finally {
      setBusy(false)
    }
  }

  const editable = company && canManageCompany(company.role)

  return (
    <section className="space-y-6 p-5 lg:p-7">
      <div>
        <p className="eyebrow">COMPANY</p>
        <h1 className="mt-2 text-2xl font-bold tracking-tight">
          {company?.displayName || company?.name || 'Company profile'}
        </h1>
      </div>

      {company ? (
        <div className="grid max-w-6xl gap-5 xl:grid-cols-[1.1fr_0.9fr]">
          <form onSubmit={save} className="panel space-y-5 p-6">
            <div className="flex items-center justify-between gap-4">
              <h2 className="font-semibold">Profile</h2>
              <span className="rounded-md bg-indigo-50 px-2 py-1 text-xs font-semibold text-indigo-600">
                {company.role}
              </span>
            </div>
            <div className="grid gap-5 md:grid-cols-2">
              <Field
                label="Company name"
                value={name}
                onChange={setName}
                readOnly={!editable}
                required
              />
              <Field
                label="Display name"
                value={displayName}
                onChange={setDisplayName}
                readOnly={!editable}
              />
            </div>
            <dl className="grid gap-4 border-t border-slate-100 pt-5 md:grid-cols-2">
              <Detail label="Legal name" value={company.profile?.legalName} />
              <Detail label="Industry" value={company.profile?.industry} />
              <Detail label="Website" value={company.profile?.website} />
              <Detail label="Business email" value={company.profile?.businessEmail} />
              <Detail label="Business phone" value={company.profile?.businessPhone} />
              <Detail label="Status" value={company.status} />
            </dl>
            {editable ? (
              <button disabled={busy} className="primary-button">
                {busy ? 'Saving...' : 'Save profile'}
              </button>
            ) : (
              <p className="text-sm text-slate-500">
                Contact a company owner or administrator to update these details.
              </p>
            )}
          </form>

          <article className="panel space-y-5 p-6">
            <h2 className="font-semibold">Address</h2>
            <dl className="grid gap-4">
              <Detail label="Address line 1" value={company.address?.addressLine1} />
              <Detail label="Address line 2" value={company.address?.addressLine2} />
              <Detail label="City" value={company.address?.city} />
              <Detail label="State" value={company.address?.state} />
              <Detail label="Postal code" value={company.address?.postalCode} />
              <Detail label="Country" value={company.address?.country} />
            </dl>
          </article>
        </div>
      ) : (
        <p role="status" className="text-sm text-slate-500">
          Loading company...
        </p>
      )}

      {message && (
        <p role="status" className="text-sm text-slate-600">
          {message}
        </p>
      )}
    </section>
  )
}

function Field({
  label,
  value,
  onChange,
  readOnly,
  required = false,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  readOnly?: boolean
  required?: boolean
}) {
  return (
    <label className="block text-sm font-medium">
      {label}
      <input
        className="form-input mt-2"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        readOnly={readOnly}
        required={required}
        maxLength={150}
      />
    </label>
  )
}

function Detail({ label, value }: { label: string; value?: string | null }) {
  return (
    <div>
      <dt className="text-xs font-semibold text-slate-400">{label}</dt>
      <dd className="mt-1 break-words text-sm font-medium text-slate-800">{value || '-'}</dd>
    </div>
  )
}
