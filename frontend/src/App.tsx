import AppRoutes from './routes/AppRoutes.tsx'
import { useEffect, useState } from 'react'
import { restoreSession } from './auth/restoreSession'
import { useSelector } from 'react-redux'
import type { RootState } from './store/store'

const App = () => {
  const [failed, setFailed] = useState(false)
  const initialized = useSelector((state: RootState) => state.auth.initialized)
  useEffect(() => {
    let active = true
    restoreSession().catch(() => {
      if (active) setFailed(true)
    })
    return () => {
      active = false
    }
  }, [])
  if (failed)
    return (
      <div role="alert" className="p-8">
        Unable to connect. <button onClick={() => window.location.reload()}>Retry</button>
      </div>
    )
  if (!initialized)
    return (
      <p role="status" className="p-8">
        Restoring your session…
      </p>
    )
  return <AppRoutes />
}

export default App
