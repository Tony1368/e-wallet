import { LoadingButton } from '@mui/lab';
import { Button, Card, Container, Grid, Stack, TextField, Typography } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useState } from 'react';
import { Helmet } from 'react-helmet-async';
import { useNavigate } from 'react-router-dom';
import AuthService from '../../services/AuthService';
import HttpService from '../../services/HttpService';

export default function NewWallet() {
  const defaultValues = {
    name: '',
    balance: '',
    iban: '',
    email: '',
    userId: AuthService.getCurrentUser()?.id,
  };

  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [formValues, setFormValues] = useState(defaultValues);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormValues({
      ...formValues,
      [name]: value,
    });
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    
    // HttpService.postWithAuth('/wallet/hust/wallets', formValues)
    HttpService.postWithAuth('/wallets', formValues)
      .then((response) => {
        enqueueSnackbar('Tạo và liên kết ví điện tử thành công', { variant: 'success' });
        navigate('/wallets');
      })
      .catch((error) => {
        if (error.response?.data?.errors) {
          error.response?.data?.errors.map((e) => enqueueSnackbar(e.message, { variant: 'error' }));
        } else if (error.response?.data?.message) {
          enqueueSnackbar(error.response?.data?.message, { variant: 'error' });
        } else {
          enqueueSnackbar(error.message, { variant: 'error' });
        }
      });
  };

  return (
    <>
      <Helmet>
        <title> Thêm ví điện tử | Ví điện tử HUST </title>
      </Helmet>
      <Container sx={{ minWidth: '100%' }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" mb={1}>
          <Typography variant="h4" gutterBottom>
            Thêm ví điện tử mới
          </Typography>
        </Stack>
        <Card>
          <Grid container alignItems="left" justify="center" direction="column" sx={{ width: 400, padding: 5 }}>
            <Stack spacing={3}>
              <TextField
                id="name"
                name="name"
                label="Tên Ví điện tử"
                autoComplete="given-name"
                autoFocus
                required
                value={formValues.name}
                onChange={handleInputChange}
              />
              <TextField
                id="iban"
                name="iban"
                label="Số tài khoản IBAN"
                autoComplete="iban"
                required
                value={formValues.iban}
                onChange={handleInputChange}
              />
              <TextField
                id="balance"
                name="balance"
                label="Số Điểm"
                autoComplete="balance"
                required
                value={formValues.balance}
                onChange={handleInputChange}
              />
            </Stack>
            <Stack spacing={2} direction="row" alignItems="right" justifyContent="end" sx={{ mt: 4 }}>
              <Button sx={{ width: 120 }} variant="outlined" onClick={() => navigate('/wallets')}>
                Hủy
              </Button>
              <LoadingButton sx={{ width: 120 }} size="large" type="submit" variant="contained" onClick={handleSubmit}>
                Thêm mới
              </LoadingButton>
            </Stack>
          </Grid>
        </Card>
      </Container>
    </>
  );
}
