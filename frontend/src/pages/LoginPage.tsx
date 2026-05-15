import { useState, type ChangeEvent, type FormEvent } from 'react';
import type { AuthRequest } from '../types';
import { login, register } from '../api/auth';
import logo from '../images/image.png';
import './LoginPage.css';

interface LoginPageProps {
  onLogin: (token: string) => void;
}

export const LoginPage = ({ onLogin }: LoginPageProps) => {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [form, setForm] = useState<AuthRequest>({ username: '', password: '' });
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const handleChange = (field: keyof AuthRequest) => (event: ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [field]: event.target.value });
  };

  const handleSubmit = async (event: React.SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSuccess(null);
    setIsLoading(true);

    try {
      if (mode === 'login') {
        const response = await login(form);
        onLogin(response.token);
      } else {
        if (form.password !== confirmPassword) {
          setError('Passwords do not match.');
          throw new Error('Passwords do not match.');
        }
        if (form.password.length < 6) {
          setError('Password must be at least 6 characters.');
          throw new Error('Password must be at least 6 characters.');
        }
        await register(form);
        setSuccess('Registration successful! You can now log in.');
        setForm({ username: '', password: '' });
        setConfirmPassword('');
        setTimeout(() => {
          setMode('login');
          setSuccess(null);
          setError(null);
        }, 2000);
      }
    } catch (err) {
      mode === 'login' && setError('Login failed. Check your credentials and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <img src={logo} alt="Wiretap logo" className="auth-page__logo" />
      <h1 className="auth-page__title">Wiretap</h1>
      <div className="auth-card">
        <div className="auth-card__header">
          <div>
            <h1 className="auth-card__title">Welcome Back</h1>
            <p className="auth-card__subtitle">Sign in to continue</p>
          </div>
        </div>

        <div className="auth-tabs">
          <button
            type="button"
            className={`auth-tab ${mode === 'login' ? 'auth-tab--active' : ''}`}
            onClick={() => {
              setMode('login');
              setError(null);
              setSuccess(null);
            }}
            disabled={isLoading}
          >
            Sign In
          </button>
          <button
            type="button"
            className={`auth-tab ${mode === 'register' ? 'auth-tab--active' : ''}`}
            onClick={() => {
              setMode('register');
              setError(null);
              setSuccess(null);
            }}
            disabled={isLoading}
          >
            Create Account
          </button>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label className="auth-label">
            Username
            <input
              type="text"
              value={form.username}
              onChange={handleChange('username')}
              className="auth-input"
              placeholder="Enter username"
              disabled={isLoading}
              required
              autoComplete="username"
            />
          </label>

          <label className="auth-label">
            Password
            <input
              type="password"
              value={form.password}
              onChange={handleChange('password')}
              className="auth-input"
              placeholder="Enter password"
              disabled={isLoading}
              required
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            />
          </label>

          {mode === 'register' && (
            <label className="auth-label">
              Confirm Password
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="auth-input"
                placeholder="Confirm password"
                disabled={isLoading}
                required
                autoComplete="new-password"
              />
            </label>
          )}

          {error && <div className="auth-error">{error}</div>}
          {success && <div className="auth-success">{success}</div>}

          <button className="auth-button" type="submit" disabled={isLoading}>
            {isLoading ? (mode === 'login' ? 'Signing in…' : 'Creating account…') : (mode === 'login' ? 'Sign in' : 'Create account')}
          </button>
        </form>
      </div>
    </div>
  );
};
