import { useEffect, useState } from 'react';
import { Wiretap } from './pages/Wiretap';
import { LoginPage } from './pages/LoginPage';
import { clearToken, getValidToken, getTokenExpiry, saveToken } from './auth';
import './styles/global.css';

function App() {
  const [jwt, setJwt] = useState<string | null>(() => getValidToken());

  useEffect(() => {
    const handleLogout = () => setJwt(null);
    window.addEventListener('wiretap:logout', handleLogout);
    return () => window.removeEventListener('wiretap:logout', handleLogout);
  }, []);

  useEffect(() => {
    if (!jwt) {
      return undefined;
    }

    const expiry = getTokenExpiry(jwt);
    if (expiry === null) {
      return undefined;
    }

    const remainingMs = expiry - Date.now();
    if (remainingMs <= 0) {
      clearToken();
      setJwt(null);
      return undefined;
    }

    const timeoutId = window.setTimeout(() => {
      clearToken();
      setJwt(null);
    }, remainingMs);

    return () => {
      window.clearTimeout(timeoutId);
    };
  }, [jwt]);

  const handleLogin = (token: string) => {
    saveToken(token);
    setJwt(token);
  };

  const handleLogout = () => {
    clearToken();
    setJwt(null);
  };

  return jwt ? <Wiretap onLogout={handleLogout} /> : <LoginPage onLogin={handleLogin} />;
}

export default App;
