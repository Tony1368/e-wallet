import { LoadingButton } from '@mui/lab';
import { Autocomplete, Button, Card, Container, Grid, Stack, TextField, Typography, CircularProgress } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useEffect, useState } from 'react';
import { Helmet } from 'react-helmet-async';
import { useNavigate } from 'react-router-dom';
import AuthService from '../../services/AuthService';
import HttpService from '../../services/HttpService';
import ClientInfoService from '../../services/ClientInfoService';

export default function AddFunds() {
  const defaultValues = {
    amount: '',
    fromWalletIban: '',
    toWalletIban: '',
    description: '',
    typeId: 4, // Add Funds
  };

  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [formValues, setFormValues] = useState(defaultValues);
  const [toWalletIbans, setFromWalletIbans] = useState([]);
  const [toWalletIban, setFromWalletIban] = useState();
  const [amountError, setAmountError] = useState('');

  const isAdminOrAccountant = AuthService.isAdminOrAccountant();
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [loadingWallets, setLoadingWallets] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormValues({
      ...formValues,
      [name]: value,
    });
  };

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

  useEffect(() => {
    if (isAdminOrAccountant) {
      setLoadingUsers(true);
      HttpService.getWithAuth('/admin/users')
        .then((result) => setUsers(result))
        .catch(() => setUsers([]))
        .finally(() => setLoadingUsers(false));
    } else {
      // Regular user: fetch own wallets
      const userId = AuthService.getCurrentUser()?.id;
      HttpService.getWithAuth(`wallets/users/${userId}`).then((result) => {
        setFromWalletIbans(result.filter(wallet => wallet.status === 'ACTIVE'));
      });
    }
  }, []);

  useEffect(() => {
    if (isAdminOrAccountant && selectedUser) {
      setLoadingWallets(true);
      HttpService.getWithAuth(`wallets/users/${selectedUser.id}`)
        .then((result) => setFromWalletIbans(result.filter(wallet => wallet.status === 'ACTIVE')))
        .catch(() => setFromWalletIbans([]))
        .finally(() => setLoadingWallets(false));
    }
  }, [selectedUser]);

  const handleWalletChange = (event) => {
    setFromWalletIban(event.iban);
    setFormValues({
      ...formValues,
      fromWalletIban: event.iban,
      toWalletIban: event.iban,
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validateAmount(formValues.amount)) return;

    try {
      // Collect client information
      const clientInfo = await ClientInfoService.getAllClientInfo();
      
      // Combine form data with client information
      const transactionData = {
        ...formValues,
        ...clientInfo
      };

      // HttpService.postWithAuth('/wallet/hust/wallets/add-funds', formValues)
      const response = await HttpService.postWithAuth('/wallets/add', transactionData);
      
      enqueueSnackbar('Nạp Điểm Thưởng Vào Ví Điện Tử Thành Công', { variant: 'success' });
      navigate('/transactions');
    } catch (error) {
      if (error.response?.data?.errors) {
        error.response?.data?.errors.map((e) => enqueueSnackbar(e.message, { variant: 'error' }));
      } else if (error.response?.data?.message) {
        enqueueSnackbar(error.response?.data?.message, { variant: 'error' });
      } else {
        enqueueSnackbar(error.message, { variant: 'error' });
      }
    }
  };

  return (
    <>
      <Helmet>
        <title> Nạp Điểm Thưởng Ví Điện Tử | Ví Điện Tử HUST </title>
      </Helmet>
      <Card>
        <Grid container alignItems="left" justify="left" direction="column" sx={{ width: 400, padding: 5 }}>
          <Stack spacing={3}>
            {isAdminOrAccountant && (
              <Autocomplete
                ListboxProps={{ style: { maxHeight: 200, overflow: 'auto' } }}
                id="user-select"
                options={users}
                loading={loadingUsers}
                getOptionLabel={(user) => user ? `${user.firstName || ''} ${user.lastName || ''} (${user.username})` : ''}
                isOptionEqualToValue={(option, value) => option.id === value.id}
                onChange={(event, newValue) => {
                  setSelectedUser(newValue);
                  setFromWalletIbans([]);
                  setFromWalletIban(null);
                  setFormValues({ ...formValues, fromWalletIban: '', toWalletIban: '' });
                }}
                renderInput={(params) => (
                  <TextField {...params} label="Chọn người dùng" required InputProps={{ ...params.InputProps, endAdornment: (<>{loadingUsers ? <CircularProgress color="inherit" size={20} /> : null}{params.InputProps.endAdornment}</>) }} />
                )}
              />
            )}
            <Autocomplete
              ListboxProps={{ style: { maxHeight: 200, overflow: 'auto' } }}
              required
              disablePortal
              id="toWalletIban"
              noOptionsText={loadingWallets ? 'Đang tải...' : 'Không có ví nào'}
              options={toWalletIbans}
              loading={loadingWallets}
              getOptionLabel={(toWalletIban) => toWalletIban ? toWalletIban.name : ''}
              isOptionEqualToValue={(option, value) => option && value && option.id === value.id}
              onChange={(event, newValue) => {
                handleWalletChange(newValue);
              }}
              value={toWalletIbans.find(w => w.iban === formValues.toWalletIban) || null}
              renderInput={(params) => <TextField {...params} label="Lựa chọn Ví điện tử nạp" required InputProps={{ ...params.InputProps, endAdornment: (<>{loadingWallets ? <CircularProgress color="inherit" size={20} /> : null}{params.InputProps.endAdornment}</>) }} />}
            />
            <TextField
              id="amount"
              name="amount"
              label="Số điểm"
              autoFocus
              required
              value={formValues.amount}
              onChange={(e) => {
                handleInputChange(e);
                validateAmount(e.target.value);
              }}
              error={!!amountError}
              helperText={amountError}
              type="text"
              inputProps={{ inputMode: 'decimal', pattern: '[0-9]*' }}
            />
            <TextField
              id="description"
              name="description"
              label="Diễn giải giao dịch"
              autoComplete="description"
              required
              value={formValues.description}
              onChange={handleInputChange}
            />
          </Stack>
          <Stack spacing={2} direction="row" alignItems="right" justifyContent="end" sx={{ mt: 4 }}>
            <Button sx={{ width: 120 }} variant="outlined" onClick={() => navigate('/wallets')}>
              Hủy
            </Button>
            <LoadingButton sx={{ width: 120 }} size="large" type="submit" variant="contained" onClick={handleSubmit} disabled={!!amountError}>
              Xác nhận
            </LoadingButton>
          </Stack>
        </Grid>
      </Card>
    </>
  );
}
