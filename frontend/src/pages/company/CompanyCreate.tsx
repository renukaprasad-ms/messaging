import { useState, type ChangeEvent, type FormEvent } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router";
import { getApiErrorMessage } from "../../service/authService";
import { companyService, type CompanyCreateRequest } from "../../service/companyService";
import { setUser } from "../../store/auth/authSlice";
import type { RootState } from "../../store/store";

const initialForm: CompanyCreateRequest = {
  name: "",
  displayName: "",
  legalName: "",
  website: "",
  businessEmail: "",
  businessPhone: "",
  industry: "",
  registrationNumber: "",
  taxId: "",
  addressLine1: "",
  addressLine2: "",
  city: "",
  state: "",
  postalCode: "",
  country: "",
};

const CompanyCreate = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector((state: RootState) => state.auth.user);
  const [form, setForm] = useState(initialForm);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const updateField = (field: keyof CompanyCreateRequest) => (event: ChangeEvent<HTMLInputElement>) => {
    setForm((current) => ({ ...current, [field]: event.target.value }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      await companyService.createCompany(form);
      if (user) {
        dispatch(setUser({ ...user, hasCompany: true }));
      }
      navigate("/", { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, "Unable to create company. Please try again."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="mx-auto max-w-5xl px-6 py-8">
      <div className="mb-8">
        <p className="text-sm font-medium text-indigo-500">Company</p>
        <h1 className="mt-2 text-2xl font-bold tracking-tight text-slate-950">Create company</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-8">
        <div className="grid gap-5 md:grid-cols-2">
          <Field label="Company name" value={form.name} onChange={updateField("name")} required />
          <Field label="Display name" value={form.displayName} onChange={updateField("displayName")} />
          <Field label="Legal name" value={form.legalName} onChange={updateField("legalName")} required />
          <Field label="Industry" value={form.industry} onChange={updateField("industry")} />
          <Field label="Website" value={form.website} onChange={updateField("website")} />
          <Field label="Business email" value={form.businessEmail} onChange={updateField("businessEmail")} />
          <Field label="Business phone" value={form.businessPhone} onChange={updateField("businessPhone")} />
          <Field label="Registration number" value={form.registrationNumber} onChange={updateField("registrationNumber")} />
          <Field label="Tax ID" value={form.taxId} onChange={updateField("taxId")} />
        </div>

        <div className="grid gap-5 md:grid-cols-2">
          <Field label="Address line 1" value={form.addressLine1} onChange={updateField("addressLine1")} required />
          <Field label="Address line 2" value={form.addressLine2} onChange={updateField("addressLine2")} />
          <Field label="City" value={form.city} onChange={updateField("city")} required />
          <Field label="State" value={form.state} onChange={updateField("state")} />
          <Field label="Postal code" value={form.postalCode} onChange={updateField("postalCode")} />
          <Field label="Country" value={form.country} onChange={updateField("country")} required />
        </div>

        {error ? <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600">{error}</p> : null}

        <div className="flex justify-end">
          <button type="submit" disabled={isSubmitting} className="h-11 rounded-lg bg-indigo-500 px-6 text-sm font-semibold text-white hover:bg-indigo-600 disabled:cursor-not-allowed disabled:bg-indigo-300">
            {isSubmitting ? "Creating..." : "Create company"}
          </button>
        </div>
      </form>
    </section>
  );
};

interface FieldProps {
  label: string
  value: string
  onChange: (event: ChangeEvent<HTMLInputElement>) => void
  required?: boolean
}

const Field = ({ label, value, onChange, required = false }: FieldProps) => (
  <label className="block">
    <span className="mb-2 block text-sm font-medium text-slate-800">{label}</span>
    <input
      value={value}
      onChange={onChange}
      required={required}
      className="h-11 w-full rounded-lg border border-slate-200 px-3 text-sm outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-50"
    />
  </label>
);

export default CompanyCreate;
