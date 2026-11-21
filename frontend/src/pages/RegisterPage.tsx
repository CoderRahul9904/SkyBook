import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { register as registerRequest } from '../services/api/authApi'
import { useAuth } from '../app/AuthContext'
import { getApiErrorMessage } from '../services/api/client'
import { useState } from 'react'
import { ErrorBlock } from '../components/common/Feedback'

const passwordRules = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/

const registerSchema = z
  .object({
    fullName: z.string().min(1, 'Full name is required').max(120),
    email: z.email('Enter a valid email address').max(320),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .max(255)
      .regex(passwordRules, 'Password must include uppercase, lowercase, number and symbol'),
    confirmPassword: z.string().min(1, 'Confirm your password'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    path: ['confirmPassword'],
    message: 'Passwords do not match',
  })

type RegisterForm = z.infer<typeof registerSchema>

export function RegisterPage() {
  const navigate = useNavigate()
  const { saveAuth } = useAuth()
  const [error, setError] = useState<string>('')

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      fullName: '',
      email: '',
      password: '',
      confirmPassword: '',
    },
  })

  async function onSubmit(values: RegisterForm) {
    setError('')
    try {
      const auth = await registerRequest({
        fullName: values.fullName.trim(),
        email: values.email.trim(),
        password: values.password,
      })
      saveAuth(auth)
      navigate('/flights', { replace: true })
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  return (
    <section className="mx-auto max-w-lg rounded-3xl border border-sky-100 bg-white p-8 shadow-sm">
      <h1 className="text-2xl font-black text-slate-900">Create your SkyBook account</h1>
      <p className="mt-1 text-sm text-slate-600">Register once and start booking flights instantly.</p>

      <form className="mt-6 space-y-4" onSubmit={handleSubmit(onSubmit)}>
        {error ? <ErrorBlock message={error} /> : null}

        <label className="block space-y-1">
          <span className="text-sm font-semibold text-slate-700">Full Name</span>
          <input className="w-full rounded-xl border border-slate-300 px-3 py-2 outline-none focus:border-sky-500" {...register('fullName')} />
          {errors.fullName ? <span className="text-xs text-rose-600">{errors.fullName.message}</span> : null}
        </label>

        <label className="block space-y-1">
          <span className="text-sm font-semibold text-slate-700">Email</span>
          <input type="email" className="w-full rounded-xl border border-slate-300 px-3 py-2 outline-none focus:border-sky-500" {...register('email')} />
          {errors.email ? <span className="text-xs text-rose-600">{errors.email.message}</span> : null}
        </label>

        <label className="block space-y-1">
          <span className="text-sm font-semibold text-slate-700">Password</span>
          <input type="password" className="w-full rounded-xl border border-slate-300 px-3 py-2 outline-none focus:border-sky-500" {...register('password')} />
          {errors.password ? <span className="text-xs text-rose-600">{errors.password.message}</span> : null}
        </label>

        <label className="block space-y-1">
          <span className="text-sm font-semibold text-slate-700">Confirm Password</span>
          <input
            type="password"
            className="w-full rounded-xl border border-slate-300 px-3 py-2 outline-none focus:border-sky-500"
            {...register('confirmPassword')}
          />
          {errors.confirmPassword ? <span className="text-xs text-rose-600">{errors.confirmPassword.message}</span> : null}
        </label>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-xl bg-sky-700 px-4 py-2.5 font-semibold text-white hover:bg-sky-600 disabled:opacity-70"
        >
          {isSubmitting ? 'Creating account...' : 'Create account'}
        </button>
      </form>

      <p className="mt-4 text-sm text-slate-600">
        Already have an account?{' '}
        <Link to="/login" className="font-semibold text-sky-700 hover:text-sky-500">
          Login
        </Link>
      </p>
    </section>
  )
}
