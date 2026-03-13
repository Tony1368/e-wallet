import React, { useState, useEffect } from 'react';
import {
  Card,
  Container,
  Stack,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  CircularProgress,
  Grid,
  Box,
  Chip,
  IconButton,
  Tooltip,
  Select,
  MenuItem,
  InputLabel,
  FormControl
} from '@mui/material';
import { Helmet } from 'react-helmet-async';
import { useSnackbar } from 'notistack';
import { useNavigate } from 'react-router-dom';
import Iconify from '../../components/iconify';
import AuthService from '../../services/AuthService';
import HttpService from '../../services/HttpService';

// Mock data for rewards - in real implementation, this would come from API
const REWARDS_DATA = [
  {
    id: 1,
    name: 'Khóa học Online',
    category: 'Education',
    pointsRequired: 1000,
    description: 'Khóa học trực tuyến về công nghệ thông tin',
    available: true,
    maxQuantity: 50,
    currentQuantity: 25,
    image: '/assets/images/rewards/course.jpg'
  },
  {
    id: 2,
    name: 'Voucher Ăn Uống',
    category: 'Food',
    pointsRequired: 500,
    description: 'Voucher giảm giá 20% tại các nhà hàng đối tác',
    available: true,
    maxQuantity: 100,
    currentQuantity: 80,
    image: '/assets/images/rewards/food.jpg'
  },
  {
    id: 3,
    name: 'Quà Tặng Công Nghệ',
    category: 'Gift',
    pointsRequired: 2000,
    description: 'Tai nghe Bluetooth cao cấp',
    available: true,
    maxQuantity: 20,
    currentQuantity: 5,
    image: '/assets/images/rewards/tech.jpg'
  },
  {
    id: 4,
    name: 'Dịch Vụ Spa',
    category: 'Service',
    pointsRequired: 1500,
    description: 'Gói dịch vụ spa thư giãn 2 giờ',
    available: true,
    maxQuantity: 30,
    currentQuantity: 15,
    image: '/assets/images/rewards/spa.jpg'
  },
  {
    id: 5,
    name: 'Voucher Mua Sắm',
    category: 'Shopping',
    pointsRequired: 800,
    description: 'Voucher giảm giá 15% tại các cửa hàng đối tác',
    available: true,
    maxQuantity: 200,
    currentQuantity: 150,
    image: '/assets/images/rewards/shopping.jpg'
  }
];

const getCategoryColor = (category) => {
  const colors = {
    'Education': 'primary',
    'Food': 'success',
    'Gift': 'warning',
    'Service': 'info',
    'Shopping': 'secondary'
  };
  return colors[category] || 'default';
};

