import {
  Button,
  Card,
  Container,
  IconButton,
  MenuItem,
  Popover,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TablePagination,
  TableRow,
  Typography,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Select,
} from '@mui/material';
import { enqueueSnackbar } from 'notistack';
import { useEffect, useState } from 'react';
import { Helmet } from 'react-helmet-async';
import { Link, useNavigate } from 'react-router-dom';
import Iconify from '../../components/iconify';
import Scrollbar from '../../components/scrollbar';
import AuthService from '../../services/AuthService';
import HttpService from '../../services/HttpService';
import WalletListHead from './WalletListHead';
import { fCurrency } from '../../utils/formatNumber';
import Label from '../../components/label';

const TABLE_HEAD = [
  { id: 'id', label: 'Id', alignRight: false, firstColumn: true },
  { id: 'name', label: 'Tên ví điện tử', alignRight: false },
  { id: 'balance', label: 'Điểm Thưởng', alignRight: false },
  { id: 'userId', label: 'Người dùng', alignRight: false },
  { id: 'iban', label: 'Số tài khoản IBAN liên kết', alignRight: false },
  { id: 'bankInfo', label: 'Ngân hàng', alignRight: false },
  { id: 'status', label: 'Trạng thái', alignRight: false },
  { id: '' },
];

const getWalletStatusChip = (status) => {
  const color = status === 'ACTIVE' ? 'success' : 'error';
  const label = status === 'ACTIVE' ? 'Active' : 'Closed';
  return <Label color={color}>{label}</Label>;
};

