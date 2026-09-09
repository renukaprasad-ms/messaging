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
    <section className="max-w-3xl space-y-6 p-6 lg:p-7">
      <div>
        <p className="eyebrow">COMPANY</p>
        <h1 className="mt-2 text-2xl font-bold">{company?.name ?? 'Company details'}</h1>
      </div>
      {company && (
        <form onSubmit={save} className="panel space-y-5 p-6">
          <p className="text-sm text-slate-500">
            Your role: <strong className="text-indigo-600">{company.role}</strong>
          </p>
          <label className="block text-sm font-medium">
            Company name
            <input
              className="form-input mt-2"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              maxLength={150}
              readOnly={!editable}
            />
          </label>
          <label className="block text-sm font-medium">
            Display name
            <input
              className="form-input mt-2"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              maxLength={150}
              readOnly={!editable}
            />
          </label>
          {editable ? (
            <button disabled={busy} className="primary-button">
              {busy ? 'Saving…' : 'Save changes'}
            </button>
          ) : (
            <p className="text-sm text-slate-500">
              Contact your company owner or administrator to update these details.
            </p>
          )}
        </form>
      )}
      <p role="status" className="text-sm">
        {message || (!company ? 'Loading company…' : '')}
      </p>
    </section>
  )
}
