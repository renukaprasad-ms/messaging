import { Outlet } from "react-router";
import Header from "../components/Header";

const AuthLayout = () => {
  return (
    <div>
      <Header />
      <Outlet />
    </div>
  );
};

export default AuthLayout;
