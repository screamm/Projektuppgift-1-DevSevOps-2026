import Link from "next/link";

export default function SignInPage() {
  return (
    <div className='flex min-h-full flex-1 items-center justify-center bg-zinc-50 px-4 py-12 font-sans dark:bg-zinc-950'>
      <div className='w-full max-w-md text-center'>
        <h1 className='text-3xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-50'>
          Sign in
        </h1>
        <div className='space-y-2'>
          <label
            htmlFor='email'
            className='block text-sm font-medium text-zinc-700 dark:text-zinc-300'
          >
            Email
          </label>
          <input
            id='email'
            name='email'
            type='email'
            autoComplete='email'
            placeholder='you@example.com'
            required
            className='block w-full rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2.5 text-zinc-900 placeholder:text-zinc-400 transition-colors outline-none focus:border-zinc-400 focus:bg-white focus:ring-2 focus:ring-zinc-900/10 dark:border-zinc-700 dark:bg-zinc-800/50 dark:text-zinc-50 dark:placeholder:text-zinc-500 dark:focus:border-zinc-500 dark:focus:bg-zinc-800 dark:focus:ring-zinc-50/10'
          />
        </div>

        <div className='space-y-2'>
          <label
            htmlFor='password'
            className='block text-sm font-medium text-zinc-700 dark:text-zinc-300'
          >
            Password
          </label>
          <input
            id='password'
            name='password'
            type='password'
            autoComplete='new-password'
            placeholder='••••••••'
            required
            minLength={8}
            className='block w-full rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2.5 text-zinc-900 placeholder:text-zinc-400 transition-colors outline-none focus:border-zinc-400 focus:bg-white focus:ring-2 focus:ring-zinc-900/10 dark:border-zinc-700 dark:bg-zinc-800/50 dark:text-zinc-50 dark:placeholder:text-zinc-500 dark:focus:border-zinc-500 dark:focus:bg-zinc-800 dark:focus:ring-zinc-50/10'
          />
          <p className='text-xs text-zinc-500 dark:text-zinc-500'>
            At least 8 characters.
          </p>
        </div>
        <p className='mt-6 text-sm text-zinc-600 dark:text-zinc-400'>
          Don&apos;t have an account?{' '}
          <Link
            href='/sign-up'
            className='font-medium text-zinc-900 underline-offset-4 hover:underline dark:text-zinc-50'
          >
            Sign up
          </Link>
        </p>
      </div>
    </div>
  )
}
