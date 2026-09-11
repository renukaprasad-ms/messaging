import { Navigate, Route, Routes } from 'react-router'
import AuthLayout from '../layouts/AuthLayout'
import MainLayout from '../layouts/MainLayout'
import Login from '../pages/auth/Login'
import Register from '../pages/auth/Register'
import VerifyOtp from '../pages/auth/VerifyOtp'

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
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/verifyotp" element={<VerifyOtp />} />
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default AppRoutes
