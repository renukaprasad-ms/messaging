import { Outlet } from "react-router";
import Header from "../components/Header";

const MainLayout = () => {
  return (
    <main className="min-h-dvh bg-slate-50">
      <Header />
      <Outlet />
    </main>
  );
};

export default MainLayout;
