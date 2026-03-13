import React, { useEffect, useState } from 'react';
import {
  Card,
  Container,
  IconButton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TablePagination,
  TableRow,
  Typography,
} from '@mui/material';
import { sentenceCase } from 'change-case';
import { enqueueSnackbar } from 'notistack';
import { Helmet } from 'react-helmet-async';
import { useNavigate } from 'react-router-dom';
import Iconify from '../../components/iconify';
import Label from '../../components/label';
import Scrollbar from '../../components/scrollbar';
import HttpService from '../../services/HttpService';
import AuthService from '../../services/AuthService';
import TransactionListHead from './TransactionListHead';
import { fCurrency } from '../../utils/formatNumber';

const TABLE_HEAD = [
  { id: 'id', label: 'Id', alignRight: false },
  { id: 'fromWallet', label: 'Người Gửi', alignRight: false },
  { id: 'toWallet', label: 'Người Nhận', alignRight: false },
  { id: 'amount', label: 'Số Điểm', alignRight: true },
  { id: 'description', label: 'Diễn Giải', alignRight: false },
  { id: 'createdAt', label: 'Ngày giao dịch', alignRight: false },
  { id: 'type', label: 'Giao Dịch', alignRight: false },
  { id: 'status', label: 'Trạng Thái', alignRight: false },
  { id: '' },
];

function formatAmount(amount) {
  if (amount === undefined || amount === null) return 'N/A';
  try {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  } catch {
    return amount;
  }
}

function formatDate(dateString) {
  if (!dateString) return 'N/A';
  // If your backend returns 'dd.MM.yyyy HH:mm:ss', parse it manually
  const [date, time] = dateString.split(' ');
  if (!date || !time) return dateString;
  const [day, month, year] = date.split('.');
  return `${year}-${month}-${day} ${time}`;
}

export default function AdminTransaction() {
  const [data, setData] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const navigate = useNavigate();

  useEffect(() => {
    // Check if user is logged in and has admin role
    const user = AuthService.getCurrentUser();
    if (!user || !user.roles.includes('ROLE_ADMIN')) {
      navigate('/login');
      return;
    }

    HttpService.getWithAuth('/admin/transactions')
      .then((response) => {
        if (response?.content) {
          setData(response.content);
          setTotalElements(response.totalElements || 0);
        } else {
          setData([]);
          setTotalElements(0);
        }
      })
      .catch((error) => {
        console.error('API Error:', error);
        if (error?.response?.status === 401) {
          AuthService.logout();
          navigate('/login', { replace: true });
          enqueueSnackbar('Your session has expired. Please log in again.', { variant: 'error' });
        } else if (error.response?.data?.message) {
          enqueueSnackbar(error.response.data.message, { variant: 'error' });
        } else {
          enqueueSnackbar('Failed to load transactions', { variant: 'error' });
        }
      });
  }, [page, rowsPerPage, navigate]);

  const handleChangePage = (event, newPage) => setPage(newPage);
  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  return (
    <>
      <Helmet>
        <title>Quản lý giao dịch | Ví điện tử HUST</title>
      </Helmet>
      <Container sx={{ minWidth: '100%' }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" mb={1}>
          <Typography variant="h4" gutterBottom>
            Quản lý giao dịch
          </Typography>
        </Stack>
        <Card>
          <Scrollbar>
            <TableContainer sx={{ minWidth: 800 }}>
              <Table>
                <TransactionListHead headLabel={TABLE_HEAD} />
                <TableBody>
                  {data.map((row) => {
                    const { id, amount, description, createdAt, fromWallet, toWallet, type, status } = row;
                    const fromUser = fromWallet?.user
                      ? `${fromWallet.user.firstName || ''} ${fromWallet.user.lastName || ''}`.trim() || 'N/A'
                      : 'N/A';
                    const toUser = toWallet?.user
                      ? `${toWallet.user.firstName || ''} ${toWallet.user.lastName || ''}`.trim() || 'N/A'
                      : 'N/A';
                    return (
                      <TableRow hover key={id}>
                        <TableCell align="left">{id ?? 'N/A'}</TableCell>
                        <TableCell align="left">{fromUser}</TableCell>
                        <TableCell align="left">{toUser}</TableCell>
                        <TableCell align="right">{fCurrency(amount)}</TableCell>
                        <TableCell align="left">{description ?? 'N/A'}</TableCell>
                        <TableCell align="left">{formatDate(createdAt)}</TableCell>
                        <TableCell align="left">{type?.name ?? 'N/A'}</TableCell>
                        <TableCell align="left">
                          <Label color={status === 'SUCCESS' ? 'success' : 'warning'}>
                            {sentenceCase(status ?? 'PENDING')}
                          </Label>
                        </TableCell>
                        <TableCell align="right" width="20">
                          <IconButton size="large" color="inherit">
                            <Iconify icon={'eva:more-vertical-fill'} />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          </Scrollbar>
          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={totalElements}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
          />
        </Card>
      </Container>
    </>
  );
} 