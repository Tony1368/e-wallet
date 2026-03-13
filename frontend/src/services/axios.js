import axios from "axios";

// const instance = axios.create({ baseURL: "https://localhost:8243" });
const instance = axios.create({ baseURL: "http://localhost:8080/api/v1" });
instance.defaults.headers.common["Content-Type"] = "application/json";

// Add request interceptor to include JWT token
instance.interceptors.request.use(
  (config) => {
    const user = JSON.parse(localStorage.getItem('user'));
    console.log('Axios interceptor - user from localStorage:', user);
    console.log('Axios interceptor - token:', user?.token);
    
    if (user && user.token) {
      config.headers.Authorization = `Bearer ${user.token}`;
      console.log('Axios interceptor - Authorization header set:', config.headers.Authorization);
    } else {
      console.log('Axios interceptor - No token found, request will be unauthenticated');
    }
    
    console.log('Axios interceptor - Request URL:', config.url);
    console.log('Axios interceptor - Request headers:', config.headers);
    
    return config;
  },
  (error) => {
    console.error('Axios interceptor - Request error:', error);
    return Promise.reject(error);
  }
);

export default instance;