export default function PlaceholderPage({ title }: { title: string }) {
  return (
    <section className="space-y-3 p-5 lg:p-7">
      <p className="eyebrow">WORKSPACE</p>
      <h1 className="text-2xl font-bold tracking-tight">{title}</h1>
      <div className="panel p-6">
        <p className="text-sm text-slate-500">This section is ready for the next feature slice.</p>
      </div>
    </section>
  )
}
