import React, { useEffect, useState, useMemo } from 'react';
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
  Select,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from '@mui/material';
import { enqueueSnackbar } from 'notistack';
import { useNavigate } from 'react-router-dom';
import { Helmet } from 'react-helmet-async';
import TextField from '@mui/material/TextField';
import Iconify from '../../components/iconify';
import Scrollbar from '../../components/scrollbar';
import HttpService from '../../services/HttpService';
import WalletListHead from '../wallet/WalletListHead';
import { fCurrency } from '../../utils/formatNumber';
import Label from '../../components/label';

const TABLE_HEAD = [
  { id: 'id', label: 'Id', alignRight: false, firstColumn: true },
  { id: 'user', label: 'Người dùng', alignRight: false },
  { id: 'name', label: 'Tên ví', alignRight: false },
  { id: 'iban', label: 'Tài khoản IBAN', alignRight: false },
  { id: 'bankInfo', label: 'Ngân hàng', alignRight: false },
  { id: 'createdAt', label: 'Ngày tạo', alignRight: false },
  { id: 'status', label: 'Trạng thái', alignRight: false },
  { id: '' },
];

const getWalletStatusChip = (status) => {
  const color = status === 'ACTIVE' ? 'success' : 'error';
  const label = status === 'ACTIVE' ? 'Active' : 'Closed';
  return <Label color={color}>{label}</Label>;
};

