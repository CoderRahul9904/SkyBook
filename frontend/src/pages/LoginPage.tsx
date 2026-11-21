import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { login } from '../services/api/authApi'
import { useAuth } from '../app/AuthContext'
import { getApiErrorMessage } from '../services/api/client'
import { useState } from 'react'
import { ErrorBlock } from '../components/common/Feedback'

const loginSchema = z.object({
  email: z.email('Enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
})

type LoginForm = z.infer<typeof loginSchema>

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { saveAuth } = useAuth()
  const [error, setError] = useState<string>('')

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })

  async function onSubmit(values: LoginForm) {
    setError('')
    try {
      const auth = await login(values)
      saveAuth(auth)
      const redirectPath = (location.state as { from?: string } | undefined)?.from ?? '/flights'
      navigate(redirectPath, { replace: true })
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  return (
    <section className="mx-auto max-w-lg rounded-3xl border border-sky-100 bg-white p-8 shadow-sm">
      <h1 className="text-2xl font-black text-slate-900">Welcome back</h1>
      <p className="mt-1 text-sm text-slate-600">Sign in to continue booking and managing your flights.</p>

      <form className="mt-6 space-y-4" onSubmit={handleSubmit(onSubmit)}>
        {error ? <ErrorBlock message={error} /> : null}

        <label className="block space-y-1">
          <span className="text-sm font-semibold text-slate-700">Email</span>
          <input
            className="w-full rounded-xl border border-slate-300 px-3 py-2 outline-none transition focus:border-sky-500"
            type="email"
            {...register('email')}
          />
          {errors.email ? <span className="text-xs text-rose-600">{errors.email.message}</span> : null}
        </label>

        <label className="block space-y-1">
          <span className="text-sm font-semibold text-slate-700">Password</span>
          <input
            className="w-full rounded-xl border border-slate-300 px-3 py-2 outline-none transition focus:border-sky-500"
            type="password"
            {...register('password')}
          />
          {errors.password ? <span className="text-xs text-rose-600">{errors.password.message}</span> : null}
        </label>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-xl bg-sky-700 px-4 py-2.5 font-semibold text-white hover:bg-sky-600 disabled:opacity-70"
        >
          {isSubmitting ? 'Signing in...' : 'Sign in'}
        </button>
      </form>

      <p className="mt-4 text-sm text-slate-600">
        Need an account?{' '}
        <Link to="/register" className="font-semibold text-sky-700 hover:text-sky-500">
          Register now
        </Link>
      </p>
    </section>
  )
}
