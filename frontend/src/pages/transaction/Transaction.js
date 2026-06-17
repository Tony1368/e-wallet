import Popover from '@mui/material/Popover';
import MenuItem from '@mui/material/MenuItem';
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
import { useEffect, useState } from 'react';
import { Helmet } from 'react-helmet-async';
import { useNavigate } from 'react-router-dom';
import Iconify from '../../components/iconify';
import Label from '../../components/label';
import Scrollbar from '../../components/scrollbar';
import AuthService from '../../services/AuthService';
import HttpService from '../../services/HttpService';
import TransactionListHead from './TransactionListHead';
import { fCurrency } from '../../utils/formatNumber';
import TransactionDetailDialog from '../admin/TransactionDetailDialog';

const TABLE_HEAD = [
  { id: 'id', label: 'Id', alignRight: false, firstColumn: true },
  { id: 'fromWallet', label: 'Người Gửi', alignRight: false },
  { id: 'toWallet', label: 'Người Nhận', alignRight: false },
  { id: 'amount', label: 'Số Điểm', alignRight: true },
  { id: 'description', label: 'Diễn Giải', alignRight: false },
  { id: 'createdAt', label: 'Ngày giao dịch', alignRight: false },
  { id: 'type', label: 'Giao Dịch', alignRight: false },
  { id: 'status', label: 'Trạng Thái', alignRight: false },
  { id: '' },
];

export default function Transaction() {
  const [open, setOpen] = useState(null);
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState([]);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const [data, setData] = useState([]);
  const [selectedTransaction, setSelectedTransaction] = useState(null);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const navigate = useNavigate();

  const handleOpenMenu = (event, transaction) => {
    setOpen(event.currentTarget);
    setSelectedTransaction(transaction);
  };

  const handleCloseMenu = () => {
    setOpen(null);
  };

  const handleOpenDetail = () => {
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
    setPage(0);
    setRowsPerPage(parseInt(event.target.value, 10));
  };

  const emptyRows = page > 0 ? Math.max(0, (1 + page) * rowsPerPage - data.length) : 0;

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const userId = AuthService.getCurrentUser()?.id;
      // Lấy danh sách ví của user
      const wallets = await HttpService.getWithAuth(`/wallets/users/${userId}`);
      const walletIds = wallets?.map(w => w.id) || [];
      if (walletIds.length === 0) {
        setData([]);
        return;
      }
      // Query giao dịch theo tất cả walletIds
      const response = await HttpService.getWithAuth(`/transactions/wallets?ids=${walletIds.join(',')}`);
      setData(response.content || []);
    } catch (error) {
      if (error?.response?.status === 401) {
        navigate('/login');
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
        <title> Lịch sử giao dịch | Ví điện tử HUST </title>
      </Helmet>
      <Container sx={{ minWidth: '100%' }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" mb={1}>
          <Typography variant="h4" gutterBottom>
            Giao dịch của bạn
          </Typography>
        </Stack>
        <Card>
          <Scrollbar>
            <TableContainer sx={{ minWidth: 800 }}>
              <Table>
                <TransactionListHead headLabel={TABLE_HEAD} />
                <TableBody>
                  {data &&
                    data.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((row) => {
                      const { id, amount, description, createdAt, fromWallet, toWallet, type, status } = row;
                      const selectedRecord = selected.indexOf(id) !== -1;
                      return (
                        <TableRow hover key={id} tabIndex={-1} role="checkbox" selected={selectedRecord}>
                          <TableCell align="left" sx={{ paddingLeft: 5 }}>
                            {id}
                          </TableCell>
                          <TableCell align="left">
                            {fromWallet?.name || `Ví #${fromWallet?.id}`}
                            {fromWallet?.iban && <><br/><span style={{fontSize:'0.75em',color:'#888'}}>{fromWallet.iban.slice(0,4)}***{fromWallet.iban.slice(-4)}</span></>}
                          </TableCell>
                          <TableCell align="left">
                            {toWallet?.name || `Ví #${toWallet?.id}`}
                            {toWallet?.iban && <><br/><span style={{fontSize:'0.75em',color:'#888'}}>{toWallet.iban.slice(0,4)}***{toWallet.iban.slice(-4)}</span></>}
                          </TableCell>
                          <TableCell align="right">
                            {(() => {
                              const formatted = fCurrency(amount);
                              if (formatted.startsWith('$')) {
                                return <><span style={{ color: 'green', fontWeight: 'bold', marginRight: 2 }}>$</span>{formatted.slice(1)}</>;
                              }
                              return formatted;
                            })()}
                          </TableCell>
                          <TableCell align="left">{description}</TableCell>
                          <TableCell align="left">{createdAt}</TableCell>
                          <TableCell align="left">{type.name}</TableCell>
                          <TableCell align="left">
                            <Label color={status === 'SUCCESS' ? 'success' : 'warning'}>{sentenceCase(status)}</Label>
                          </TableCell>
                          <TableCell align="right" width="20">
                            <IconButton size="large" color="inherit" onClick={(event) => handleOpenMenu(event, row)}>
                              <Iconify icon={'eva:more-vertical-fill'} />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  {emptyRows > 0 && (
                    <TableRow style={{ height: 53 * emptyRows }}>
                      <TableCell colSpan={6} />
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
        <MenuItem onClick={handleOpenDetail}>
          <Iconify icon={'material-symbols:info-outline'} sx={{ mr: 2 }} />
          Chi tiết
        </MenuItem>
      </Popover>
      <TransactionDetailDialog open={detailDialogOpen} onClose={handleCloseDetail} transaction={selectedTransaction} />
    </>
  );
}
