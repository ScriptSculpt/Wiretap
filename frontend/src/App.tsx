import { useEffect, useState } from 'react';
import { Wiretap } from './pages/Wiretap';
import { LoginPage } from './components/LoginPage';
import { clearToken, getToken, saveToken } from './auth';
import './styles/global.css';

function App() {
  const [jwt, setJwt] = useState<string | null>(() => getToken());

  useEffect(() => {
    const handleLogout = () => setJwt(null);
    window.addEventListener('wiretap:logout', handleLogout);
    return () => window.removeEventListener('wiretap:logout', handleLogout);
  }, []);

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