export default function Wallet() {
  const [open, setOpen] = useState(null);
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState([]);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const [data, setData] = useState([]);
  const [selectedWallet, setSelectedWallet] = useState(null);
  const [editingWalletId, setEditingWalletId] = useState(null);
  const [walletToUpdate, setWalletToUpdate] = useState(null);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const navigate = useNavigate();
  const emptyRows = page > 0 ? Math.max(0, (1 + page) * rowsPerPage - data.length) : 0;

  const handleOpenMenu = (event, wallet) => {
    setOpen(event.currentTarget);
    setSelectedWallet(wallet);
  };

  const handleCloseMenu = () => {
    setOpen(null);
  };

  const handleEditClick = () => {
    if (selectedWallet) {
      setEditingWalletId(selectedWallet.id);
    }
    handleCloseMenu();
  };

  const handleSelectStatusChange = (event, wallet) => {
    const newStatus = event.target.value;
    if (newStatus !== wallet.status) {
      setWalletToUpdate({ wallet, newStatus });
      setConfirmDialogOpen(true);
    } else {
      setEditingWalletId(null);
    }
  };

  const handleConfirmStatusChange = () => {
    console.log('[DEBUG] walletToUpdate:', walletToUpdate);
    if (!walletToUpdate) return;
    const { wallet, newStatus } = walletToUpdate;

    HttpService.postWithAuth(`/wallets/${wallet.id}/status`, { status: newStatus })
      .then(() => {
        enqueueSnackbar('Trạng thái ví đã được cập nhật.', { variant: 'success' });
        fetchData();
      })
      .catch((error) => {
        enqueueSnackbar('Cập nhật trạng thái ví thất bại.', { variant: 'error' });
      })
      .finally(() => {
        setConfirmDialogOpen(false);
        setEditingWalletId(null);
        setWalletToUpdate(null);
      });
  };

  const handleCloseConfirmDialog = () => {
    setConfirmDialogOpen(false);
    setEditingWalletId(null);
    setWalletToUpdate(null);
  };

  const getDialogInfo = () => {
    if (!walletToUpdate) return { title: '', content: '' };
    const isClosing = walletToUpdate.newStatus === 'CLOSED';
    return {
      title: 'Xác nhận thay đổi trạng thái',
      content: `${isClosing ? 'Bạn có muốn Đóng ví điện tử' : 'Bạn có muốn Mở ví điện tử'} "${
        walletToUpdate.wallet.name
      }"?`,
    };
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setPage(0);
    setRowsPerPage(parseInt(event.target.value, 10));
  };

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = () => {
    const userId = AuthService.getCurrentUser()?.id;
    HttpService.getWithAuth(`/wallets/users/${userId}`)
//    HttpService.getWithAuth(`/wallet/hust/wallets/users/${userId}`)
      .then((response) => {
        setData(response);
      })
      .catch((error) => {
        if (error?.response?.status === 401) {
          navigate('/login');
        } else if (error.response?.data?.errors) {
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
        <title> Ví điện tử | Ví điện tử HUST </title>
      </Helmet>
      <Container sx={{ minWidth: '100%' }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" mb={1}>
          <Typography variant="h4" gutterBottom>
            Ví điện tử của bạn
          </Typography>
          <Button
            variant="contained"
            startIcon={<Iconify icon="eva:plus-fill" />}
            onClick={() => navigate('/wallets/new')}
          >
            Thêm Ví điện tử mới
          </Button>
        </Stack>
        <Card>
          <Scrollbar>
            <TableContainer sx={{ minWidth: 800 }}>
              <Table>
                <WalletListHead headLabel={TABLE_HEAD} />
                <TableBody>
                  {data &&
                    data.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((row) => {
                      console.log('Wallet row:', row);
                      const { id, name, balance, user, iban, bankInfo, status } = row;
                      const selectedRecord = selected.indexOf(name) !== -1;
                      return (
                        <TableRow hover key={id} tabIndex={-1} role="checkbox" selected={selectedRecord}>
                          <TableCell align="left" sx={{ paddingLeft: 5 }}>
                            {id}
                          </TableCell>
                          <TableCell align="left">{name}</TableCell>
                          <TableCell align="left">
                            {(() => {
                              const formatted = fCurrency(balance);
                              if (formatted.startsWith('$')) {
                                return <><span style={{ color: 'green', fontWeight: 'bold', marginRight: 2 }}>$</span>{formatted.slice(1)}</>;
                              }
                              return formatted;
                            })()}
                          </TableCell>
                          <TableCell align="left">
                            {user ? `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username : 'N/A'}
                          </TableCell>
                          <TableCell align="left">{iban}</TableCell>
                          <TableCell align="left">{bankInfo || 'N/A'}</TableCell>
                          <TableCell align="left">
                            {editingWalletId === row.id ? (
                              <Select
                                value={row.status}
                                onChange={(e) => handleSelectStatusChange(e, row)}
                                size="small"
                                sx={{ width: '120px' }}
                                autoFocus
                              >
                                <MenuItem value="ACTIVE">Active</MenuItem>
                                <MenuItem value="CLOSED">Closed</MenuItem>
                              </Select>
                            ) : (
                              getWalletStatusChip(status)
                            )}
                          </TableCell>
                          <TableCell align="right">
                            <IconButton size="large" color="inherit" onClick={(e) => handleOpenMenu(e, row)}>
                              <Iconify icon={'eva:more-vertical-fill'} />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  {emptyRows > 0 && (
                    <TableRow style={{ height: 53 * emptyRows }}>
                      <TableCell colSpan={7} />
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Scrollbar>
          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={data?.length > 0 ? data.length : 0}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
          />
        </Card>
      </Container>
      <Popover
        open={Boolean(open)}
        anchorEl={open}
        onClose={handleCloseMenu}
        anchorOrigin={{ vertical: 'top', horizontal: 'left' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        PaperProps={{
          sx: {
            p: 1,
            width: 140,
            '& .MuiMenuItem-root': {
              px: 1,
              typography: 'body2',
              borderRadius: 0.75,
            },
          },
        }}
      >
        <MenuItem onClick={handleEditClick}>
          <Iconify icon={'material-symbols:edit'} sx={{ mr: 2 }} />
          Chỉnh sửa
        </MenuItem>
        <MenuItem disabled sx={{ color: 'error.main' }}>
          <Iconify icon={'material-symbols:delete-rounded'} sx={{ mr: 2 }} />
          <Link style={{ textDecoration: 'none' }}>Xóa</Link>
        </MenuItem>
      </Popover>
      
      <Dialog open={confirmDialogOpen} onClose={handleCloseConfirmDialog}>
        <DialogTitle>{getDialogInfo().title}</DialogTitle>
        <DialogContent>
          <DialogContentText>{getDialogInfo().content}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseConfirmDialog}>Hủy</Button>
          <Button onClick={handleConfirmStatusChange} color="primary">
            Xác nhận
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
