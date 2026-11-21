import { AlertTriangle, Loader2, SearchX } from 'lucide-react'

export function LoadingBlock({ label = 'Loading...' }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-2 rounded-2xl border border-sky-100 bg-white p-6 text-slate-600">
      <Loader2 className="h-5 w-5 animate-spin" />
      <span>{label}</span>
    </div>
  )
}

export function ErrorBlock({ message }: { message: string }) {
  return (
    <div className="flex items-start gap-3 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-rose-700">
      <AlertTriangle className="mt-0.5 h-5 w-5" />
      <p className="text-sm">{message}</p>
    </div>
  )
}

export function EmptyBlock({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 text-center">
      <SearchX className="mx-auto mb-2 h-6 w-6 text-slate-500" />
      <h3 className="text-base font-semibold text-slate-800">{title}</h3>
      {subtitle ? <p className="mt-1 text-sm text-slate-500">{subtitle}</p> : null}
    </div>
  )
}
