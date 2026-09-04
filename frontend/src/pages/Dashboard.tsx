import { useSelector } from "react-redux";
import { Navigate } from "react-router";
import type { RootState } from "../store/store";

const Dashboard = () => {
  const user = useSelector((state: RootState) => state.auth.user);

  if (user && !user.hasCompany) {
    return <Navigate to="/company/create" replace />;
  }

  return (
    <section className="mx-auto max-w-6xl px-6 py-8">
      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <p className="text-sm font-medium text-indigo-500">Dashboard</p>
        <h1 className="mt-2 text-2xl font-bold tracking-tight text-slate-950">
          Welcome{user?.name ? `, ${user.name}` : ""}
        </h1>
        <p className="mt-2 text-sm text-slate-500">Your messaging workspace is ready.</p>
      </div>
    </section>
  );
};

export default Dashboard;
