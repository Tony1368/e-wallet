import HttpService from './HttpService';

// User Sessions
const getAllUserSessions = (page = 0, size = 10) => 
  HttpService.getWithAuth(`/admin/tracking/sessions?page=${page}&size=${size}`);

const getUserSessionsByUserId = (userId) => 
  HttpService.getWithAuth(`/admin/tracking/sessions/users/${userId}`);

const getUserSessionsByUsername = (username) => 
  HttpService.getWithAuth(`/admin/tracking/sessions/username/${username}`);

const getActiveUserSessions = (page = 0, size = 10) => 
  HttpService.getWithAuth(`/admin/tracking/sessions/active?page=${page}&size=${size}`);

const getUserSessionsByDateRange = (userId, startTime, endTime) => 
  HttpService.getWithAuth(`/admin/tracking/sessions/users/${userId}/date-range?startTime=${startTime}&endTime=${endTime}`);

// User Activities
const getAllUserActivities = (page = 0, size = 10) => 
  HttpService.getWithAuth(`/admin/tracking/activities?page=${page}&size=${size}`);

const getUserActivitiesByUserId = (userId, page = 0, size = 10) => 
  HttpService.getWithAuth(`/admin/tracking/activities/users/${userId}?page=${page}&size=${size}`);

const getUserActivitiesByUsername = (username, page = 0, size = 10) => 
  HttpService.getWithAuth(`/admin/tracking/activities/username/${username}?page=${page}&size=${size}`);

const getFinancialActivities = (page = 0, size = 10) => 
  HttpService.getWithAuth(`/admin/tracking/activities/financial?page=${page}&size=${size}`);

const getFinancialActivitiesByUserId = (userId, page = 0, size = 10) => 
  HttpService.getWithAuth(`/admin/tracking/activities/financial/users/${userId}?page=${page}&size=${size}`);

const getUserActivitiesByDateRange = (userId, startTime, endTime) => 
  HttpService.getWithAuth(`/admin/tracking/activities/users/${userId}/date-range?startTime=${startTime}&endTime=${endTime}`);

// Specific Activity Types
const getLoginActivities = (userId) => 
  HttpService.getWithAuth(`/admin/tracking/activities/users/${userId}/login`);

const getTransferActivities = (userId) => 
  HttpService.getWithAuth(`/admin/tracking/activities/users/${userId}/transfer`);

const getWithdrawalActivities = (userId) => 
  HttpService.getWithAuth(`/admin/tracking/activities/users/${userId}/withdraw`);

const getAddFundsActivities = (userId) => 
  HttpService.getWithAuth(`/admin/tracking/activities/users/${userId}/add-funds`);

const TrackingService = {
  // Sessions
  getAllUserSessions,
  getUserSessionsByUserId,
  getUserSessionsByUsername,
  getActiveUserSessions,
  getUserSessionsByDateRange,
  
  // Activities
  getAllUserActivities,
  getUserActivitiesByUserId,
  getUserActivitiesByUsername,
  getFinancialActivities,
  getFinancialActivitiesByUserId,
  getUserActivitiesByDateRange,
  
  // Specific Activities
  getLoginActivities,
  getTransferActivities,
  getWithdrawalActivities,
  getAddFundsActivities,
};

export default TrackingService; 