import { useState, useEffect } from 'react';
import {
  Card,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Tabs,
  Tab,
  Box,
  Typography,
  Chip,
  Pagination,
  Stack,
  TextField,
  Button,
  Grid,
} from '@mui/material';
import { Helmet } from 'react-helmet-async';
import { useSnackbar } from 'notistack';
import TrackingService from '../../services/TrackingService';
import { fCurrency } from '../../utils/formatNumber';
import FraudConfig from './FraudConfig';
import AuthService from '../../services/AuthService';

function TabPanel({ children, value, index }) {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`tracking-tabpanel-${index}`}
      aria-labelledby={`tracking-tab-${index}`}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

export default function UserTracking() {
  const [tabValue, setTabValue] = useState(0);
  const [sessions, setSessions] = useState([]);
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [username, setUsername] = useState('');
  const { enqueueSnackbar } = useSnackbar();
  const user = AuthService.getCurrentUser();
  const isAdmin = user && (user.roles?.some(role => role.name === 'ROLE_ADMIN' || role === 'admin'));

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
    setPage(0);
    loadData(newValue, 0);
  };

  const loadData = async (tab, currentPage) => {
    setLoading(true);
    try {
      if (tab === 0) {
        // Load sessions
        const response = await TrackingService.getAllUserSessions(currentPage, 10);
        console.log('Sessions response:', response);
        setSessions(response.content || []);
        setTotalPages(response.totalPages || 0);
      } else {
        // Load activities
        const response = await TrackingService.getAllUserActivities(currentPage, 10);
        console.log('Activities response:', response);
        setActivities(response.content || []);
        setTotalPages(response.totalPages || 0);
      }
    } catch (error) {
      console.error('Error loading tracking data:', error);
      console.error('Error details:', error.response?.data);
      enqueueSnackbar(`Error loading tracking data: ${error.message}`, { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (event, value) => {
    setPage(value - 1);
    loadData(tabValue, value - 1);
  };

  const handleUsernameSearch = () => {
    if (username.trim()) {
      loadUserSpecificData();
    } else {
      loadData(tabValue, page);
    }
  };

  const loadUserSpecificData = async () => {
    setLoading(true);
    try {
      if (tabValue === 0) {
        const response = await TrackingService.getUserSessionsByUsername(username);
        console.log('User sessions response:', response);
        setSessions(response || []);
        setTotalPages(0);
      } else {
        const response = await TrackingService.getUserActivitiesByUsername(username, 0, 10);
        console.log('User activities response:', response);
        setActivities(response.content || []);
        setTotalPages(response.totalPages || 0);
      }
    } catch (error) {
      console.error('Error loading user data:', error);
      console.error('Error details:', error.response?.data);
      enqueueSnackbar(`Error loading user data: ${error.message}`, { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData(tabValue, page);
  }, []);

  const getActivityTypeColor = (type) => {
    switch (type) {
      case 'LOGIN':
        return 'primary';
      case 'LOGOUT':
        return 'secondary';
      case 'TRANSFER':
        return 'success';
      case 'WITHDRAW':
        return 'warning';
      case 'ADD_FUNDS':
        return 'info';
      default:
        return 'default';
    }
  };

  const getStatusColor = (isSuccessful) => {
    return isSuccessful ? 'success' : 'error';
  };

  const getSessionStatusChip = (session) => {
    let statusLabel;
    let color;
    // Determine status based on the `status` field, with a fallback to `isActive`
    const sessionStatus = session.status || (session.isActive ? 'ACTIVE' : 'INACTIVE');

    switch (sessionStatus) {
      case 'ACTIVE':
        statusLabel = 'Active';
        color = 'success';
        break;
      case 'INACTIVE':
        statusLabel = 'Inactive';
        color = 'default';
        break;
      case 'EXPIRED':
        statusLabel = 'Expired';
        color = 'error';
        break;
      default:
        statusLabel = 'Unknown';
        color = 'default';
    }

    return <Chip label={statusLabel} color={color} size="small" />;
  };

  const renderSessionsTable = () => (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Session ID</TableCell>
            <TableCell>User</TableCell>
            <TableCell>Login Time</TableCell>
            <TableCell>Logout Time</TableCell>
            <TableCell>IP Address</TableCell>
            <TableCell>Device</TableCell>
            <TableCell>Browser</TableCell>
            <TableCell>Location</TableCell>
            <TableCell>Status</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {sessions.map((session) => (
            <TableRow key={session.id}>
              <TableCell>{session.sessionId?.substring(0, 8)}...</TableCell>
              <TableCell>
                {session.user?.firstName} {session.user?.lastName}
                <br />
                <Typography variant="caption" color="textSecondary">
                  {session.user?.username}
                </Typography>
              </TableCell>
              <TableCell>{session.loginTime}</TableCell>
              <TableCell>{session.logoutTime || '-'}</TableCell>
              <TableCell>{session.ipAddress || 'Unknown'}</TableCell>
              <TableCell>
                <Chip label={session.deviceType || 'Unknown'} size="small" />
              </TableCell>
              <TableCell>{session.browser || 'Unknown'}</TableCell>
              <TableCell>
                {session.city && session.country ? `${session.city}, ${session.country}` : 'Unknown'}
              </TableCell>
              <TableCell>{getSessionStatusChip(session)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );

  const renderActivitiesTable = () => (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Activity ID</TableCell>
            <TableCell>User</TableCell>
            <TableCell>Type</TableCell>
            <TableCell>Description</TableCell>
            <TableCell>Amount</TableCell>
            <TableCell>Time</TableCell>
            <TableCell>IP Address</TableCell>
            <TableCell>Device</TableCell>
            <TableCell>Location</TableCell>
            <TableCell>Status</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {activities.map((activity) => (
            <TableRow key={activity.id}>
              <TableCell>{activity.activityId?.substring(0, 8)}...</TableCell>
              <TableCell>
                {activity.user?.firstName} {activity.user?.lastName}
                <br />
                <Typography variant="caption" color="textSecondary">
                  {activity.user?.username}
                </Typography>
              </TableCell>
              <TableCell>
                <Chip
                  label={activity.activityType}
                  color={getActivityTypeColor(activity.activityType)}
                  size="small"
                />
              </TableCell>
              <TableCell>{activity.description}</TableCell>
              <TableCell>
                {activity.amount ? fCurrency(activity.amount) : '-'}
              </TableCell>
              <TableCell>{activity.activityTime}</TableCell>
              <TableCell>{activity.ipAddress || 'Unknown'}</TableCell>
              <TableCell>
                <Chip label={activity.deviceType || 'Unknown'} size="small" />
              </TableCell>
              <TableCell>
                {activity.city && activity.country ? `${activity.city}, ${activity.country}` : 'Unknown'}
              </TableCell>
              <TableCell>
                <Chip
                  label={activity.isSuccessful ? 'Success' : 'Failed'}
                  color={getStatusColor(activity.isSuccessful)}
                  size="small"
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );

  return (
    <>
      <Helmet>
        <title> User Tracking | Ví điện tử HUST </title>
      </Helmet>

      <Stack spacing={3}>
        <Stack direction="row" alignItems="center" justifyContent="space-between">
          <Typography variant="h4">User Tracking</Typography>
        </Stack>

        <Card>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={tabValue} onChange={handleTabChange}>
              <Tab label="User Sessions" />
              <Tab label="User Activities" />
              {isAdmin && <Tab label="Quản lý cấu hình" />}
            </Tabs>
          </Box>

          <Box sx={{ p: 2 }}>
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  label="Username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter username to filter"
                />
              </Grid>
              <Grid item xs={12} sm={2}>
                <Button
                  variant="contained"
                  onClick={handleUsernameSearch}
                  disabled={loading}
                >
                  Search
                </Button>
              </Grid>
              <Grid item xs={12} sm={2}>
                <Button
                  variant="outlined"
                  onClick={() => {
                    setUsername('');
                    loadData(tabValue, 0);
                  }}
                  disabled={loading}
                >
                  Clear
                </Button>
              </Grid>
            </Grid>
          </Box>

          <TabPanel value={tabValue} index={0}>
            {renderSessionsTable()}
          </TabPanel>

          <TabPanel value={tabValue} index={1}>
            {renderActivitiesTable()}
          </TabPanel>

          {isAdmin && (
            <TabPanel value={tabValue} index={2}>
              <FraudConfig />
            </TabPanel>
          )}

          {totalPages > 1 && (
            <Box sx={{ p: 2, display: 'flex', justifyContent: 'center' }}>
              <Pagination
                count={totalPages}
                page={page + 1}
                onChange={handlePageChange}
                color="primary"
              />
            </Box>
          )}
        </Card>
      </Stack>
    </>
  );
} 