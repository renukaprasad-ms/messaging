import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { FiArrowLeft, FiArrowRight, FiBriefcase, FiCheck, FiUser } from 'react-icons/fi'
import { useDispatch } from 'react-redux'
import { Link, useNavigate } from 'react-router'
import { homePath } from '../../auth/access'
import { isStrongPassword } from '../../auth/passwordPolicy'
import PasswordField from '../../components/PasswordField'
import { authService, getApiErrorMessage, type AccountType } from '../../service/authService'
import { companyService, type CompanyCreateRequest } from '../../service/companyService'
import { uploadImage } from '../../service/storageService'
import { subscriptionService, type SubscriptionPlan } from '../../service/subscriptionService'
import { userService } from '../../service/userService'
import { setLoading, setUser } from '../../store/auth/authSlice'

const emptyCompany: CompanyCreateRequest = {
  name: '',
  displayName: '',
  logoUrl: '',
  legalName: '',
  website: '',
  businessEmail: '',
  businessPhone: '',
  industry: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  state: '',
  postalCode: '',
  country: '',
}

export default function Register() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const [step, setStep] = useState(0)
  const [form, setForm] = useState({
    name: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    profilePhotoUrl: '',
    accountType: 'INDIVIDUAL' as AccountType,
  })
  const [company, setCompany] = useState(emptyCompany)
  const [profilePhoto, setProfilePhoto] = useState<File | null>(null)
  const [companyLogo, setCompanyLogo] = useState<File | null>(null)
  const [plans, setPlans] = useState<SubscriptionPlan[]>([])
  const [selectedPlan, setSelectedPlan] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const isOrg = form.accountType === 'ORGANIZATION'
  const steps = isOrg
    ? ['Type', 'User', 'Organization', 'Subscription']
    : ['Type', 'User', 'Subscription']

  useEffect(() => {
    const controller = new AbortController()
    subscriptionService
      .listPlans(form.accountType, controller.signal)
      .then((plans) => {
        setPlans(plans)
        setSelectedPlan((current) => current || plans[0]?.code || '')
      })
      .catch(() => {
        setPlans([])
      })
    return () => controller.abort()
  }, [form.accountType])

  const currentStep = steps[step]
  const selectedPlanData = useMemo(
    () => plans.find((plan) => plan.code === selectedPlan) ?? plans[0],
    [plans, selectedPlan],
  )

  function next() {
    setError('')
    if (currentStep === 'User') {
      if (!isStrongPassword(form.password)) return setError('Meet all password requirements.')
      if (form.password !== form.confirmPassword)
        return setError('Password and confirm password must match.')
    }
    setStep((value) => Math.min(value + 1, steps.length - 1))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)
    dispatch(setLoading(true))

    try {
      const response = await authService.register({ ...form, profilePhotoUrl: '' })
      let currentUser = response.data
      if (currentUser) dispatch(setUser(currentUser))

      const profilePhotoUrl = await uploadImage('PROFILE_PHOTO', profilePhoto)
      if (profilePhotoUrl && currentUser) {
        const updated = await userService.updateMe({
          name: currentUser.name,
          phone: currentUser.phone,
          profilePhotoUrl,
        })
        currentUser = updated.data ?? currentUser
        dispatch(setUser(currentUser))
      }

      if (isOrg) {
        const logoUrl = await uploadImage('COMPANY_LOGO', companyLogo)
        const created = await companyService.createCompany({ ...company, logoUrl })
        const companyId = created.data?.id
        if (companyId && selectedPlan) {
          await subscriptionService.selectCompanyPlan(String(companyId), selectedPlan)
          navigate(`/companies/${companyId}/subscription`, { replace: true })
          return
        }
      }

      if (currentUser) navigate(homePath(currentUser), { replace: true })
    } catch (err) {
      setError(getApiErrorMessage(err, 'Unable to create account. Please try again.'))
    } finally {
      setIsSubmitting(false)
      dispatch(setLoading(false))
    }
  }

  return (
    <div>
      <p className="eyebrow">CREATE ACCOUNT</p>
      <h1 className="mt-2 text-3xl font-bold tracking-tight text-slate-950">{currentStep}</h1>
      <div
        className="mt-5 grid gap-2 text-[11px] font-semibold text-slate-500"
        style={{ gridTemplateColumns: `repeat(${steps.length}, minmax(0, 1fr))` }}
      >
        {steps.map((label, index) => (
          <span
            key={label}
            className={`rounded-md px-2 py-1.5 text-center ${
              index <= step ? 'bg-indigo-50 text-indigo-700' : 'bg-slate-50'
            }`}
          >
            {label}
          </span>
        ))}
      </div>

      <form className="mt-7 space-y-5" onSubmit={handleSubmit}>
        {currentStep === 'Type' && (
          <div className="grid gap-3">
            <TypeCard
              icon={FiUser}
              title="Individual"
              description="A personal workspace for one user. You can switch to an organization later."
              active={form.accountType === 'INDIVIDUAL'}
              onClick={() => {
                setForm((current) => ({ ...current, accountType: 'INDIVIDUAL' }))
                setSelectedPlan('')
              }}
            />
            <TypeCard
              icon={FiBriefcase}
              title="Organization"
              description="A company workspace with members, channels, contacts, ads and shared billing."
              active={form.accountType === 'ORGANIZATION'}
              onClick={() => {
                setForm((current) => ({ ...current, accountType: 'ORGANIZATION' }))
                setSelectedPlan('')
              }}
            />
          </div>
        )}

        {currentStep === 'User' && (
          <>
            <Field
              label="Full name"
              value={form.name}
              onChange={(name) => setForm({ ...form, name })}
            />
            <Field
              label="Email address"
              type="email"
              value={form.email}
              onChange={(email) => setForm({ ...form, email })}
            />
            <Field
              label="Phone number"
              type="tel"
              value={form.phone}
              onChange={(phone) => setForm({ ...form, phone })}
              required={false}
            />
            <FileField label="Profile photo" onChange={setProfilePhoto} />
            <PasswordField
              value={form.password}
              onChange={(password) => setForm({ ...form, password })}
            />
            <Field
              label="Confirm password"
              type="password"
              value={form.confirmPassword}
              onChange={(confirmPassword) => setForm({ ...form, confirmPassword })}
              maxLength={72}
            />
          </>
        )}

        {currentStep === 'Organization' && (
          <>
            <Field label="Company name" value={company.name} onChange={updateCompany('name')} />
            <Field
              label="Display name"
              value={company.displayName}
              onChange={updateCompany('displayName')}
              required={false}
            />
            <FileField label="Company photo" onChange={setCompanyLogo} />
            <Field
              label="Website"
              value={company.website}
              onChange={updateCompany('website')}
              required={false}
            />
            <Field
              label="Business email"
              type="email"
              value={company.businessEmail}
              onChange={updateCompany('businessEmail')}
              required={false}
            />
            <Field
              label="Industry"
              value={company.industry}
              onChange={updateCompany('industry')}
              required={false}
            />
            <Field
              label="Address line 1"
              value={company.addressLine1}
              onChange={updateCompany('addressLine1')}
            />
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="City" value={company.city} onChange={updateCompany('city')} />
              <Field label="Country" value={company.country} onChange={updateCompany('country')} />
            </div>
          </>
        )}

        {currentStep === 'Subscription' && (
          <div className="space-y-3">
            {(plans.length ? plans : fallbackPlans(form.accountType)).map((plan) => (
              <button
                key={plan.code}
                type="button"
                className={`w-full rounded-xl border p-4 text-left transition ${
                  (selectedPlan || plans[0]?.code || plan.code) === plan.code
                    ? 'border-indigo-500 bg-indigo-50'
                    : 'border-slate-200 hover:border-indigo-200'
                }`}
                onClick={() => setSelectedPlan(plan.code)}
              >
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <p className="font-semibold text-slate-950">{plan.name}</p>
                    <p className="mt-1 text-xs text-slate-500">
                      {plan.accountType.toLowerCase()} plan
                    </p>
                  </div>
                  <p className="text-sm font-bold">
                    {plan.currency} {Number(plan.monthlyPrice).toFixed(2)}
                  </p>
                </div>
                <p className="mt-3 text-xs leading-5 text-slate-500">
                  {plan.limits
                    .filter(
                      (limit) =>
                        limit.featureKey !== 'users.max_count' ||
                        plan.accountType === 'ORGANIZATION',
                    )
                    .slice(0, 4)
                    .map((limit) => `${limit.limitValue} ${limit.unit}`)
                    .join(' / ')}
                </p>
              </button>
            ))}
            {selectedPlanData && (
              <p className="text-xs text-slate-500">Selected: {selectedPlanData.name}</p>
            )}
          </div>
        )}

        {error && <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-600">{error}</p>}

        <div className="flex gap-3">
          {step > 0 && (
            <button
              type="button"
              className="secondary-button h-12 flex-1"
              onClick={() => setStep(step - 1)}
            >
              <FiArrowLeft aria-hidden="true" />
              Back
            </button>
          )}
          {step < steps.length - 1 ? (
            <button type="button" className="primary-button h-12 flex-1" onClick={next}>
              Next
              <FiArrowRight aria-hidden="true" />
            </button>
          ) : (
            <button type="submit" disabled={isSubmitting} className="primary-button h-12 flex-1">
              <FiCheck aria-hidden="true" />
              {isSubmitting ? 'Creating...' : 'Create account'}
            </button>
          )}
        </div>
      </form>

      <p className="mt-6 text-center text-xs text-slate-500">
        Already have an account?{' '}
        <Link to="/login" className="font-semibold text-indigo-500">
          Sign in
        </Link>
      </p>
    </div>
  )

  function updateCompany(field: keyof CompanyCreateRequest) {
    return (value: string) => setCompany((current) => ({ ...current, [field]: value }))
  }
}

