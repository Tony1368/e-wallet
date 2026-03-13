import HttpService from './HttpService';
import axios from './axios';

const login = (body) => {
  const url = '/auth/login';
//  const url = '/authen/hust/auth/login';
  return axios.post(url, body).then((response) => {
    localStorage.setItem('user', JSON.stringify(response.data));
    return response.data;
  });
};

const signup = (body) => {
  const url = '/auth/signup';
  return axios.post(url, body).then((response) => response.data);
};

const logout = () => {
  // Use postWithAuth to ensure Authorization header is sent
  return HttpService.postWithAuth('/auth/logout', {})
    .then(() => {
      localStorage.removeItem('user');
    })
    .catch((error) => {
      console.error('Error during logout:', error);
      localStorage.removeItem('user');
    });
};

const expireSession = () => {
  return HttpService.postWithAuth('/auth/session/expire', {});
};

const getCurrentUser = () => JSON.parse(localStorage.getItem('user'));

const isAdmin = () => {
  const user = getCurrentUser();
  if (!user || !user.roles) return false;
  
  return user.roles.some(role => {
    // Handle both string format and object format
    const roleName = typeof role === 'string' ? role : role.name;
    return roleName === 'ROLE_ADMIN';
  });
};

const isAccountant = () => {
  const user = getCurrentUser();
  if (!user || !user.roles) return false;
  
  return user.roles.some(role => {
    // Handle both string format and object format
    const roleName = typeof role === 'string' ? role : role.name;
    return roleName === 'ROLE_ACCOUNTANT';
  });
};

const isCustomer = () => {
  const user = getCurrentUser();
  if (!user || !user.roles) return false;
  
  return user.roles.some(role => {
    // Handle both string format and object format
    const roleName = typeof role === 'string' ? role : role.name;
    return roleName === 'ROLE_CUSTOMER';
  });
};

const isAdminOrAccountant = () => {
  return isAdmin() || isAccountant();
};

const isAdminOrAccountantOrCustomer = () => {
  return isAdmin() || isAccountant() || isCustomer();
};

// Utility to decode JWT token and get payload
export function decodeJwt(token) {
  if (!token) return null;
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => `%${('00' + c.charCodeAt(0).toString(16)).slice(-2)}`)
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
}

const AuthService = {
  login,
  signup,
  logout,
  expireSession,
  getCurrentUser,
  isAdmin,
  isAccountant,
  isCustomer,
  isAdminOrAccountant,
  isAdminOrAccountantOrCustomer,
};

export default AuthService;
