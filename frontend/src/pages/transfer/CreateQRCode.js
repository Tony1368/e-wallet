import { useEffect, useState } from 'react';
import { Card, Grid, Stack, TextField, Autocomplete, Button, Typography, Alert, CircularProgress } from '@mui/material';
import { Helmet } from 'react-helmet-async';
import { QRCodeSVG } from 'qrcode.react';
import AuthService from '../../services/AuthService';
import HttpService from '../../services/HttpService';

export default function CreateQRCode() {
  const [wallets, setWallets] = useState([]);
  const [allWallets, setAllWallets] = useState([]); // Track all wallets for debugging
  const [selectedWallet, setSelectedWallet] = useState(null);
  const [amount, setAmount] = useState('');
  const [qrValue, setQrValue] = useState('');
  const [amountError, setAmountError] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadWallets = async () => {
      try {
        setLoading(true);
        setError('');
        const userId = AuthService.getCurrentUser()?.id;
        const userRoles = AuthService.getCurrentUser()?.roles;
        console.log('Loading wallets for user:', userId);
        console.log('User roles:', userRoles);
        
        const result = await HttpService.getWithAuth(`/wallets/users/${userId}`);
        console.log('All wallets from API:', result);
        
        // Show all wallets for debugging
        const allWalletsResult = result || [];
        setAllWallets(allWalletsResult); // Store all wallets for debugging
        console.log('All wallets count:', allWalletsResult.length);
        allWalletsResult.forEach((wallet, index) => {
          console.log(`Wallet ${index + 1}:`, {
            id: wallet.id,
            name: wallet.name,
            status: wallet.status,
            balance: wallet.balance,
            iban: wallet.iban
          });
        });
        
        const activeWallets = allWalletsResult.filter((wallet) => wallet.status === 'ACTIVE');
        console.log('Active wallets count:', activeWallets.length);
        activeWallets.forEach((wallet, index) => {
          console.log(`Active Wallet ${index + 1}:`, {
            id: wallet.id,
            name: wallet.name,
            status: wallet.status,
            balance: wallet.balance,
            iban: wallet.iban
          });
        });
        
        setWallets(activeWallets);
      } catch (err) {
        console.error('Error loading wallets:', err);
        setError('Không thể tải danh sách ví. Vui lòng thử lại.');
      } finally {
        setLoading(false);
      }
    };

    loadWallets();
  }, []);

  const validateAmount = (value) => {
    if (!value) {
      setAmountError('');
      return true;
    }
    const num = Number(value);
    if (Number.isNaN(num) || num <= 0) {
      setAmountError('Số điểm phải là số dương hợp lệ hoặc để trống.');
      return false;
    }
    setAmountError('');
    return true;
  };

  const handleGenerateQR = () => {
    if (!selectedWallet) return;
    if (!validateAmount(amount)) return;
    const data = {
      iban: selectedWallet.iban,
      amount: amount || null,
    };
    setQrValue(JSON.stringify(data));
  };

  if (loading) {
    return (
      <Card>
        <Grid container justifyContent="center" alignItems="center" sx={{ minHeight: 300, padding: 5 }}>
          <CircularProgress />
        </Grid>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <Grid container justifyContent="center" alignItems="center" sx={{ minHeight: 300, padding: 5 }}>
          <Alert severity="error">{error}</Alert>
        </Grid>
      </Card>
    );
  }

  if (wallets.length === 0) {
    return (
      <Card>
        <Grid container justifyContent="center" alignItems="center" sx={{ minHeight: 300, padding: 5 }}>
          <Stack spacing={2} alignItems="center">
            <Alert severity="info">
              Bạn chưa có ví điện tử nào hoặc tất cả ví đều không hoạt động. Vui lòng tạo ví mới hoặc liên hệ quản trị viên.
            </Alert>
            {allWallets.length > 0 && (
              <Alert severity="warning">
                <Typography variant="body2">
                  Bạn có {allWallets.length} ví điện tử, nhưng không có ví nào đang hoạt động:
                </Typography>
                <ul style={{ margin: '8px 0', paddingLeft: '20px' }}>
                  {allWallets.map((wallet) => (
                    <li key={wallet.id}>
                      {wallet.name} - Trạng thái: {wallet.status === 'ACTIVE' ? 'Hoạt động' : 'Đã đóng'}
                    </li>
                  ))}
                </ul>
              </Alert>
            )}
          </Stack>
        </Grid>
      </Card>
    );
  }

  return (
    <>
      <Helmet>
        <title>Tạo QR Code | Ví điện tử HUST</title>
      </Helmet>
      <Card>
        <Grid container direction="row" alignItems="center" justifyContent="flex-start" sx={{ minHeight: 300, padding: 5 }}>
          <Grid item xs={12} md={6}>
            <Stack spacing={3}>
              <Autocomplete
                ListboxProps={{ style: { maxHeight: 200, overflow: 'auto' } }}
                required
                disablePortal
                id="wallet"
                noOptionsText="Không có ví nào"
                options={wallets || []}
                getOptionLabel={(wallet) => wallet.name}
                isOptionEqualToValue={(option, value) => option?.name === value?.name}
                onChange={(event, newValue) => setSelectedWallet(newValue)}
                renderInput={(params) => <TextField {...params} label="Chọn ví nhận điểm" />}
              />
              <TextField
                id="amount"
                name="amount"
                label="Số điểm (có thể để trống)"
                value={amount}
                onChange={(e) => {
                  setAmount(e.target.value);
                  validateAmount(e.target.value);
                }}
                error={!!amountError}
                helperText={amountError}
                type="text"
                inputProps={{ inputMode: 'decimal', pattern: '[0-9]*' }}
              />
              <Button
                variant="contained"
                onClick={handleGenerateQR}
                disabled={!selectedWallet || !!amountError}
                sx={{ width: 200 }}
              >
                Tạo mã QR
              </Button>
            </Stack>
          </Grid>
          <Grid item xs={12} md={6} display="flex" justifyContent="center" alignItems="center" minHeight={300}>
            {qrValue && (
              <Stack alignItems="center" spacing={2}>
                <Typography variant="subtitle1">Mã QR của bạn:</Typography>
                <QRCodeSVG value={qrValue} size={200} />
                <Typography variant="body2" color="text.secondary">
                  {selectedWallet?.name} - {selectedWallet?.iban}
                  {amount && ` | Số điểm: ${amount}`}
                </Typography>
              </Stack>
            )}
          </Grid>
        </Grid>
      </Card>
    </>
  );
} 