export default function RedeemRewards() {
  const [rewards, setRewards] = useState([]);
  const [userWallets, setUserWallets] = useState([]);
  const [selectedWalletId, setSelectedWalletId] = useState('');
  const [selectedReward, setSelectedReward] = useState(null);
  const [redeemDialogOpen, setRedeemDialogOpen] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [userPoints, setUserPoints] = useState(0);
  const { enqueueSnackbar } = useSnackbar();
  const navigate = useNavigate();
  const currentUser = AuthService.getCurrentUser();
  const isEmployee = currentUser?.roles?.some(role => 
    typeof role === 'string' ? role === 'ROLE_USER' : role.name === 'ROLE_USER'
  );
  const isCustomer = currentUser?.roles?.some(role => 
    typeof role === 'string' ? role === 'ROLE_CUSTOMER' : role.name === 'ROLE_CUSTOMER'
  );
  const isAdmin = currentUser?.roles?.some(role => 
    typeof role === 'string' ? role === 'ROLE_ADMIN' : role.name === 'ROLE_ADMIN'
  );

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    // When wallets are loaded, select the first wallet by default
    if (userWallets.length > 0 && !selectedWalletId) {
      setSelectedWalletId(userWallets[0].id);
    }
  }, [userWallets]);

  useEffect(() => {
    // Update points when selected wallet changes
    if (selectedWalletId && userWallets.length > 0) {
      const wallet = userWallets.find(w => w.id === selectedWalletId);
      setUserPoints(wallet ? parseFloat(wallet.balance) : 0);
    }
  }, [selectedWalletId, userWallets]);

  const loadData = async () => {
    try {
      setLoading(true);
      // Load user wallets
      const userId = currentUser?.id;
      const walletsResponse = await HttpService.getWithAuth(`/wallets/users/${userId}`);
      setUserWallets(walletsResponse.filter(w => w.status === 'ACTIVE'));
      // In real implementation, load rewards from API
      setRewards(REWARDS_DATA);
    } catch (error) {
      console.error('Error loading data:', error);
      enqueueSnackbar('Không thể tải dữ liệu phần thưởng', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleRedeemClick = (reward) => {
    setSelectedReward(reward);
    setQuantity(1);
    setRedeemDialogOpen(true);
  };

  const handleRedeemConfirm = async () => {
    if (!selectedReward) return;
    
    const totalPointsNeeded = selectedReward.pointsRequired * quantity;
    
    if (totalPointsNeeded > userPoints) {
      enqueueSnackbar('Bạn không đủ điểm để đổi phần thưởng này', { variant: 'error' });
      return;
    }
    
    try {
      // Call API to redeem
      await HttpService.postWithAuth('/rewards/redeem', {
        rewardId: selectedReward.id,
        quantity: quantity,
        userId: currentUser.id,
        walletId: selectedWalletId
      });
      
      enqueueSnackbar(`Đổi thưởng thành công! Bạn đã đổi ${quantity} ${selectedReward.name}`, { variant: 'success' });
      
      // Reload wallets to get updated balance
      await loadData();
      
      // Update reward availability
      setRewards(prev => prev.map(reward => 
        reward.id === selectedReward.id 
          ? { ...reward, currentQuantity: reward.currentQuantity - quantity }
          : reward
      ));
      
      setRedeemDialogOpen(false);
      setSelectedReward(null);
      
    } catch (error) {
      console.error('Error redeeming reward:', error);
      enqueueSnackbar('Có lỗi xảy ra khi đổi thưởng', { variant: 'error' });
    }
  };

  const handleQuantityChange = (event) => {
    const newQuantity = parseInt(event.target.value);
    if (newQuantity > 0 && newQuantity <= selectedReward?.maxQuantity) {
      setQuantity(newQuantity);
    }
  };

  const getMaxQuantity = () => {
    if (!selectedReward) return 1;
    const maxByPoints = Math.floor(userPoints / selectedReward.pointsRequired);
    const maxByAvailability = selectedReward.maxQuantity - selectedReward.currentQuantity;
    return Math.min(maxByPoints, maxByAvailability);
  };

  if (loading) {
    return (
      <Container sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <>
      <Helmet>
        <title>Đổi Điểm Thưởng | Ví điện tử HUST</title>
      </Helmet>
      <Container sx={{ minWidth: '100%' }}>
        {/* User Info Section */}
        <Card sx={{ mb: 3, p: 2 }}>
          <Grid container spacing={2} alignItems="center" justifyContent="space-between">
            <Grid item xs={12} md={4}>
              <FormControl fullWidth size="small">
                <InputLabel id="wallet-select-label">Chọn ví điện tử</InputLabel>
                <Select
                  labelId="wallet-select-label"
                  id="wallet-select"
                  value={selectedWalletId}
                  label="Chọn ví điện tử"
                  onChange={e => setSelectedWalletId(e.target.value)}
                >
                  {userWallets.map(wallet => (
                    <MenuItem key={wallet.id} value={wallet.id}>
                      {wallet.name} ({wallet.iban})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={4}>
              <Stack direction="row" alignItems="center" spacing={1} justifyContent="center">
                <Typography variant="subtitle2" color="text.secondary">
                  Điểm hiện có:
                </Typography>
                <Typography variant="h6" color="primary">
                  {userPoints.toLocaleString()}
                </Typography>
              </Stack>
            </Grid>
            <Grid item xs={12} md={4}>
              <Stack direction="row" alignItems="center" justifyContent="flex-end" spacing={1}>
                <Typography variant="subtitle2" color="text.secondary">
                  Vai trò:
                </Typography>
                <Typography variant="h6" color="secondary">
                  {isAdmin ? 'Quản trị viên' : isEmployee ? 'Nhân viên' : isCustomer ? 'Khách hàng' : 'Người dùng'}
                </Typography>
              </Stack>
            </Grid>
          </Grid>
        </Card>

        <Alert severity="info" sx={{ mb: 3 }}>
          <Typography variant="body2">
            {isEmployee ? (
              <>
                <ol style={{ margin: 0, paddingLeft: 20 }}>
                  <li>Chỉ đổi quà bằng điểm nội bộ, không dùng tiền mặt.</li>
                  <li>Điểm sử dụng để đổi quà không có giá trị quy đổi ngược lại thành tiền.</li>
                  <li>Đối với Nhân viên, quà tặng đổi được không chuyển nhượng và không bán lại.</li>
                </ol>
              </>
            ) : isCustomer ? (
              <>
                <ol style={{ margin: 0, paddingLeft: 20 }}>
                  <li>Chỉ đổi quà bằng điểm nội bộ, không dùng tiền mặt.</li>
                  <li>Điểm sử dụng để đổi quà không có giá trị quy đổi ngược lại thành tiền.</li>
                  <li>Khách hàng chỉ được đổi quà bằng điểm thưởng tích lũy từ sử dụng sản phẩm/dịch vụ nội bộ.</li>
                </ol>
              </>
            ) : (
              'Bạn có thể đổi điểm cho các phần thưởng phù hợp.'
            )}
          </Typography>
        </Alert>

        <Card>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Phần Thưởng</TableCell>
                  <TableCell>Danh Mục</TableCell>
                  <TableCell align="right">Điểm Cần Thiết</TableCell>
                  <TableCell align="center">Tình Trạng</TableCell>
                  <TableCell align="center">Số Lượng Còn Lại</TableCell>
                  <TableCell align="center">Thao Tác</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rewards.map((reward) => {
                  const canRedeem = reward.available && 
                    reward.currentQuantity > 0 && 
                    userPoints >= reward.pointsRequired;
                  
                  return (
                    <TableRow key={reward.id} hover>
                      <TableCell>
                        <Stack direction="row" spacing={2} alignItems="center">
                          <Box
                            component="img"
                            src={reward.image}
                            alt={reward.name}
                            sx={{ width: 50, height: 50, borderRadius: 1, objectFit: 'cover' }}
                          />
                          <Box>
                            <Typography variant="subtitle2">{reward.name}</Typography>
                            <Typography variant="body2" color="text.secondary">
                              {reward.description}
                            </Typography>
                          </Box>
                        </Stack>
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={reward.category} 
                          color={getCategoryColor(reward.category)}
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="subtitle2" color="primary">
                          {reward.pointsRequired.toLocaleString()}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip 
                          label={reward.available ? 'Có sẵn' : 'Hết hàng'} 
                          color={reward.available ? 'success' : 'error'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Typography variant="body2">
                          {reward.currentQuantity}/{reward.maxQuantity}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Tooltip title="Đổi thưởng">
                          <IconButton
                            color="primary"
                            onClick={() => handleRedeemClick(reward)}
                            disabled={!canRedeem}
                          >
                            <Iconify icon="eva:gift-fill" />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>

        {/* Redeem Dialog */}
        <Dialog open={redeemDialogOpen} onClose={() => setRedeemDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>
            Đổi Thưởng: {selectedReward?.name}
          </DialogTitle>
          <DialogContent>
            <Stack spacing={3} sx={{ mt: 2 }}>
              <Box>
                <Typography variant="body2" color="text.secondary">
                  Mô tả: {selectedReward?.description}
                </Typography>
              </Box>
              
              <Box>
                <Typography variant="body2" color="text.secondary">
                  Điểm cần thiết: {selectedReward?.pointsRequired?.toLocaleString()} điểm/phần
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Điểm hiện có: {userPoints.toLocaleString()} điểm
                </Typography>
              </Box>

              <TextField
                label="Số lượng"
                type="number"
                value={quantity}
                onChange={handleQuantityChange}
                inputProps={{ 
                  min: 1, 
                  max: getMaxQuantity() 
                }}
                helperText={`Tối đa: ${getMaxQuantity()} phần`}
              />

              <Alert severity="info">
                <Typography variant="body2">
                  Tổng điểm cần: {(selectedReward?.pointsRequired * quantity).toLocaleString()} điểm
                </Typography>
              </Alert>
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setRedeemDialogOpen(false)}>
              Hủy
            </Button>
            <Button 
              onClick={handleRedeemConfirm} 
              variant="contained"
              disabled={quantity <= 0 || quantity > getMaxQuantity()}
            >
              Xác Nhận Đổi Thưởng
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </>
  );
} 