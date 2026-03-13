import React, { useEffect, useRef } from 'react';
import { Route, Routes, useNavigate, useLocation } from 'react-router-dom';
import DashboardLayout from './layouts/dashboard/DashboardLayout';
import Login from './pages/auth/Login';
import Signup from './pages/auth/Signup';
import Unauthorized from './pages/auth/Unauthorized';
import Dashboard from './pages/dashboard/Dashboard';
import Transaction from './pages/transaction/Transaction';
import BasicTabs from './pages/transfer/BasicTabs';
import AddFunds from './pages/wallet/AddFunds';
import NewWallet from './pages/wallet/NewWallet';
import Wallet from './pages/wallet/Wallet';
import PrivateRoute from './PrivateRoute';
import ProtectedRoute from './ProtectedRoute';
import AdminTransaction from './pages/admin/AdminTransaction';
import AdminWallet from './pages/admin/AdminWallet';
import UserTracking from './pages/admin/UserTracking';
import FraudConfig from './pages/admin/FraudConfig';
import AuthService, { decodeJwt } from './services/AuthService';

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const inactivityTimeoutRef = useRef();
  const tokenCheckIntervalRef = useRef();

  useEffect(() => {
    const handleLogout = () => {
      // Clear timers to prevent multiple triggers
      if (inactivityTimeoutRef.current) clearTimeout(inactivityTimeoutRef.current);
      if (tokenCheckIntervalRef.current) clearInterval(tokenCheckIntervalRef.current);
      // eslint-disable-next-line no-use-before-define
      events.forEach((event) => window.removeEventListener(event, resetInactivityTimer));

      AuthService.logout().then(() => {
        navigate('/login', { replace: true });
      });
    };

    const resetInactivityTimer = () => {
      if (inactivityTimeoutRef.current) clearTimeout(inactivityTimeoutRef.current);
      inactivityTimeoutRef.current = setTimeout(handleLogout, 15 * 60 * 1000); // 15 minutes
    };

    const checkTokenExpiry = () => {
      const user = AuthService.getCurrentUser();
      if (user?.token) {
        const payload = decodeJwt(user.token);
        if (payload?.exp && Date.now() / 1000 > payload.exp) {
          handleLogout();
        }
      }
    };

    const events = ['mousemove', 'mousedown', 'keydown', 'touchstart', 'scroll'];

    // Only run timers if user is logged in
    if (AuthService.getCurrentUser()) {
      events.forEach((event) => window.addEventListener(event, resetInactivityTimer));
      resetInactivityTimer();

      tokenCheckIntervalRef.current = setInterval(checkTokenExpiry, 10000); // check every 10s
      checkTokenExpiry();
    }

    // Cleanup function to remove listeners and timers
    return () => {
      if (inactivityTimeoutRef.current) clearTimeout(inactivityTimeoutRef.current);
      if (tokenCheckIntervalRef.current) clearInterval(tokenCheckIntervalRef.current);
      events.forEach((event) => window.removeEventListener(event, resetInactivityTimer));
    };
  }, [location.pathname, navigate]); // Rerun effect on navigation

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />

      <Route path="unauthorized" element={<PrivateRoute />}>
        <Route index element={<Unauthorized />} />
      </Route>

      <Route path="/" element={<PrivateRoute />}>
        <Route path="" element={<DashboardLayout />}>
          <Route path="" element={<Dashboard />}>
            <Route element={<ProtectedRoute roles={['ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER']} />}>
              <Route index element={<Dashboard />} />
            </Route>
          </Route>

          <Route path="wallets" element={<PrivateRoute />}>
            <Route element={<ProtectedRoute roles={['ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER']} />}>
              <Route index element={<Wallet />} />
            </Route>
            <Route element={<ProtectedRoute roles={['ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER']} />}>
              <Route path="new" element={<NewWallet />} />
            </Route>
            <Route element={<ProtectedRoute roles={['ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER']} />}>
              <Route path="addFunds" element={<AddFunds />} />
            </Route>
          </Route>

          <Route path="transfers" element={<PrivateRoute />}>
            <Route element={<ProtectedRoute roles={['ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER']} />}>
              <Route index element={<BasicTabs />} />
            </Route>
          </Route>

          <Route path="transactions" element={<PrivateRoute />}>
            <Route element={<ProtectedRoute roles={['ROLE_USER', 'ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_CUSTOMER']} />}>
              <Route index element={<Transaction />} />
            </Route>
          </Route>

          <Route path="admin" element={<PrivateRoute />}>
            <Route path="transactions" element={<ProtectedRoute roles={['ROLE_ADMIN', 'ROLE_ACCOUNTANT']} />}>
              <Route index element={<AdminTransaction />} />
            </Route>
            <Route path="wallets" element={<ProtectedRoute roles={['ROLE_ADMIN', 'ROLE_ACCOUNTANT']} />}>
              <Route index element={<AdminWallet />} />
            </Route>
            <Route path="tracking" element={<ProtectedRoute roles={['ROLE_ADMIN']} />}>
              <Route index element={<UserTracking />} />
            </Route>
            <Route path="fraud-config" element={<ProtectedRoute roles={['ROLE_ADMIN']} />}>
              <Route index element={<FraudConfig />} />
            </Route>
          </Route>
        </Route>
      </Route>
    </Routes>
  );
}
