import { useEffect, useState, useCallback } from 'react';
import {
  Container, Card, CardContent, Typography, Button, Stack, Grid, Dialog, DialogTitle,
  DialogContent, DialogActions, Alert, LinearProgress, Box, Chip, Table, TableHead,
  TableRow, TableCell, TableBody, Snackbar
} from '@mui/material';
import { Helmet } from 'react-helmet-async';
import HttpService from '../../services/HttpService';

export default function StoreManagerPortal() {
  const [refundRequests, setRefundRequests] = useState([]);
  const [selectedRefund, setSelectedRefund] = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [notification, setNotification] = useState({ open: false, message: '' });
  const [actionLoading, setActionLoading] = useState(false);

  // Voucher budget state
  const [voucherBudget, setVoucherBudget] = useState({ total: 10000000, used: 3500000 });

  // Polling for refund requests every 3 seconds
  const fetchRefundRequests = useCallback(async () => {
    try {
      const response = await HttpService.getWithAuth('/payments/refund-requests/pending');
      const data = Array.isArray(response) ? response : (response?.content || []);
      // Check for new requests
      if (data.length > refundRequests.length && refundRequests.length > 0) {
        setNotification({ open: true, message: 'Có yêu cầu hoàn điểm mới!' });
      }
      setRefundRequests(data);
    } catch (err) {
      // silent fail for polling
    }
  }, [refundRequests.length]);

  useEffect(() => {
    fetchRefundRequests();
    const interval = setInterval(fetchRefundRequests, 3000);
    return () => clearInterval(interval);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    // Load voucher budget
    const loadVoucherBudget = async () => {
      try {
        const res = await HttpService.getWithAuth('/payments/voucher-budget');
        if (res) setVoucherBudget(res);
      } catch (err) {
        // use default
      }
    };
    loadVoucherBudget();
  }, []);

  const handleApprove = async () => {
    if (!selectedRefund) return;
    setActionLoading(true);
    try {
      await HttpService.postWithAuth(`/payments/refund-requests/${selectedRefund.id}/approve`, {});
      setNotification({ open: true, message: 'Đã phê duyệt hoàn điểm thành công!' });
      setRefundRequests((prev) => prev.filter((r) => r.id !== selectedRefund.id));
    } catch (err) {
      setNotification({ open: true, message: 'Lỗi phê duyệt: ' + (err.response?.data?.message || 'Unknown') });
    } finally {
      setActionLoading(false);
      setDialogOpen(false);
      setSelectedRefund(null);
    }
  };

  const handleReject = async () => {
    if (!selectedRefund) return;
    setActionLoading(true);
    try {
      await HttpService.postWithAuth(`/payments/refund-requests/${selectedRefund.id}/reject`, {});
      setNotification({ open: true, message: 'Đã từ chối yêu cầu hoàn điểm.' });
      setRefundRequests((prev) => prev.filter((r) => r.id !== selectedRefund.id));
    } catch (err) {
      setNotification({ open: true, message: 'Lỗi từ chối: ' + (err.response?.data?.message || 'Unknown') });
    } finally {
      setActionLoading(false);
      setDialogOpen(false);
      setSelectedRefund(null);
    }
  };

  const usedPercent = (voucherBudget.used / voucherBudget.total) * 100;

  return (
    <>
      <Helmet>
        <title>Quản lý Cửa hàng | Ví điện tử HUST</title>
      </Helmet>
      <Container maxWidth="lg">
        <Typography variant="h4" sx={{ mb: 3 }}>
          Cổng Quản lý Cửa hàng
        </Typography>

        <Grid container spacing={3}>
          {/* Refund Requests */}
          <Grid item xs={12} md={8}>
            <Card>
              <CardContent>
                <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                  <Typography variant="h6">Yêu cầu hoàn điểm chờ duyệt</Typography>
                  <Chip label={`${refundRequests.length} đang chờ`} color="warning" size="small" />
                </Stack>

                {refundRequests.length === 0 ? (
                  <Alert severity="info">Không có yêu cầu hoàn điểm nào đang chờ.</Alert>
                ) : (
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Mã GD</TableCell>
                        <TableCell>Số tiền</TableCell>
                        <TableCell>Thời gian</TableCell>
                        <TableCell>Hành động</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {refundRequests.map((req) => (
                        <TableRow key={req.id}>
                          <TableCell>{req.transactionId}</TableCell>
                          <TableCell>{Number(req.amount || 0).toLocaleString('vi-VN')} đ</TableCell>
                          <TableCell>{req.createdAt || 'N/A'}</TableCell>
                          <TableCell>
                            <Button
                              size="small"
                              variant="contained"
                              color="primary"
                              onClick={() => { setSelectedRefund(req); setDialogOpen(true); }}
                            >
                              Xử lý
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>
          </Grid>

          {/* Voucher Budget */}
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>Quỹ Voucher hôm nay</Typography>
                <Box sx={{ mb: 2 }}>
                  <Stack direction="row" justifyContent="space-between" sx={{ mb: 0.5 }}>
                    <Typography variant="body2">Đã sử dụng</Typography>
                    <Typography variant="body2" fontWeight={600}>
                      {usedPercent.toFixed(1)}%
                    </Typography>
                  </Stack>
                  <LinearProgress
                    variant="determinate"
                    value={usedPercent}
                    color={usedPercent > 80 ? 'error' : usedPercent > 50 ? 'warning' : 'primary'}
                    sx={{ height: 10, borderRadius: 5 }}
                  />
                </Box>
                <Stack spacing={1}>
                  <Stack direction="row" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">Tổng ngân sách:</Typography>
                    <Typography variant="body2">{voucherBudget.total.toLocaleString('vi-VN')} đ</Typography>
                  </Stack>
                  <Stack direction="row" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">Đã chi:</Typography>
                    <Typography variant="body2" color="error.main">{voucherBudget.used.toLocaleString('vi-VN')} đ</Typography>
                  </Stack>
                  <Stack direction="row" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">Còn lại:</Typography>
                    <Typography variant="body2" color="success.main">
                      {(voucherBudget.total - voucherBudget.used).toLocaleString('vi-VN')} đ
                    </Typography>
                  </Stack>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* Approval Dialog */}
        <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Xử lý yêu cầu hoàn điểm</DialogTitle>
          <DialogContent>
            {selectedRefund && (
              <Stack spacing={1} sx={{ mt: 1 }}>
                <Typography>Mã giao dịch: <strong>{selectedRefund.transactionId}</strong></Typography>
                <Typography>Số tiền: <strong>{Number(selectedRefund.amount || 0).toLocaleString('vi-VN')} đ</strong></Typography>
                <Typography variant="body2" color="text.secondary">
                  Bạn muốn phê duyệt hay từ chối yêu cầu này?
                </Typography>
              </Stack>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Hủy</Button>
            <Button variant="outlined" color="error" onClick={handleReject} disabled={actionLoading}>
              Từ chối
            </Button>
            <Button variant="contained" color="success" onClick={handleApprove} disabled={actionLoading}>
              Phê duyệt
            </Button>
          </DialogActions>
        </Dialog>

        {/* Notification Snackbar */}
        <Snackbar
          open={notification.open}
          autoHideDuration={4000}
          onClose={() => setNotification({ open: false, message: '' })}
          message={notification.message}
          anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
        />
      </Container>
    </>
  );
}
