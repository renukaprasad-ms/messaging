import { Link } from "react-router";

const fields = [
  ["Full name", "text", "Your name"],
  ["Email address", "email", "you@example.com"],
  ["Phone number", "tel", "+91 98765 43210"],
  ["Password", "password", "Create a password"],
  ["Confirm password", "password", "Repeat your password"],
];

const Register = () => (
  <div>
    <h1 className="text-3xl font-bold tracking-tight text-slate-950">Create your account</h1>
    <p className="mt-2 text-sm leading-6 text-slate-500">Start managing customer conversations and campaigns from one place.</p>
    <form className="mt-7 space-y-4">
      {fields.map(([label, type, placeholder]) => <label key={label} className="block"><span className="mb-2 block text-sm font-medium text-slate-800">{label}</span><input type={type} placeholder={placeholder} className="h-11 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50" /></label>)}
      <button className="mt-2 h-12 w-full rounded-xl bg-indigo-500 text-sm font-semibold text-white shadow-lg shadow-indigo-100 hover:bg-indigo-600">Create account</button>
    </form>
    <p className="mt-6 text-center text-xs text-slate-500">Already have an account? <Link to="/login" className="font-semibold text-indigo-500">Sign in</Link></p>
  </div>
);
export default Register;
