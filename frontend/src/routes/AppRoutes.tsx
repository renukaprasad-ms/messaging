import { Route, Routes } from "react-router";
import AuthLayout from "../layouts/AuthLayout";
import Login from "../pages/auth/Login";

const AppRoutes = () => {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<Login />} />
      </Route>
    </Routes>
  );
};

export default AppRoutes;
