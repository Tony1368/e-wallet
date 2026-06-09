import { useEffect, useState } from 'react';
import {
  Container, Card, CardContent, Typography, Stack, Skeleton, Grid, Chip, Box, ToggleButtonGroup, ToggleButton
} from '@mui/material';
import { Helmet } from 'react-helmet-async';
import { QRCodeSVG } from 'qrcode.react';
import AuthService from '../../services/AuthService';
import HttpService from '../../services/HttpService';

export default function EmployeePortal() {
  const [wallets, setWallets] = useState([]);
  const [selectedWalletId, setSelectedWalletId] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const currentUser = AuthService.getCurrentUser();

  useEffect(() => {
    const loadData = async () => {
      try {
        const [walletsRes, txRes] = await Promise.all([
          HttpService.getWithAuth(`/wallets/users/${currentUser?.id}`),
          HttpService.getWithAuth(`/transactions/users/${currentUser?.id}`),
        ]);
        const activeWallets = walletsRes?.filter((w) => w.status === 'ACTIVE') || [];
        setWallets(activeWallets);
        if (activeWallets.length > 0) {
          setSelectedWalletId(activeWallets[0].id);
        }
        setTransactions(Array.isArray(txRes) ? txRes.slice(0, 10) : (txRes?.content?.slice(0, 10) || []));
      } catch (err) {
        console.error('Error loading employee data:', err);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [currentUser?.id]);

  const selectedWallet = wallets.find((w) => w.id === selectedWalletId) || null;

  const qrData = selectedWallet
    ? JSON.stringify({ userId: currentUser?.id, walletId: selectedWallet.id, iban: selectedWallet.iban })
    : '';

  if (loading) {
    return (
      <Container maxWidth="sm" sx={{ py: 3 }}>
        <Skeleton variant="rectangular" height={300} sx={{ borderRadius: 2 }} />
        <Skeleton variant="rectangular" height={100} sx={{ mt: 2, borderRadius: 2 }} />
      </Container>
    );
  }

  return (
    <>
      <Helmet>
        <title>Nhân viên | Ví điện tử HUST</title>
      </Helmet>
      <Container maxWidth="sm" sx={{ py: 2 }}>
        {/* Chọn ví */}
        {wallets.length > 0 && (
          <Card sx={{ mb: 2 }}>
            <CardContent sx={{ pb: '12px !important' }}>
              <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
                Chọn ví hiển thị QR
              </Typography>
              <ToggleButtonGroup
                value={selectedWalletId}
                exclusive
                onChange={(e, val) => { if (val !== null) setSelectedWalletId(val); }}
                size="small"
                fullWidth
                sx={{ flexWrap: 'wrap', gap: 0.5 }}
              >
                {wallets.map((wallet) => (
                  <ToggleButton key={wallet.id} value={wallet.id} sx={{ textTransform: 'none', flex: '1 1 auto' }}>
                    {wallet.name}
                  </ToggleButton>
                ))}
              </ToggleButtonGroup>
            </CardContent>
          </Card>
        )}

        {/* QR Code thay đổi theo ví được chọn */}
        <Card sx={{ mb: 2, textAlign: 'center', py: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Mã QR thanh toán
            </Typography>
            {selectedWallet ? (
              <>
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 2 }}>
                  <QRCodeSVG value={qrData} size={250} level="H" />
                </Box>
                <Typography variant="body1" fontWeight={600}>
                  {selectedWallet.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  IBAN: {selectedWallet.iban}
                </Typography>
                <Typography variant="body2" color="primary" sx={{ mt: 0.5 }}>
                  Số dư: {Number(selectedWallet.balance).toLocaleString('vi-VN')} đ
                </Typography>
              </>
            ) : (
              <Typography color="text.secondary">Chưa có ví hoạt động để tạo QR</Typography>
            )}
            <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
              Đưa mã này cho thu ngân để quét thanh toán
            </Typography>
          </CardContent>
        </Card>

        {/* Thẻ Số dư */}
        <Typography variant="subtitle1" sx={{ mb: 1 }}>Số dư hạn mức</Typography>
        <Grid container spacing={1.5} sx={{ mb: 2 }}>
          {wallets.map((wallet) => (
            <Grid item xs={6} key={wallet.id}>
              <Card
                sx={{
                  height: '100%',
                  cursor: 'pointer',
                  border: wallet.id === selectedWalletId ? '2px solid' : '1px solid transparent',
                  borderColor: wallet.id === selectedWalletId ? 'primary.main' : 'transparent',
                }}
                onClick={() => setSelectedWalletId(wallet.id)}
              >
                <CardContent sx={{ p: 1.5, '&:last-child': { pb: 1.5 } }}>
                  <Typography variant="caption" color="text.secondary" noWrap>
                    {wallet.name}
                  </Typography>
                  <Typography variant="h6" color="primary">
                    {Number(wallet.balance).toLocaleString('vi-VN')} đ
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
          {wallets.length === 0 && (
            <Grid item xs={12}>
              <Typography variant="body2" color="text.secondary">Chưa có ví nào</Typography>
            </Grid>
          )}
        </Grid>

        {/* Lịch sử giao dịch gần đây */}
        <Typography variant="subtitle1" sx={{ mb: 1 }}>Giao dịch gần đây</Typography>
        <Stack spacing={1}>
          {transactions.map((tx) => (
            <Card key={tx.id}>
              <CardContent sx={{ p: 1.5, '&:last-child': { pb: 1.5 } }}>
                <Stack direction="row" justifyContent="space-between" alignItems="center">
                  <Box>
                    <Typography variant="body2" fontWeight={600}>
                      {tx.description || 'Giao dịch'}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {tx.createdAt}
                    </Typography>
                  </Box>
                  <Stack alignItems="flex-end">
                    <Typography
                      variant="body2"
                      fontWeight={600}
                      color={tx.fromWalletId === selectedWalletId ? 'error.main' : 'success.main'}
                    >
                      {tx.fromWalletId === selectedWalletId ? '-' : '+'}
                      {Number(tx.amount).toLocaleString('vi-VN')} đ
                    </Typography>
                    <Chip
                      label={tx.status === 'SUCCESS' ? 'Thành công' : tx.status}
                      size="small"
                      color={tx.status === 'SUCCESS' ? 'success' : 'default'}
                      sx={{ height: 18, fontSize: '0.65rem' }}
                    />
                  </Stack>
                </Stack>
              </CardContent>
            </Card>
          ))}
          {transactions.length === 0 && (
            <Typography variant="body2" color="text.secondary">Chưa có giao dịch nào</Typography>
          )}
        </Stack>
      </Container>
    </>
  );
}
