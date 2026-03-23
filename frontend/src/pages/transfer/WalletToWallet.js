import { LoadingButton } from '@mui/lab';
import { Autocomplete, Button, Card, Grid, Stack, TextField } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useEffect, useState } from 'react';
import { Helmet } from 'react-helmet-async';
import { useNavigate } from 'react-router-dom';
import AuthService from '../../services/AuthService';
import HttpService from '../../services/HttpService';
import ClientInfoService from '../../services/ClientInfoService';

export default function WalletToWallet() {
  const defaultValues = {
    amount: '',
    fromWalletIban: '',
    toWalletIban: '',
    description: '',
    typeId: 1, // Transfer
  };

  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [formValues, setFormValues] = useState(defaultValues);
  const [fromWalletIbans, setFromWalletIbans] = useState([]);
  const [fromWalletIban, setFromWalletIban] = useState();
  const [selectedWallet, setSelectedWallet] = useState(null);
  const [amountError, setAmountError] = useState('');

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
    const userId = AuthService.getCurrentUser()?.id;
    HttpService.getWithAuth(`/wallets/users/${userId}`).then((result) => {
      setFromWalletIbans(result.filter(wallet => wallet.status === 'ACTIVE'));
    });
  }, []);

  const handleWalletChange = (event) => {
    setSelectedWallet(event);
    setFromWalletIban(event?.iban);
    setFormValues({
      ...formValues,
      fromWalletIban: event?.iban,
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validateAmount(formValues.amount)) return;
    if (selectedWallet && parseFloat(formValues.amount) > selectedWallet.balance) {
      enqueueSnackbar('Số điểm không được lớn hơn điểm thưởng khả dụng.', { variant: 'error' });
      return;
    }

    try {
      // Collect client information
      const clientInfo = await ClientInfoService.getAllClientInfo();
      
      // Combine form data with client information
      const transactionData = {
        ...formValues,
        ...clientInfo
      };

    // HttpService.postWithAuth('/wallet/hust/wallets/transfer', formValues)
      const response = await HttpService.postWithAuth('/payments/transfer', transactionData);
      
        if (response?.message) {
          enqueueSnackbar(response.message, { variant: 'warning' });
        } else {
          enqueueSnackbar('Chuyển điểm đến ví điện tử thành công', { variant: 'success' });
        }
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
        <title> Chuyển Điểm Thưởng Từ Ví Đến Ví | Ví Điện Tử HUST </title>
      </Helmet>
      <Card>
        <Grid container alignItems="left" justify="left" direction="column" sx={{ width: 400, padding: 5 }}>
          <Stack spacing={3}>
            <Autocomplete
              ListboxProps={{ style: { maxHeight: 200, overflow: 'auto' } }}
              required
              disablePortal
              id="fromWalletIban"
              noOptionsText="no records"
              options={fromWalletIbans || []}
              getOptionLabel={(fromWalletIban) => fromWalletIban.name}
              isOptionEqualToValue={(option, value) => option?.name === value?.name}
              onChange={(event, newValue) => {
                handleWalletChange(newValue);
              }}
              renderInput={(params) => <TextField {...params} label="Lựa chọn Ví điện tử chuyển" />}
            />
            {selectedWallet && (
              <TextField
                id="availableBalance"
                name="availableBalance"
                label="Điểm Thưởng khả dụng"
                value={selectedWallet.balance.toLocaleString('it-IT', { style: 'currency', currency: 'VND' })}
                disabled
              />
            )}
            <TextField
              id="amount"
              name="amount"
              label="Số Điểm Thưởng"
              autoFocus
              required
              value={formValues.amount}
              onChange={(e) => {
                handleInputChange(e);
                validateAmount(e.target.value);
              }}
              error={!!amountError || (selectedWallet && parseFloat(formValues.amount) > selectedWallet.balance)}
              helperText={
                amountError ||
                (selectedWallet && parseFloat(formValues.amount) > selectedWallet.balance
                  ? 'Số điểm không được lớn hơn điểm thưởng khả dụng.'
                  : '')
              }
              type="text"
              inputProps={{ inputMode: 'decimal', pattern: '[0-9]*' }}
            />
            <TextField
              id="toWalletIban"
              name="toWalletIban"
              label="Số IBAN của Ví điện tử nhận"
              autoComplete="toWalletIban"
              required
              value={formValues.toWalletIban}
              onChange={handleInputChange}
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
