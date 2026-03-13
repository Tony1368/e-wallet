import {
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
  TextField,
  Button,
} from '@mui/material';
import { enqueueSnackbar } from 'notistack';
import { useEffect, useState } from 'react';
import { Helmet } from 'react-helmet-async';
import { useNavigate } from 'react-router-dom';
import { sentenceCase } from 'change-case';
import { QRCodeSVG } from 'qrcode.react';
import Iconify from '../../components/iconify';
import Scrollbar from '../../components/scrollbar';
import HttpService from '../../services/HttpService';
import TransactionListHead from '../transaction/TransactionListHead';
import Label from '../../components/label';
import AuthService from '../../services/AuthService';
import TransactionDetailDialog from './TransactionDetailDialog';

const TABLE_HEAD = [
  { id: 'id', label: 'Id', alignRight: false, firstColumn: true },
  { id: 'type', label: 'Loại giao dịch', alignRight: false },
  { id: 'fromWallet', label: 'Ví điện tử người gửi', alignRight: false },
  { id: 'toWallet', label: 'Ví điện tử người nhận', alignRight: false },
  { id: 'amount', label: 'Số điểm', alignRight: true },
  { id: 'description', label: 'Mô tả', alignRight: false },
  { id: 'createdAt', label: 'Thời gian', alignRight: false },
  { id: 'status', label: 'Trạng thái', alignRight: false },
  { id: '' },
];

export default function AdminTransaction() {
  const [open, setOpen] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const [data, setData] = useState([]);
  const [selectedTransaction, setSelectedTransaction] = useState(null);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [searchUsername, setSearchUsername] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [totalElements, setTotalElements] = useState(0);
  const [searchResults, setSearchResults] = useState(null);
  const navigate = useNavigate();
  const emptyRows = page > 0 ? Math.max(0, (1 + page) * rowsPerPage - data.length) : 0;

  const handleOpenMenu = (event) => {
    setOpen(event.currentTarget);
  };

  const handleCloseMenu = () => {
    setOpen(null);
  };

  const handleOpenDetail = (transaction) => {
    setSelectedTransaction(transaction);
    setDetailDialogOpen(true);
    setOpen(null);
  };

  const handleCloseDetail = () => {
    setDetailDialogOpen(false);
    setSelectedTransaction(null);
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    const newSize = parseInt(event.target.value, 10);
    setRowsPerPage(newSize);
    setPage(0);
  };

  const handleSearch = () => {
    if (searchUsername.trim()) {
      setIsSearching(true);
      HttpService.getWithAuth(`/admin/transactions/search?username=${encodeURIComponent(searchUsername.trim())}`)
        .then((response) => {
          const results = Array.isArray(response) ? response : response.content || [];
          setSearchResults(results);
          setTotalElements(results.length);
          setPage(0);
          setIsSearching(false);
        })
        .catch((error) => {
          enqueueSnackbar('Không tìm thấy giao dịch cho username này.', { variant: 'warning' });
          setSearchResults([]);
          setTotalElements(0);
          setPage(0);
          setIsSearching(false);
        });
    } else {
      setIsSearching(false);
      setSearchResults(null);
      fetchData();
    }
  };

  const handleInputChange = (e) => {
    setSearchUsername(e.target.value);
    if (e.target.value === '') {
      setIsSearching(false);
      setSearchResults(null);
      fetchData();
    }
  };

  // Log current user info for debugging
  const currentUser = AuthService.getCurrentUser();
  console.log('Current user:', currentUser);

  const fetchData = (pageParam = page, sizeParam = rowsPerPage) => {
    console.log('Fetching /admin/transactions...');
    HttpService.getWithAuth(`/admin/transactions?page=${pageParam}&size=${sizeParam}`)
      .then((response) => {
        console.log('API response:', response);
        setData(response.content);
        setTotalElements(response.totalElements);
      })
      .catch((error) => {
        console.error('API error:', error);
        if (error?.response?.status === 401) {
          console.warn('Redirecting to /login due to 401');
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

  useEffect(() => {
    fetchData(page, rowsPerPage);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      <Helmet>
        <title>Quản Lý Giao Dịch | E-Wallet</title>
      </Helmet>

      <Container sx={{ minWidth: '100%' }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" mb={3}>
          <Typography variant="h4">Quản Lý Giao Dịch</Typography>
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
                <TransactionListHead
                  rowCount={data.length}
                  headLabel={TABLE_HEAD}
                />
                <TableBody>
                  {(searchResults !== null ? searchResults : data)
                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    .map((row) => (
                      <TableRow hover key={row.id} tabIndex={-1}>
                        <TableCell>{row.id}</TableCell>
                        <TableCell>{row.type?.name || '-'}</TableCell>
                        <TableCell>
                          {row.fromWallet?.name || '-'}
                          <br />
                          <span style={{ fontSize: '0.8em', color: '#888' }}>{row.fromWallet?.iban || ''}</span>
                        </TableCell>
                        <TableCell>
                          {row.toWallet?.name || '-'}
                          <br />
                          <span style={{ fontSize: '0.8em', color: '#888' }}>{row.toWallet?.iban || ''}</span>
                        </TableCell>
                        <TableCell align="right">
                          <span style={{ color: 'green', fontWeight: 'bold', marginRight: 2 }}>$</span>{row.amount?.toLocaleString()}
                        </TableCell>
                        <TableCell>{row.description}</TableCell>
                        <TableCell>{row.createdAt}</TableCell>
                        <TableCell>
                          <Label color={row.status === 'SUCCESS' ? 'success' : 'warning'}>
                            {sentenceCase(row.status)}
                          </Label>
                        </TableCell>
                        <TableCell align="right">
                          <IconButton size="large" color="inherit" onClick={(event) => {
                            setOpen(event.currentTarget);
                            setSelectedTransaction(row);
                          }}>
                            <Iconify icon={'eva:more-vertical-fill'} />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))}

                  {emptyRows > 0 && (
                    <TableRow style={{ height: 53 * emptyRows }}>
                      <TableCell colSpan={9} />
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Scrollbar>

          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={searchResults !== null ? searchResults.length : totalElements}
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
        <MenuItem onClick={() => handleOpenDetail(selectedTransaction)}>
          <Iconify icon={'material-symbols:info-outline'} sx={{ mr: 2 }} />
          Chi tiết
        </MenuItem>
        <MenuItem disabled>
          <Iconify icon={'material-symbols:edit'} sx={{ mr: 2 }} />
          Chỉnh sửa
        </MenuItem>
        <MenuItem disabled sx={{ color: 'error.main' }}>
          <Iconify icon={'material-symbols:delete-rounded'} sx={{ mr: 2 }} />
          Xóa
        </MenuItem>
      </Popover>

      <TransactionDetailDialog
        open={detailDialogOpen}
        onClose={handleCloseDetail}
        transaction={selectedTransaction}
      />
    </>
  );
} 