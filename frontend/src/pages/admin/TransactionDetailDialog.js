import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Paper,
  Typography,
  Box,
} from '@mui/material';
import { sentenceCase } from 'change-case';
import Label from '../../components/label';
import { fCurrency } from '../../utils/formatNumber';

export default function TransactionDetailDialog({ open, onClose, transaction }) {
  if (!transaction) return null;

  const getStatusColor = (status) => {
    switch (status) {
      case 'SUCCESS':
        return 'success';
      case 'PENDING':
        return 'warning';
      case 'FAILED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusLabel = (status) => {
    switch (status) {
      case 'SUCCESS':
        return 'Thành công';
      case 'PENDING':
        return 'Đang xử lý';
      case 'FAILED':
        return 'Thất bại';
      default:
        return sentenceCase(status);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Typography variant="h6">Chi tiết giao dịch</Typography>
      </DialogTitle>
      <DialogContent>
        <TableContainer component={Paper} sx={{ mt: 2 }}>
          <Table>
            <TableBody>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold', width: '40%' }}>ID</TableCell>
                <TableCell>{transaction.id}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Thời gian giao dịch</TableCell>
                <TableCell>{transaction.createdAt}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Loại giao dịch</TableCell>
                <TableCell>{transaction.type?.name || '-'}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Trạng thái</TableCell>
                <TableCell>
                  <Label color={getStatusColor(transaction.status)}>
                    {getStatusLabel(transaction.status)}
                  </Label>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Số Điểm</TableCell>
                <TableCell>
                  {(() => {
                    const formatted = transaction.amount ? fCurrency(transaction.amount) : '';
                    if (formatted.startsWith('$')) {
                      return <><span style={{ color: 'green', fontWeight: 'bold', marginRight: 2 }}>$</span>{formatted.slice(1)} VND</>;
                    }
                    return `${formatted} VND`;
                  })()}
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Mô tả</TableCell>
                <TableCell>{transaction.description || '-'}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Mã tham chiếu</TableCell>
                <TableCell>{transaction.referenceNumber || '-'}</TableCell>
              </TableRow>
              
              {/* Sender Information */}
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5' }} colSpan={2}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                    Thông tin người gửi
                  </Typography>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Tên người chuyển</TableCell>
                <TableCell>
                  {transaction.fromWallet?.user 
                    ? `${transaction.fromWallet.user.firstName} ${transaction.fromWallet.user.lastName}`
                    : '-'
                  }
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Tên ví điện tử người chuyển</TableCell>
                <TableCell>{transaction.fromWallet?.name || '-'}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Số tài khoản IBAN của ví điện tử người chuyển</TableCell>
                <TableCell>{transaction.fromWallet?.iban || '-'}</TableCell>
              </TableRow>
              
              {/* Receiver Information */}
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5' }} colSpan={2}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                    Thông tin người nhận
                  </Typography>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Tên người nhận</TableCell>
                <TableCell>
                  {transaction.toWallet?.user 
                    ? `${transaction.toWallet.user.firstName} ${transaction.toWallet.user.lastName}`
                    : '-'
                  }
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Tên ví điện tử người nhận</TableCell>
                <TableCell>{transaction.toWallet?.name || '-'}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Số tài khoản IBAN của ví điện tử người nhận</TableCell>
                <TableCell>{transaction.toWallet?.iban || '-'}</TableCell>
              </TableRow>
              
              {/* Device and Location Information */}
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5' }} colSpan={2}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                    Thông tin thiết bị và vị trí
                  </Typography>
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>IP thiết bị giao dịch</TableCell>
                <TableCell>{transaction.ipAddress || '-'}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Ứng dụng sử dụng để thực hiện giao dịch</TableCell>
                <TableCell>
                  {transaction.browser ? `${transaction.browser}${transaction.operatingSystem ? ` (${transaction.operatingSystem})` : ''}` : '-'}
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Loại thiết bị</TableCell>
                <TableCell>{transaction.deviceType || '-'}</TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Vị trí</TableCell>
                <TableCell>
                  {transaction.city && transaction.country 
                    ? `${transaction.city}${transaction.region ? `, ${transaction.region}` : ''}, ${transaction.country}`
                    : transaction.country || '-'
                  }
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold' }}>Quốc gia</TableCell>
                <TableCell>{transaction.country || '-'}</TableCell>
              </TableRow>
              {transaction.latitude && transaction.longitude && (
                <TableRow>
                  <TableCell sx={{ fontWeight: 'bold' }}>Tọa độ</TableCell>
                  <TableCell>{transaction.latitude}, {transaction.longitude}</TableCell>
                </TableRow>
              )}
              {transaction.timezone && (
                <TableRow>
                  <TableCell sx={{ fontWeight: 'bold' }}>Múi giờ</TableCell>
                  <TableCell>{transaction.timezone}</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} variant="contained">
          Đóng
        </Button>
      </DialogActions>
    </Dialog>
  );
} 