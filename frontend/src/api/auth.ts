import type { AuthRequest, AuthResponse } from '../types';
import { fetchWithAuth } from './client';

export const login = async (credentials: AuthRequest): Promise<AuthResponse> => {
  const response = await fetchWithAuth('/auth/login', {
    method: 'POST',
    body: credentials,
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || 'Login failed. Check your credentials and try again.');
  }

  return response.json();
};

export const register = async (credentials: AuthRequest): Promise<string> => {
  const response = await fetchWithAuth('/auth/register', {
    method: 'POST',
    body: credentials,
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || 'Registration failed. Please try again.');
  }

  return response.text();
};
