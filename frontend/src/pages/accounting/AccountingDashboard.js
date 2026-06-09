import { useState, useEffect } from 'react';
import {
  Container, Card, CardContent, Typography, Button, Stack, Grid, Table, TableHead,
  TableRow, TableCell, TableBody, TableContainer, TextField, LinearProgress, Box,
  Alert, Chip, Paper, TablePagination
} from '@mui/material';
import { Helmet } from 'react-helmet-async';
import HttpService from '../../services/HttpService';

export default function AccountingDashboard() {
  // Batch upload state
  const [file, setFile] = useState(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploading, setUploading] = useState(false);
  const [uploadResult, setUploadResult] = useState(null);

  // Ledger state
  const [entries, setEntries] = useState([]);
  const [ledgerLoading, setLedgerLoading] = useState(false);
  const [filterDate, setFilterDate] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    loadLedgerEntries();
  }, [page, rowsPerPage]); // eslint-disable-line react-hooks/exhaustive-deps

  const loadLedgerEntries = async () => {
    setLedgerLoading(true);
    try {
      const params = `?page=${page}&size=${rowsPerPage}${filterDate ? `&date=${filterDate}` : ''}`;
      const res = await HttpService.getWithAuth(`/accounting/journal-entries${params}`);
      setEntries(Array.isArray(res) ? res : (res?.content || []));
      setTotalElements(res?.totalElements || (Array.isArray(res) ? res.length : 0));
    } catch (err) {
      console.error('Error loading ledger:', err);
    } finally {
      setLedgerLoading(false);
    }
  };

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
    setUploadResult(null);
  };

  const handleBatchUpload = async () => {
    if (!file) return;
    setUploading(true);
    setUploadProgress(0);
    setUploadResult(null);

    const formData = new FormData();
    formData.append('file', file);

    try {
      // Simulate progress for UX
      const progressInterval = setInterval(() => {
        setUploadProgress((prev) => Math.min(prev + 10, 90));
      }, 500);

      const res = await HttpService.postWithAuth('/wallets/batch-credit', formData);

      clearInterval(progressInterval);
      setUploadProgress(100);
      setUploadResult({ success: true, message: `Cấp điểm thành công cho ${res?.processedCount || 'N/A'} nhân viên` });
    } catch (err) {
      setUploadProgress(0);
      setUploadResult({ success: false, message: err.response?.data?.message || 'Lỗi xử lý file' });
    } finally {
      setUploading(false);
    }
  };

  const handleExportExcel = () => {
    window.open(`http://localhost:8080/api/v1/accounting/journal-entries/export?date=${filterDate}`, '_blank');
  };

  const handleErpTransfer = async () => {
    if (!filterDate) return;
    try {
      const res = await HttpService.postWithAuth('/accounting/erp-transfer', { date: filterDate });
      alert(`Kết chuyển ERP thành công! Số bút toán: ${res.transferredCount}, Ngày: ${res.date}`);
      loadLedgerEntries();
    } catch (err) {
      alert('Lỗi kết chuyển: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleFilterByDate = () => {
    setPage(0);
    loadLedgerEntries();
  };

  return (
    <>
      <Helmet>
        <title>Kế toán | Ví điện tử HUST</title>
      </Helmet>
      <Container maxWidth="xl">
        <Typography variant="h4" sx={{ mb: 3 }}>
          Cổng Kế toán & Quản trị
        </Typography>

        <Grid container spacing={3}>
          {/* Batch Processing */}
          <Grid item xs={12} md={4}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Cấp điểm theo lô (Batch)
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Tải lên file Excel/CSV chứa danh sách nhân viên để cấp điểm ăn ca đầu tháng.
                </Typography>

                <Stack spacing={2}>
                  <Button variant="outlined" component="label" fullWidth>
                    {file ? file.name : 'Chọn file Excel/CSV'}
                    <input type="file" hidden accept=".xlsx,.xls,.csv" onChange={handleFileChange} />
                  </Button>

                  <Button
                    variant="contained"
                    onClick={handleBatchUpload}
                    disabled={!file || uploading}
                    fullWidth
                  >
                    {uploading ? 'Đang xử lý...' : 'Bắt đầu cấp điểm'}
                  </Button>

                  {uploading && (
                    <Box>
                      <LinearProgress variant="determinate" value={uploadProgress} sx={{ height: 8, borderRadius: 4 }} />
                      <Typography variant="caption" textAlign="center" display="block" sx={{ mt: 0.5 }}>
                        {uploadProgress}%
                      </Typography>
                    </Box>
                  )}

                  {uploadResult?.success && <Alert severity="success">{uploadResult.message}</Alert>}
                  {uploadResult && !uploadResult.success && <Alert severity="error">{uploadResult.message}</Alert>}
                </Stack>
              </CardContent>
            </Card>
          </Grid>

          {/* Ledger View */}
          <Grid item xs={12} md={8}>
            <Card>
              <CardContent>
                <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                  <Typography variant="h6">Bút toán (Ledger)</Typography>
                  <Stack direction="row" spacing={1}>
                    <TextField
                      type="date"
                      size="small"
                      value={filterDate}
                      onChange={(e) => setFilterDate(e.target.value)}
                      InputLabelProps={{ shrink: true }}
                      label="Lọc theo ngày"
                    />
                    <Button variant="outlined" size="small" onClick={handleFilterByDate}>
                      Lọc
                    </Button>
                    <Button variant="outlined" size="small" color="success" onClick={handleExportExcel}>
                      Xuất Excel
                    </Button>
                    <Button variant="outlined" size="small" color="secondary" onClick={handleErpTransfer} disabled={!filterDate}>
                      Kết chuyển ERP
                    </Button>
                  </Stack>
                </Stack>

                {ledgerLoading && <LinearProgress sx={{ mb: 1 }} />}

                <TableContainer component={Paper} sx={{ maxHeight: 500 }}>
                  <Table stickyHeader size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>ID</TableCell>
                        <TableCell>Mã GD</TableCell>
                        <TableCell>Loại</TableCell>
                        <TableCell>Từ ví</TableCell>
                        <TableCell>Đến ví</TableCell>
                        <TableCell align="right">Số tiền</TableCell>
                        <TableCell>Thời gian</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {entries.map((entry) => (
                        <TableRow key={entry.id} hover>
                          <TableCell>{entry.id}</TableCell>
                          <TableCell>
                            <Typography variant="caption" noWrap sx={{ maxWidth: 120, display: 'block' }}>
                              {entry.transactionId}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={entry.entryType}
                              size="small"
                              color={entry.entryType === 'CREDIT' ? 'success' : 'error'}
                              sx={{ fontSize: '0.7rem' }}
                            />
                          </TableCell>
                          <TableCell>{entry.fromWalletId}</TableCell>
                          <TableCell>{entry.toWalletId}</TableCell>
                          <TableCell align="right">
                            {Number(entry.amount || 0).toLocaleString('vi-VN')} đ
                          </TableCell>
                          <TableCell>{entry.createdAt}</TableCell>
                        </TableRow>
                      ))}
                      {entries.length === 0 && !ledgerLoading && (
                        <TableRow>
                          <TableCell colSpan={7} align="center">
                            <Typography variant="body2" color="text.secondary">Chưa có bút toán nào</Typography>
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>

                <TablePagination
                  component="div"
                  count={totalElements}
                  page={page}
                  onPageChange={(e, newPage) => setPage(newPage)}
                  rowsPerPage={rowsPerPage}
                  onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
                  rowsPerPageOptions={[10, 20, 50, 100]}
                  labelRowsPerPage="Dòng/trang:"
                />
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Container>
    </>
  );
}
