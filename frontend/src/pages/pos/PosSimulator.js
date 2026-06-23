import { useState, useEffect } from 'react';
import {
  Container, Card, CardContent, Grid, TextField, Button, Typography, Alert, Stack,
  Dialog, DialogTitle, DialogContent, DialogActions, CircularProgress, Chip, Box,
  Autocomplete, ToggleButtonGroup, ToggleButton
} from '@mui/material';
import { Helmet } from 'react-helmet-async';
import HttpService from '../../services/HttpService';
import AuthService from '../../services/AuthService';

export default function PosSimulator() {
  const currentUser = AuthService.getCurrentUser();

  // Ví cửa hàng (ví của thu ngân nhận tiền)
  const [storeWallets, setStoreWallets] = useState([]);
  const [selectedStoreWallet, setSelectedStoreWallet] = useState(null);

  // Chế độ tìm kiếm
  const [lookupMode, setLookupMode] = useState('iban');
  const [ibanInput, setIbanInput] = useState('');
  const [userIdInput, setUserIdInput] = useState('');
  const [customerWallets, setCustomerWallets] = useState([]);
  const [selectedCustomerWallet, setSelectedCustomerWallet] = useState(null);
  const [lookupLoading, setLookupLoading] = useState(false);
  const [lookupError, setLookupError] = useState('');

  // Thanh toán
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [paymentLoading, setPaymentLoading] = useState(false);
  const [paymentResult, setPaymentResult] = useState(null);

  // Hoàn điểm
  const [refundDialogOpen, setRefundDialogOpen] = useState(false);
  const [refundTxId, setRefundTxId] = useState('');
  const [refundAmount, setRefundAmount] = useState('');
  const [refundLoading, setRefundLoading] = useState(false);
  const [refundResult, setRefundResult] = useState(null);

  // Thanh toán thành công dialog
  const [successDialogOpen, setSuccessDialogOpen] = useState(false);
  const [successData, setSuccessData] = useState(null);

  // Tải ví cửa hàng khi khởi tạo
  useEffect(() => {
    const loadStoreWallets = async () => {
      try {
        const wallets = await HttpService.getWithAuth(`/wallets/users/${currentUser?.id}`);
        const active = wallets?.filter(w => w.status === 'ACTIVE') || [];
        setStoreWallets(active);
        if (active.length > 0) setSelectedStoreWallet(active[0]);
      } catch (err) {
        console.error('Lỗi tải ví cửa hàng:', err);
      }
    };
    loadStoreWallets();
  }, [currentUser?.id]);

  // Tìm theo IBAN
  const handleLookupIban = async () => {
    if (!ibanInput.trim()) return;
    setLookupLoading(true);
    setLookupError('');
    setSelectedCustomerWallet(null);
    setCustomerWallets([]);
    try {
      const wallet = await HttpService.getWithAuth(`/wallets/iban/${ibanInput.trim()}`);
      setSelectedCustomerWallet(wallet);
    } catch (err) {
      setLookupError(err.response?.data?.message || 'Không tìm thấy ví với IBAN này');
    } finally {
      setLookupLoading(false);
    }
  };

  // Tìm theo User ID (nhập ID người dùng)
  const handleLookupUser = async () => {
    if (!userIdInput.trim()) return;
    setLookupLoading(true);
    setLookupError('');
    setSelectedCustomerWallet(null);
    setCustomerWallets([]);
    try {
      const wallets = await HttpService.getWithAuth(`/wallets/users/${userIdInput.trim()}`);
      const active = wallets?.filter(w => w.status === 'ACTIVE') || [];
      if (active.length === 0) {
        setLookupError('Người dùng không có ví hoạt động');
        return;
      }
      setCustomerWallets(active);
      if (active.length === 1) setSelectedCustomerWallet(active[0]);
    } catch (err) {
      setLookupError(err.response?.data?.message || 'Không tìm thấy người dùng');
    } finally {
      setLookupLoading(false);
    }
  };

  // Thanh toán: chuyển từ ví khách → ví cửa hàng
  const handlePayment = async () => {
    if (!selectedCustomerWallet || !selectedStoreWallet || !amount) return;
    setPaymentLoading(true);
    setPaymentResult(null);
    try {
      const response = await HttpService.postWithAuth('/payments/transfer', {
        fromWalletIban: selectedCustomerWallet.iban,
        toWalletIban: selectedStoreWallet.iban,
        amount: parseFloat(amount),
        description: description || `Thanh toán POS - ${currentUser?.username}`,
        typeId: 1,
      });
      setPaymentResult({ success: true, data: response });
      setSuccessData(response);
      setSuccessDialogOpen(true);
      // Cập nhật số dư khách hàng
      try {
        const updated = await HttpService.getWithAuth(`/wallets/iban/${selectedCustomerWallet.iban}`);
        setSelectedCustomerWallet(updated);
      } catch (e) { /* bỏ qua lỗi refresh */ }
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.errors?.[0]?.message || 'Giao dịch thất bại';
      setPaymentResult({ success: false, message: msg });
    } finally {
      setPaymentLoading(false);
    }
  };

  const handleReset = () => {
    setIbanInput('');
    setUserIdInput('');
    setCustomerWallets([]);
    setSelectedCustomerWallet(null);
    setAmount('');
    setDescription('');
    setPaymentResult(null);
    setLookupError('');
  };

  // Hoàn điểm: gửi yêu cầu chờ duyệt
  const handleRefundRequest = async () => {
    if (!refundTxId) return;
    setRefundLoading(true);
    setRefundResult(null);
    try {
      await HttpService.postWithAuth('/payments/refund-request', {
        transactionId: refundTxId,
        walletId: selectedStoreWallet?.id || 0,
        amount: refundAmount ? parseFloat(refundAmount) : 0, // 0 = hoàn toàn bộ
        reason: refundAmount ? `Hoàn một phần: ${Number(refundAmount).toLocaleString('vi-VN')} đ` : 'Hoàn toàn bộ giao dịch',
        requestedBy: currentUser?.username,
      });
      setRefundResult({ success: true });
    } catch (err) {
      setRefundResult({ success: false, message: err.response?.data?.message || 'Yêu cầu hoàn điểm thất bại.' });
    } finally {
      setRefundLoading(false);
    }
  };

  return (
    <>
      <Helmet>
        <title>Thu ngân POS | Ví điện tử HUST</title>
      </Helmet>
      <Container maxWidth="md">
        <Typography variant="h4" sx={{ mb: 3 }}>
          Màn hình Thu ngân (POS)
        </Typography>

        {/* Thông tin ví cửa hàng */}
        {selectedStoreWallet && (
          <Alert severity="info" sx={{ mb: 2 }}>
            Ví cửa hàng: <strong>{selectedStoreWallet.name}</strong> ({selectedStoreWallet.iban}) — Số dư: {Number(selectedStoreWallet.balance).toLocaleString('vi-VN')} đ
          </Alert>
        )}

        {/* Bước 1: Tìm khách hàng */}
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
              <Typography variant="h6">Bước 1: Tìm khách hàng</Typography>
              <ToggleButtonGroup
                value={lookupMode}
                exclusive
                onChange={(e, v) => { if (v) { setLookupMode(v); handleReset(); } }}
                size="small"
              >
                <ToggleButton value="iban">IBAN / Quét QR</ToggleButton>
                <ToggleButton value="userid">Mã người dùng</ToggleButton>
              </ToggleButtonGroup>
            </Stack>

            {lookupMode === 'iban' ? (
              <Stack direction="row" spacing={2} alignItems="center">
                <TextField
                  fullWidth
                  label="Nhập số IBAN hoặc quét mã QR"
                  value={ibanInput}
                  onChange={(e) => setIbanInput(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleLookupIban()}
                  placeholder="VD: GB29NWBK60161331926819"
                  autoFocus
                  disabled={!!selectedCustomerWallet}
                />
                {!selectedCustomerWallet ? (
                  <Button variant="contained" onClick={handleLookupIban} disabled={lookupLoading || !ibanInput.trim()} sx={{ height: 56, minWidth: 100 }}>
                    {lookupLoading ? <CircularProgress size={24} /> : 'Tìm'}
                  </Button>
                ) : (
                  <Button variant="outlined" color="secondary" onClick={handleReset} sx={{ height: 56 }}>Đổi</Button>
                )}
              </Stack>
            ) : (
              <Stack spacing={2}>
                <Stack direction="row" spacing={2} alignItems="center">
                  <TextField
                    fullWidth
                    label="Nhập mã người dùng (User ID)"
                    value={userIdInput}
                    onChange={(e) => setUserIdInput(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && handleLookupUser()}
                    placeholder="VD: 4"
                    type="number"
                    disabled={!!selectedCustomerWallet}
                  />
                  {!selectedCustomerWallet ? (
                    <Button variant="contained" onClick={handleLookupUser} disabled={lookupLoading || !userIdInput.trim()} sx={{ height: 56, minWidth: 100 }}>
                      {lookupLoading ? <CircularProgress size={24} /> : 'Tìm'}
                    </Button>
                  ) : (
                    <Button variant="outlined" color="secondary" onClick={handleReset} sx={{ height: 56 }}>Đổi</Button>
                  )}
                </Stack>
                {customerWallets.length > 1 && !selectedCustomerWallet && (
                  <Autocomplete
                    options={customerWallets}
                    getOptionLabel={(w) => `${w.name} (${w.iban}) — ${Number(w.balance).toLocaleString('vi-VN')} đ`}
                    onChange={(e, v) => setSelectedCustomerWallet(v)}
                    renderInput={(params) => <TextField {...params} label="Chọn ví của khách" />}
                  />
                )}
              </Stack>
            )}

            {lookupError && <Alert severity="error" sx={{ mt: 2 }}>{lookupError}</Alert>}

            {selectedCustomerWallet && (
              <Box sx={{ mt: 2, p: 2, bgcolor: '#f5f5f5', borderRadius: 1 }}>
                <Stack direction="row" justifyContent="space-between" alignItems="center">
                  <Box>
                    <Typography variant="subtitle1" fontWeight={600}>{selectedCustomerWallet.name}</Typography>
                    <Typography variant="body2" color="text.secondary">IBAN: {selectedCustomerWallet.iban}</Typography>
                  </Box>
                  <Stack alignItems="flex-end">
                    <Chip label={selectedCustomerWallet.status === 'ACTIVE' ? 'Hoạt động' : 'Đã khóa'} color={selectedCustomerWallet.status === 'ACTIVE' ? 'success' : 'error'} size="small" />
                    <Typography variant="h6" color="primary" sx={{ mt: 0.5 }}>
                      {Number(selectedCustomerWallet.balance).toLocaleString('vi-VN')} đ
                    </Typography>
                  </Stack>
                </Stack>
              </Box>
            )}
          </CardContent>
        </Card>

        {/* Bước 2: Thanh toán */}
        {selectedCustomerWallet && (
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>Bước 2: Thanh toán</Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Số tiền (VNĐ)"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    type="number"
                    inputProps={{ min: 0 }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Ghi chú"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="VD: Mua cà phê"
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    color="success"
                    size="large"
                    onClick={handlePayment}
                    disabled={paymentLoading || !amount || parseFloat(amount) <= 0}
                    sx={{ minWidth: 250, height: 56, fontSize: '1.1rem' }}
                  >
                    {paymentLoading ? <CircularProgress size={24} /> : `THANH TOÁN ${amount ? Number(amount).toLocaleString('vi-VN') + ' đ' : ''}`}
                  </Button>
                </Grid>
              </Grid>

              {paymentResult?.success && (
                <Alert severity="success" sx={{ mt: 2 }}>
                  Giao dịch thành công!
                  <br /><strong>Mã giao dịch: {paymentResult.data?.id || 'N/A'}</strong>
                  <br />Số dư khách còn lại: {Number(selectedCustomerWallet.balance).toLocaleString('vi-VN')} đ
                  <br />Tiền đã chuyển vào ví cửa hàng.
                </Alert>
              )}
              {paymentResult && !paymentResult.success && (
                <Alert severity="error" sx={{ mt: 2 }}>{paymentResult.message}</Alert>
              )}
            </CardContent>
          </Card>
        )}

        {/* Hoàn điểm */}
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>Hoàn điểm (Refund)</Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Khi được duyệt, tiền sẽ trừ từ ví cửa hàng và cộng vào ví khách hàng. Nếu không nhập số tiền, hệ thống sẽ hoàn toàn bộ giá trị giao dịch.
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={5}>
                <TextField
                  fullWidth
                  label="Mã giao dịch cần hoàn (*)"
                  value={refundTxId}
                  onChange={(e) => setRefundTxId(e.target.value)}
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  label="Số tiền hoàn (để trống = hoàn toàn bộ)"
                  value={refundAmount}
                  onChange={(e) => setRefundAmount(e.target.value)}
                  type="number"
                  inputProps={{ min: 0 }}
                  helperText="Không vượt quá số tiền giao dịch gốc"
                />
              </Grid>
              <Grid item xs={12} sm={3}>
                <Button
                  variant="outlined"
                  color="warning"
                  fullWidth
                  onClick={() => setRefundDialogOpen(true)}
                  disabled={!refundTxId}
                  sx={{ height: 56 }}
                >
                  Yêu cầu hoàn
                </Button>
              </Grid>
            </Grid>
          </CardContent>
        </Card>

        {/* Dialog thanh toán thành công */}
        <Dialog open={successDialogOpen} onClose={() => setSuccessDialogOpen(false)} maxWidth="xs" fullWidth>
          <DialogTitle sx={{ textAlign: 'center', color: 'success.main' }}>
            ✅ Thanh toán thành công!
          </DialogTitle>
          <DialogContent sx={{ textAlign: 'center', py: 3 }}>
            <Typography variant="h4" color="success.main" sx={{ mb: 2 }}>
              {amount ? Number(amount).toLocaleString('vi-VN') + ' đ' : ''}
            </Typography>
            <Typography variant="body1">Mã giao dịch: <strong>{successData?.id || 'N/A'}</strong></Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {description || 'Thanh toán POS'}
            </Typography>
          </DialogContent>
          <DialogActions sx={{ justifyContent: 'center', pb: 2 }}>
            <Button variant="contained" color="success" onClick={() => { setSuccessDialogOpen(false); handleReset(); }}>
              Giao dịch mới
            </Button>
            <Button variant="outlined" onClick={() => setSuccessDialogOpen(false)}>
              Tiếp tục thu
            </Button>
          </DialogActions>
        </Dialog>

        {/* Dialog xác nhận hoàn điểm */}
        <Dialog open={refundDialogOpen} onClose={() => setRefundDialogOpen(false)}>
          <DialogTitle>Xác nhận yêu cầu hoàn điểm</DialogTitle>
          <DialogContent>
            <Typography>Mã giao dịch: <strong>{refundTxId}</strong></Typography>
            <Typography>Số tiền hoàn: <strong>{refundAmount ? Number(refundAmount).toLocaleString('vi-VN') + ' đ' : 'Toàn bộ giá trị giao dịch'}</strong></Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Yêu cầu sẽ được gửi đến Quản lý cửa hàng để phê duyệt. Khi được duyệt, tiền sẽ trừ từ ví cửa hàng và hoàn vào ví khách.
            </Typography>
            {refundResult?.success && <Alert severity="success" sx={{ mt: 2 }}>Gửi yêu cầu thành công!</Alert>}
            {refundResult && !refundResult.success && <Alert severity="error" sx={{ mt: 2 }}>{refundResult.message}</Alert>}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => { setRefundDialogOpen(false); setRefundResult(null); }}>Đóng</Button>
            <Button variant="contained" color="warning" onClick={handleRefundRequest} disabled={refundLoading}>
              {refundLoading ? <CircularProgress size={20} /> : 'Gửi yêu cầu'}
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </>
  );
}
