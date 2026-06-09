import { useState } from 'react';
import {
  Container, Card, CardContent, Grid, TextField, Button, Typography, Alert, Stack,
  Divider, Dialog, DialogTitle, DialogContent, DialogActions, CircularProgress
} from '@mui/material';
import { Helmet } from 'react-helmet-async';
import HttpService from '../../services/HttpService';

export default function PosSimulator() {
  const [employeeCode, setEmployeeCode] = useState('');
  const [amount, setAmount] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  // Refund state
  const [refundDialogOpen, setRefundDialogOpen] = useState(false);
  const [refundTxId, setRefundTxId] = useState('');
  const [refundLoading, setRefundLoading] = useState(false);
  const [refundResult, setRefundResult] = useState(null);

  const handlePayment = async () => {
    if (!employeeCode || !amount) {
      setError('Vui lòng nhập đầy đủ mã nhân viên và số tiền');
      return;
    }
    setLoading(true);
    setError('');
    setResult(null);

    try {
      const response = await HttpService.postWithAuth('/payments/pos', {
        employeeCode,
        amount: parseFloat(amount),
      });
      setResult({ success: true, data: response });
    } catch (err) {
      const msg = err.response?.data?.message || 'Giao dịch thất bại. Vui lòng thử lại.';
      setResult({ success: false, message: msg });
    } finally {
      setLoading(false);
    }
  };

  const handleRefundRequest = async () => {
    if (!refundTxId) return;
    setRefundLoading(true);
    setRefundResult(null);

    try {
      const response = await HttpService.postWithAuth('/payments/refund-request', {
        transactionId: refundTxId,
      });
      setRefundResult({ success: true, data: response });
    } catch (err) {
      const msg = err.response?.data?.message || 'Yêu cầu hoàn điểm thất bại.';
      setRefundResult({ success: false, message: msg });
    } finally {
      setRefundLoading(false);
    }
  };

  return (
    <>
      <Helmet>
        <title>Giả lập POS | Ví điện tử HUST</title>
      </Helmet>
      <Container maxWidth="md">
        <Typography variant="h4" sx={{ mb: 3 }}>
          Màn hình thu ngân (POS Simulator)
        </Typography>

        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Thanh toán
            </Typography>
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Mã nhân viên / Quét QR"
                  value={employeeCode}
                  onChange={(e) => setEmployeeCode(e.target.value)}
                  placeholder="Nhập hoặc quét mã nhân viên"
                  autoFocus
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Số tiền thanh toán (VNĐ)"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  type="number"
                  inputProps={{ min: 0 }}
                />
              </Grid>
              <Grid item xs={12}>
                <Button
                  variant="contained"
                  size="large"
                  onClick={handlePayment}
                  disabled={loading || !employeeCode || !amount}
                  sx={{ minWidth: 200, height: 56 }}
                >
                  {loading ? <CircularProgress size={24} /> : 'THANH TOÁN'}
                </Button>
              </Grid>
            </Grid>

            {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
            {result?.success && (
              <Alert severity="success" sx={{ mt: 2 }}>
                Giao dịch thành công! Mã GD: {result.data?.referenceNumber || result.data?.id || 'N/A'}
              </Alert>
            )}
            {result && !result.success && (
              <Alert severity="error" sx={{ mt: 2 }}>{result.message}</Alert>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Hoàn điểm (Refund)
            </Typography>
            <Stack direction="row" spacing={2} alignItems="center">
              <TextField
                label="Mã giao dịch cần hoàn"
                value={refundTxId}
                onChange={(e) => setRefundTxId(e.target.value)}
                sx={{ flex: 1 }}
              />
              <Button
                variant="outlined"
                color="warning"
                size="large"
                onClick={() => setRefundDialogOpen(true)}
                disabled={!refundTxId}
                sx={{ height: 56 }}
              >
                YÊU CẦU HOÀN ĐIỂM
              </Button>
            </Stack>
          </CardContent>
        </Card>

        {/* Refund Confirmation Dialog */}
        <Dialog open={refundDialogOpen} onClose={() => setRefundDialogOpen(false)}>
          <DialogTitle>Xác nhận yêu cầu hoàn điểm</DialogTitle>
          <DialogContent>
            <Typography>
              Bạn đang yêu cầu hoàn điểm cho giao dịch: <strong>{refundTxId}</strong>
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Yêu cầu này sẽ được gửi đến Quản lý cửa hàng để phê duyệt.
            </Typography>
            {refundResult?.success && (
              <Alert severity="success" sx={{ mt: 2 }}>Yêu cầu hoàn điểm đã được gửi thành công!</Alert>
            )}
            {refundResult && !refundResult.success && (
              <Alert severity="error" sx={{ mt: 2 }}>{refundResult.message}</Alert>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => { setRefundDialogOpen(false); setRefundResult(null); }}>Đóng</Button>
            <Button
              variant="contained"
              color="warning"
              onClick={handleRefundRequest}
              disabled={refundLoading}
            >
              {refundLoading ? <CircularProgress size={20} /> : 'Gửi yêu cầu'}
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </>
  );
}
