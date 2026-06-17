import { useState, useEffect } from 'react';
import { Container, Grid, Typography, Card, CardContent, Stack, Box, List, ListItem,
  ListItemText, Divider, Chip, LinearProgress } from '@mui/material';
import { Helmet } from 'react-helmet-async';
import { AppWidgetSummary } from '../../sections/@dashboard/app';
import HttpService from '../../services/HttpService';
import AuthService from '../../services/AuthService';

export default function Dashboard() {
  const user = AuthService.getCurrentUser();
  const roles = (user?.roles || []).map(r => typeof r === 'string' ? r : r.name);

  const isAdmin = roles.includes('ROLE_ADMIN');
  const isAccountant = roles.includes('ROLE_ACCOUNTANT');
  const isCashier = roles.includes('ROLE_CASHIER');
  const isManager = roles.includes('ROLE_MANAGER');
  const isEmployee = !isAdmin && !isAccountant && !isCashier && !isManager;

  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({});
  const [wallets, setWallets] = useState([]);
  const [recentTx, setRecentTx] = useState([]);

  useEffect(() => {
    loadDashboardData();
  }, []); // eslint-disable-line

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      if (isAdmin || isManager) {
        const branchId = user?.branchId;
        // Admin + Accountant: xem tất cả. Manager: chỉ xem ví chi nhánh mình
        const walletUrl = (isAdmin) 
          ? '/admin/wallets?page=0&size=100'
          : `/admin/wallets?page=0&size=100&branchId=${branchId}`;
        const [walletsRes, txRes] = await Promise.all([
          HttpService.getWithAuth(walletUrl).catch(() => null),
          HttpService.getWithAuth('/admin/transactions?page=0&size=5').catch(() => null),
        ]);
        const walletList = walletsRes?.content || [];
        const totalBalance = walletList.reduce((sum, w) => sum + Number(w.balance || 0), 0);
        setStats({
          totalWallets: walletsRes?.totalElements || walletList.length,
          totalBalance,
          totalTransactions: txRes?.totalElements || 0,
        });
        setWallets(walletList.slice(0, 5));
        setRecentTx(txRes?.content || []);
      } else if (isAccountant) {
        const [entriesRes] = await Promise.all([
          HttpService.getWithAuth('/accounting/journal-entries?page=0&size=5').catch(() => null),
        ]);
        setStats({
          totalEntries: entriesRes?.totalElements || 0,
        });
        setRecentTx((entriesRes?.content || []).map(e => ({
          id: e.id,
          description: e.description,
          amount: e.amount,
          createdAt: e.createdAt,
          type: e.transactionType,
        })));
      } else if (isCashier) {
        const txRes = await HttpService.getWithAuth('/admin/transactions?page=0&size=10').catch(() => null);
        const txList = txRes?.content || [];
        const todayTotal = txList.reduce((sum, t) => sum + Number(t.amount || 0), 0);
        setStats({
          todayTransactions: txRes?.totalElements || 0,
          todayTotal,
        });
        setRecentTx(txList.slice(0, 5));
      } else {
        // Employee/Customer
        const userWallets = await HttpService.getWithAuth(`/wallets/users/${user?.id}`).catch(() => []);
        const wList = Array.isArray(userWallets) ? userWallets : [];
        setWallets(wList);
        const totalBalance = wList.reduce((sum, w) => sum + Number(w.balance || 0), 0);
        setStats({ totalBalance, walletCount: wList.length });

        if (wList.length > 0) {
          const txRes = await HttpService.getWithAuth('/transactions?page=0&size=5').catch(() => null);
          setRecentTx(txRes?.content || []);
        }
      }
    } catch (err) {
      console.error('Dashboard load error:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatAmount = (amount) => Number(amount || 0).toLocaleString('vi-VN') + ' đ';

  const getRoleName = () => {
    if (isAdmin) return 'Quản trị viên';
    if (isAccountant) return 'Kế toán';
    if (isCashier) return 'Thu ngân';
    if (isManager) return 'Quản lý cửa hàng';
    return 'Nhân viên';
  };

  return (
    <>
      <Helmet>
        <title>Trang tổng quan | Ví điện tử HUST</title>
      </Helmet>
      <Container maxWidth="xl">
        <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 4 }}>
          <Box>
            <Typography variant="h4">
              Xin chào, {user?.firstName || user?.username || 'User'} 👋
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Vai trò: {getRoleName()}
            </Typography>
          </Box>
        </Stack>

        {loading && <LinearProgress sx={{ mb: 2 }} />}

        {/* ADMIN / MANAGER Dashboard */}
        {(isAdmin || isManager) && !loading && (
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={3}>
              <AppWidgetSummary title="Tổng số ví" total={stats.totalWallets || 0} icon="ant-design:wallet-outlined" />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <AppWidgetSummary title="Tổng giao dịch" total={stats.totalTransactions || 0} color="info" icon="ant-design:transaction-outlined" />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <AppWidgetSummary title="Tổng số dư (đ)" total={stats.totalBalance || 0} color="warning" icon="ant-design:fund-outlined" />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <AppWidgetSummary title="Ví hoạt động" total={wallets.filter(w => w.status === 'ACTIVE').length} color="success" icon="ant-design:check-circle-outlined" />
            </Grid>

            {/* Top wallets */}
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>Top ví theo số dư</Typography>
                  <List dense>
                    {wallets.sort((a, b) => b.balance - a.balance).slice(0, 5).map((w) => (
                      <ListItem key={w.id} secondaryAction={<Typography variant="body2" fontWeight="bold">{formatAmount(w.balance)}</Typography>}>
                        <ListItemText primary={w.name} secondary={`IBAN: ${w.iban}`} />
                      </ListItem>
                    ))}
                  </List>
                </CardContent>
              </Card>
            </Grid>

            {/* Recent transactions */}
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>Giao dịch gần đây</Typography>
                  <List dense>
                    {recentTx.map((tx) => (
                      <Box key={tx.id}>
                        <ListItem>
                          <ListItemText
                            primary={tx.description || `GD #${tx.id}`}
                            secondary={tx.createdAt}
                          />
                          <Typography variant="body2" fontWeight="bold" color="primary">
                            {formatAmount(tx.amount)}
                          </Typography>
                        </ListItem>
                        <Divider />
                      </Box>
                    ))}
                    {recentTx.length === 0 && <Typography variant="body2" color="text.secondary">Chưa có giao dịch</Typography>}
                  </List>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        )}

        {/* ACCOUNTANT Dashboard */}
        {isAccountant && !isAdmin && !loading && (
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={4}>
              <AppWidgetSummary title="Tổng bút toán" total={stats.totalEntries || 0} icon="ant-design:book-outlined" />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <AppWidgetSummary title="Bút toán hôm nay" total={recentTx.length} color="info" icon="ant-design:calendar-outlined" />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <AppWidgetSummary title="Chưa kết chuyển ERP" total={stats.totalEntries || 0} color="warning" icon="ant-design:cloud-upload-outlined" />
            </Grid>

            <Grid item xs={12}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>Bút toán gần đây</Typography>
                  <List dense>
                    {recentTx.map((entry) => (
                      <Box key={entry.id}>
                        <ListItem>
                          <ListItemText
                            primary={entry.description || `Bút toán #${entry.id}`}
                            secondary={`${entry.type || ''} • ${entry.createdAt || ''}`}
                          />
                          <Typography variant="body2" fontWeight="bold">
                            {formatAmount(entry.amount)}
                          </Typography>
                        </ListItem>
                        <Divider />
                      </Box>
                    ))}
                    {recentTx.length === 0 && <Typography variant="body2" color="text.secondary">Chưa có bút toán</Typography>}
                  </List>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        )}

        {/* CASHIER Dashboard */}
        {isCashier && !isAdmin && !loading && (
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6}>
              <AppWidgetSummary title="Giao dịch POS" total={stats.todayTransactions || 0} icon="ant-design:shopping-outlined" />
            </Grid>
            <Grid item xs={12} sm={6}>
              <AppWidgetSummary title="Tổng thu (đ)" total={stats.todayTotal || 0} color="success" icon="ant-design:dollar-outlined" />
            </Grid>

            <Grid item xs={12}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>Giao dịch gần đây</Typography>
                  <List dense>
                    {recentTx.map((tx) => (
                      <Box key={tx.id}>
                        <ListItem>
                          <ListItemText primary={tx.description || `GD #${tx.id}`} secondary={tx.createdAt} />
                          <Typography variant="body2" fontWeight="bold" color="success.main">
                            {formatAmount(tx.amount)}
                          </Typography>
                        </ListItem>
                        <Divider />
                      </Box>
                    ))}
                    {recentTx.length === 0 && <Typography variant="body2" color="text.secondary">Chưa có giao dịch hôm nay</Typography>}
                  </List>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        )}

        {/* EMPLOYEE / CUSTOMER Dashboard */}
        {isEmployee && !loading && (
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={4}>
              <AppWidgetSummary title="Số ví" total={stats.walletCount || 0} icon="ant-design:wallet-outlined" />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <AppWidgetSummary title="Tổng số dư (đ)" total={stats.totalBalance || 0} color="success" icon="ant-design:fund-outlined" />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <AppWidgetSummary title="Giao dịch gần đây" total={recentTx.length} color="info" icon="ant-design:history-outlined" />
            </Grid>

            {/* Wallets */}
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>Ví của tôi</Typography>
                  <List dense>
                    {wallets.map((w) => (
                      <Box key={w.id}>
                        <ListItem>
                          <ListItemText primary={w.name} secondary={w.iban} />
                          <Stack alignItems="flex-end">
                            <Typography variant="body2" fontWeight="bold">{formatAmount(w.balance)}</Typography>
                            <Chip label={w.status} size="small" color={w.status === 'ACTIVE' ? 'success' : 'default'} />
                          </Stack>
                        </ListItem>
                        <Divider />
                      </Box>
                    ))}
                    {wallets.length === 0 && <Typography variant="body2" color="text.secondary">Chưa có ví</Typography>}
                  </List>
                </CardContent>
              </Card>
            </Grid>

            {/* Recent transactions */}
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>Giao dịch gần đây</Typography>
                  <List dense>
                    {recentTx.map((tx) => (
                      <Box key={tx.id}>
                        <ListItem>
                          <ListItemText primary={tx.description || `GD #${tx.id}`} secondary={tx.createdAt} />
                          <Typography variant="body2" fontWeight="bold">
                            {formatAmount(tx.amount)}
                          </Typography>
                        </ListItem>
                        <Divider />
                      </Box>
                    ))}
                    {recentTx.length === 0 && <Typography variant="body2" color="text.secondary">Chưa có giao dịch</Typography>}
                  </List>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        )}
      </Container>
    </>
  );
}
