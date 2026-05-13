const STORAGE_KEY = 'wiretap_jwt_token';

export const getToken = (): string | null => {
  return window.localStorage.getItem(STORAGE_KEY);
};

export const saveToken = (token: string): void => {
  window.localStorage.setItem(STORAGE_KEY, token);
};

export const clearToken = (): void => {
  window.localStorage.removeItem(STORAGE_KEY);
  window.dispatchEvent(new Event('wiretap:logout'));
};

export const isAuthenticated = (): boolean => Boolean(getToken());
