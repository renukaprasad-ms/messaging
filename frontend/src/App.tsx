import { useEffect, useState } from 'react'
import { restoreSession } from './auth/restoreSession'
import AppRoutes from './routes/AppRoutes.tsx'

const App = () => {
  const [failed, setFailed] = useState(false)
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
  return <AppRoutes />
}

export default App
