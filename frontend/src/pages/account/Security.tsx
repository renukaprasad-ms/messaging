import { useState } from 'react'
import { FiLock, FiShield } from 'react-icons/fi'
import { Link } from 'react-router'

export default function Security() {
  const [twoFactorEnabled, setTwoFactorEnabled] = useState(false)

  return (
    <section className="space-y-6 p-5 lg:p-7">
      <div>
        <p className="eyebrow">ACCOUNT</p>
        <h1 className="mt-2 text-2xl font-bold tracking-tight">Security</h1>
      </div>

      <div className="grid max-w-5xl gap-5 xl:grid-cols-2">
        <article className="panel p-6">
          <div className="flex items-start gap-4">
            <span className="security-icon">
              <FiLock aria-hidden="true" />
            </span>
            <div className="min-w-0">
              <h2 className="font-semibold">Password</h2>
              <p className="mt-2 text-sm leading-6 text-slate-500">
                Update the password used to sign in to this account.
              </p>
              <Link className="secondary-button mt-5 inline-flex" to="/change-password">
                Change password
              </Link>
            </div>
          </div>
        </article>

        <article className="panel p-6">
          <div className="flex items-start gap-4">
            <span className="security-icon">
              <FiShield aria-hidden="true" />
            </span>
            <div className="min-w-0 flex-1">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h2 className="font-semibold">Two-factor authentication</h2>
                  <p className="mt-2 text-sm leading-6 text-slate-500">
                    Require a second verification step when signing in.
                  </p>
                </div>
                <button
                  type="button"
                  role="switch"
                  aria-checked={twoFactorEnabled}
                  className={`switch ${twoFactorEnabled ? 'is-on' : ''}`}
                  onClick={() => setTwoFactorEnabled((current) => !current)}
                >
                  <span />
                </button>
              </div>
              <p className="mt-5 text-sm font-medium text-slate-600">
                {twoFactorEnabled ? '2FA enabled' : '2FA disabled'}
              </p>
            </div>
          </div>
        </article>
      </div>
    </section>
  )
}
