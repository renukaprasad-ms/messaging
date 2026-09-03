import { Outlet } from "react-router";
import { FiBarChart2, FiLayers, FiMessageCircle, FiShield, FiZap } from "react-icons/fi";

const capabilities = [
  { icon: FiMessageCircle, title: "Unified Inbox", description: "WhatsApp, Email and SMS conversations in one place." },
  { icon: FiZap, title: "Smart Broadcasts", description: "Reach thousands of customers with targeted campaigns." },
  { icon: FiBarChart2, title: "Advertising Insights", description: "Meta, Google, Hotstar and more ad channels." },
  { icon: FiLayers, title: "Automations", description: "Create workflows that reduce repetitive work." },
];

const AuthLayout = () => {
  return (
    <main className="min-h-screen bg-white lg:grid lg:grid-cols-[48%_52%]">
      <section className="relative hidden min-h-screen overflow-hidden border-r border-slate-100 bg-gradient-to-br from-indigo-50 via-white to-violet-50 px-12 py-10 lg:flex lg:flex-col">
        <div className="absolute -left-24 top-52 h-72 w-72 rounded-full bg-indigo-200/20 blur-3xl" />
        <div className="absolute -right-24 bottom-20 h-80 w-80 rounded-full bg-violet-200/30 blur-3xl" />

        <div className="relative z-10 flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-500 text-lg font-bold text-white shadow-lg shadow-indigo-200">•••</div>
          <span className="text-2xl font-bold tracking-tight text-slate-900">Messaging</span>
        </div>

        <div className="relative z-10 my-auto max-w-xl py-12">
          <h1 className="text-4xl font-bold leading-tight tracking-tight text-slate-950 xl:text-5xl">
            Connect every conversation.<br />Grow from one place.
          </h1>
          <p className="mt-5 max-w-lg text-base leading-7 text-slate-500">
            The all-in-one platform for messaging, broadcasts, automation and advertising.
          </p>

          <div className="mt-7 flex flex-wrap gap-2.5 text-xs font-semibold">
            <span className="rounded-full bg-emerald-50 px-4 py-2 text-emerald-600">WhatsApp</span>
            <span className="rounded-full bg-indigo-50 px-4 py-2 text-indigo-600">Email</span>
            <span className="rounded-full bg-amber-50 px-4 py-2 text-amber-600">SMS</span>
            <span className="rounded-full bg-blue-50 px-4 py-2 text-blue-600">Ads</span>
          </div>

          <div className="mt-9 grid gap-5">
            {capabilities.map(({ icon: Icon, title, description }) => (
              <div key={title} className="flex items-center gap-4">
                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-white text-indigo-500 shadow-sm ring-1 ring-slate-100">
                  <Icon size={19} />
                </div>
                <div>
                  <h2 className="text-sm font-semibold text-slate-900">{title}</h2>
                  <p className="mt-1 text-xs leading-5 text-slate-500">{description}</p>
                </div>
              </div>
            ))}
          </div>

          <div className="ml-auto mt-9 w-64 rounded-2xl border border-slate-100 bg-white p-5 shadow-xl shadow-slate-200/50">
            <p className="text-sm font-semibold text-slate-900">Dashboard</p>
            <div className="mt-5 grid grid-cols-2 gap-4">
              <div><strong className="block text-xl text-slate-950">24.8K</strong><span className="text-[11px] text-slate-400">Conversations</span></div>
              <div><strong className="block text-xl text-slate-950">112K</strong><span className="text-[11px] text-slate-400">Messages</span></div>
            </div>
            <div className="mt-5 h-2 overflow-hidden rounded-full bg-indigo-50"><div className="h-full w-[70%] rounded-full bg-indigo-500" /></div>
            <p className="mt-4 text-[11px] font-medium text-indigo-500">Messaging + marketing, together</p>
          </div>
        </div>

        <div className="relative z-10 flex items-center gap-3 text-slate-500">
          <FiShield className="text-indigo-500" size={22} />
          <div><p className="text-xs font-semibold text-slate-800">Secure by design</p><p className="mt-0.5 text-[11px]">Modern authentication protects your workspace.</p></div>
        </div>
      </section>

      <section className="flex min-h-screen items-center justify-center bg-white px-6 py-12 sm:px-10">
        <div className="w-full max-w-[390px]">
          <div className="mb-10 flex items-center gap-3 lg:hidden">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-indigo-500 text-sm font-bold text-white">•••</div>
            <span className="text-xl font-bold text-slate-900">Messaging</span>
          </div>
          <Outlet />
        </div>
      </section>
    </main>
  );
};

export default AuthLayout;
