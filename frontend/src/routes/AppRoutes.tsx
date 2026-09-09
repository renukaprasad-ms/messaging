import { Navigate, Route, Routes } from 'react-router'
import AuthLayout from '../layouts/AuthLayout'
import MainLayout from '../layouts/MainLayout'
import Dashboard from '../pages/Dashboard'
import ForgotPassword from '../pages/auth/ForgotPassword'
import Login from '../pages/auth/Login'
import Register from '../pages/auth/Register'
import ResetPassword from '../pages/auth/ResetPassword'
import VerifyOtp from '../pages/auth/VerifyOtp'
import CompanyCreate from '../pages/company/CompanyCreate'
import RequireAuth from '../auth/RequireAuth'
import VerifyEmail from '../pages/auth/VerifyEmail'
import PlatformAdmin from '../pages/PlatformAdmin'
import CompanyDetails from '../pages/company/CompanyDetails'
import ChangePassword from '../pages/auth/ChangePassword'
import Profile from '../pages/account/Profile'
import Security from '../pages/account/Security'

const AppRoutes = () => {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/verify-otp" element={<VerifyOtp />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route element={<RequireAuth verified={false} />}>
          <Route path="/verify-email" element={<VerifyEmail />} />
        </Route>
      </Route>
      <Route element={<RequireAuth verified={false} />}>
        <Route element={<MainLayout />}>
          <Route path="/change-password" element={<ChangePassword />} />
        </Route>
      </Route>
      <Route element={<RequireAuth />}>
        <Route element={<MainLayout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/account/profile" element={<Profile />} />
          <Route path="/account/security" element={<Security />} />
          <Route path="/account/settings" element={<Navigate to="/account/profile" replace />} />
          <Route path="/company/create" element={<CompanyCreate />} />
          <Route path="/companies/:companyId" element={<CompanyDetails />} />
          <Route
            path="/forbidden"
            element={
              <p role="alert" className="p-8">
                Your role does not allow access to this page.
              </p>
            }
          />
          <Route element={<RequireAuth platformAdmin />}>
            <Route path="/admin" element={<PlatformAdmin />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default AppRoutes
