import { useState } from 'react'
import LoginForm from './LoginForm'
import RegisterForm from './RegisterForm'

// Login and registration are two distinct layouts (their own heading, fields, and copy),
// not one form relabeled by a mode flag — switching just swaps which is mounted.
export default function AuthScreen({ error, onLogin, onRegister }) {
  const [mode, setMode] = useState('login')

  return (
    <div className="auth-screen">
      <div className="auth-card">
        <p className="auth-tagline">Get notified the moment news, markets, or emergencies you're watching change.</p>

        {mode === 'login' ? (
          <LoginForm error={error} onSubmit={onLogin} />
        ) : (
          <RegisterForm error={error} onSubmit={onRegister} />
        )}

        <p className="auth-switch">
          {mode === 'login' ? (
            <>
              New here?{' '}
              <button type="button" onClick={() => setMode('register')}>
                Create an account
              </button>
            </>
          ) : (
            <>
              Already have an account?{' '}
              <button type="button" onClick={() => setMode('login')}>
                Log in
              </button>
            </>
          )}
        </p>
      </div>
    </div>
  )
}
