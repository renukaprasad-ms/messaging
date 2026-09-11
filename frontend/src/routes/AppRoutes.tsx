import { Navigate, Route, Routes } from 'react-router'
import AuthLayout from '../layouts/AuthLayout'
import MainLayout from '../layouts/MainLayout'

const AppRoutes = () => {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route
          index
          element={
            <section className="p-6">
              <p className="eyebrow">Starter</p>
              <h1 className="mt-2 text-2xl font-bold tracking-tight text-slate-950">
                Frontend setup is ready.
              </h1>
            </section>
          }
        />
      </Route>
      <Route element={<AuthLayout />}>
        <Route
          path="/auth"
          element={
            <div className="panel p-6">
              <p className="eyebrow">Auth</p>
              <h1 className="mt-2 text-xl font-bold text-slate-950">Auth layout ready.</h1>
            </div>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default AppRoutes