export default function AdminWallet() {
  const [open, setOpen] = useState(null);
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState([]);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const [data, setData] = useState([]);
  const [orderBy, setOrderBy] = useState('user');
  const [order, setOrder] = useState('asc');
  const [selectedWallet, setSelectedWallet] = useState(null);
  const [editingWalletId, setEditingWalletId] = useState(null);
  const [walletToUpdate, setWalletToUpdate] = useState(null);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [searchUsername, setSearchUsername] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const navigate = useNavigate();
  const emptyRows = page > 0 ? Math.max(0, (1 + page) * rowsPerPage - data.length) : 0;

  const handleOpenMenu = (event, wallet) => {
    setOpen(event.currentTarget);
    setSelectedWallet(wallet);
  };

  const handleCloseMenu = () => {
    setOpen(null);
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setPage(0);
    setRowsPerPage(parseInt(event.target.value, 10));
  };

  const handleRequestSort = (property) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  const sortData = (array, comparator) => {
    const stabilizedThis = array.map((el, index) => [el, index]);
    stabilizedThis.sort((a, b) => {
      const order = comparator(a[0], b[0]);
      if (order !== 0) return order;
      return a[1] - b[1];
    });
    return stabilizedThis.map((el) => el[0]);
  };

  const getComparator = (order, orderBy) => {
    return order === 'desc'
      ? (a, b) => descendingComparator(a, b, orderBy)
      : (a, b) => -descendingComparator(a, b, orderBy);
  };

  const descendingComparator = (a, b, orderBy) => {
    if (orderBy === 'user') {
      const aName = a.user ? `${a.user.firstName || ''} ${a.user.lastName || ''}`.trim().toLowerCase() : '';
      const bName = b.user ? `${b.user.firstName || ''} ${b.user.lastName || ''}`.trim().toLowerCase() : '';
      if (bName < aName) return -1;
      if (bName > aName) return 1;
      return 0;
    }
    if (b[orderBy] < a[orderBy]) return -1;
    if (b[orderBy] > a[orderBy]) return 1;
    return 0;
  };

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = () => {
    HttpService.getWithAuth('/admin/wallets')
      .then((response) => {
        setData(response.content);
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

  // Group wallets by user
  const groupedWallets = useMemo(() => {
    const groups = {};
    data.forEach((wallet) => {
      const userKey = wallet.user ? `${wallet.user.firstName || ''} ${wallet.user.lastName || ''}`.trim() || wallet.user.username : 'N/A';
      if (!groups[userKey]) groups[userKey] = [];
      groups[userKey].push(wallet);
    });
    return groups;
  }, [data]);

  const sortedData = [...data].sort((a, b) => {
    const aName = a.user ? `${a.user.firstName || ''} ${a.user.lastName || ''}`.trim().toLowerCase() : '';
    const bName = b.user ? `${b.user.firstName || ''} ${b.user.lastName || ''}`.trim().toLowerCase() : '';
    if (aName < bName) return -1;
    if (aName > bName) return 1;
    return 0;
  });

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
    if (!walletToUpdate) return;
    const { wallet, newStatus } = walletToUpdate;
    HttpService.postWithAuth(`/wallets/${wallet.id}/status`, { status: newStatus })
      .then(() => {
        enqueueSnackbar('Trạng thái ví đã được cập nhật.', { variant: 'success' });
        fetchData();
      })
      .catch(() => {
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
      content: `${isClosing ? 'Bạn có muốn Đóng ví điện tử' : 'Bạn có muốn Mở ví điện tử'} "${walletToUpdate.wallet.name}"?`,
    };
  };

  const handleSearch = () => {
    if (searchUsername.trim()) {
      setIsSearching(true);
      HttpService.getWithAuth(`/wallets/search?username=${encodeURIComponent(searchUsername.trim())}`)
        .then((response) => {
          setData(response);
        })
        .catch((error) => {
          enqueueSnackbar('Không tìm thấy ví cho username này.', { variant: 'warning' });
          setData([]);
        });
    } else {
      setIsSearching(false);
      fetchData();
    }
  };

  const handleInputChange = (e) => {
    setSearchUsername(e.target.value);
    if (e.target.value === '') {
      setIsSearching(false);
      fetchData();
    }
  };

  return (
    <>
      <Helmet>
        <title>Quản Lý Ví Điện Tử | E-Wallet</title>
      </Helmet>

      <Container sx={{ minWidth: '100%' }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" mb={3}>
          <Typography variant="h4">Quản Lý Ví Điện Tử</Typography>
          <Stack direction="row" spacing={1}>
            <TextField
              size="small"
              label="Tìm kiếm theo username"
              value={searchUsername}
              onChange={handleInputChange}
              onKeyDown={(e) => { if (e.key === 'Enter') handleSearch(); }}
              variant="outlined"
            />
            <Button variant="contained" onClick={handleSearch}>Tìm kiếm</Button>
          </Stack>
        </Stack>

        <Card>
          <Scrollbar>
            <TableContainer sx={{ minWidth: 800 }}>
              <Table>
                <WalletListHead headLabel={TABLE_HEAD} />
                <TableBody>
                  {sortedData
                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    .map((row) => (
                      <TableRow hover key={row.id} tabIndex={-1}>
                        <TableCell>{row.id}</TableCell>
                        <TableCell>
                          {row.user ? `${row.user.firstName || ''} ${row.user.lastName || ''}`.trim() || row.user.username : 'N/A'}
                        </TableCell>
                        <TableCell>{row.name}</TableCell>
                        <TableCell>{row.iban}</TableCell>
                        <TableCell>{row.bankInfo}</TableCell>
                        <TableCell>{row.createdAt}</TableCell>
                        <TableCell>
                          {editingWalletId === row.id ? (
                            <Select
                              value={row.status}
                              onChange={(e) => handleSelectStatusChange(e, row)}
                              size="small"
                              sx={{ minWidth: 100 }}
                            >
                              <MenuItem value="ACTIVE">Active</MenuItem>
                              <MenuItem value="CLOSED">Closed</MenuItem>
                            </Select>
                          ) : (
                            getWalletStatusChip(row.status)
                          )}
                        </TableCell>
                        <TableCell align="right">
                          <IconButton size="large" color="inherit" onClick={(event) => handleOpenMenu(event, row)}>
                            <Iconify icon={'eva:more-vertical-fill'} />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                  {emptyRows > 0 && (
                    <TableRow style={{ height: 53 * emptyRows }}>
                      <TableCell colSpan={8} />
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Scrollbar>

          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={data.length}
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
      </Popover>

      <Dialog open={confirmDialogOpen} onClose={handleCloseConfirmDialog}>
        <DialogTitle>{getDialogInfo().title}</DialogTitle>
        <DialogContent>
          <DialogContentText>{getDialogInfo().content}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseConfirmDialog}>Hủy</Button>
          <Button onClick={handleConfirmStatusChange} variant="contained" color="primary">Xác nhận</Button>
        </DialogActions>
      </Dialog>
    </>
  );
} 