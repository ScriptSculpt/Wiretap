const STORAGE_KEY = 'wiretap_jwt_token';

const decodeJwtPayload = (token: string): Record<string, unknown> | null => {
  try {
    // const [, payload] = token.split('.');
    // const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    // const json = decodeURIComponent(atob(base64).split('').map((c) => {
    //   return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    // }).join(''));
    // return JSON.parse(json) as Record<string, unknown>;

    const payload = token.split('.')[1];

    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const binary = atob(base64);
    const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
    const json = new TextDecoder().decode(bytes);
    
    return JSON.parse(json) as Record<string, unknown>;

  } catch {
    return null;
  }
};

export const getToken = (): string | null => {
  return window.localStorage.getItem(STORAGE_KEY);
};

export const getTokenExpiry = (token: string): number | null => {
  const payload = decodeJwtPayload(token);
  if (!payload || typeof payload.exp !== 'number') {
    return null;
  }

  return payload.exp * 1000;
};

export const isTokenExpired = (token: string): boolean => {
  const expiry = getTokenExpiry(token);
  return expiry === null ? false : expiry <= Date.now();
};

export const getValidToken = (): string | null => {
  const token = getToken();
  if (!token) {
    return null;
  }

  if (isTokenExpired(token)) {
    clearToken();
    return null;
  }

  return token;
};

export const saveToken = (token: string): void => {
  window.localStorage.setItem(STORAGE_KEY, token);
};

export const clearToken = (): void => {
  window.localStorage.removeItem(STORAGE_KEY);
  window.dispatchEvent(new Event('wiretap:logout'));
};

export const isAuthenticated = (): boolean => Boolean(getValidToken());