function TypeCard({
  icon: Icon,
  title,
  description,
  active,
  onClick,
}: {
  icon: typeof FiUser
  title: string
  description: string
  active: boolean
  onClick: () => void
}) {
  return (
    <button
      type="button"
      className={`flex items-start gap-4 rounded-xl border p-4 text-left transition ${
        active ? 'border-indigo-500 bg-indigo-50' : 'border-slate-200 hover:border-indigo-200'
      }`}
      onClick={onClick}
    >
      <span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-white text-indigo-600 ring-1 ring-slate-100">
        <Icon aria-hidden="true" />
      </span>
      <span>
        <span className="block font-semibold text-slate-950">{title}</span>
        <span className="mt-1 block text-xs leading-5 text-slate-500">{description}</span>
      </span>
    </button>
  )
}

function Field({
  label,
  value,
  onChange,
  type = 'text',
  required = true,
  maxLength = 150,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  type?: string
  required?: boolean
  maxLength?: number
}) {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-medium text-slate-800">{label}</span>
      <input
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        required={required}
        maxLength={maxLength}
        className="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50"
      />
    </label>
  )
}

function FileField({ label, onChange }: { label: string; onChange: (file: File | null) => void }) {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-medium text-slate-800">{label}</span>
      <input
        type="file"
        accept="image/*"
        onChange={(event) => onChange(event.target.files?.[0] ?? null)}
        className="block w-full text-sm text-slate-500 file:mr-4 file:rounded-lg file:border-0 file:bg-indigo-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-indigo-700"
      />
    </label>
  )
}

function fallbackPlans(accountType: AccountType): SubscriptionPlan[] {
  return [
    {
      id: 0,
      code: accountType === 'ORGANIZATION' ? 'FREE_ORGANIZATION' : 'FREE_INDIVIDUAL',
      accountType,
      name: accountType === 'ORGANIZATION' ? 'Free Organization' : 'Free Individual',
      monthlyPrice: 0,
      currency: 'INR',
      active: true,
      limits: [],
      pricing: [],
    },
  ]
}
