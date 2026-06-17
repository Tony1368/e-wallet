import { useState, useEffect } from 'react';
import {
  Container, Card, CardContent, Typography, Button, Stack, Grid, Table, TableHead,
  TableRow, TableCell, TableBody, TableContainer, TextField, LinearProgress, Box,
  Alert, Chip, Paper, TablePagination, MenuItem, Select, FormControl, InputLabel
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
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [totalElements, setTotalElements] = useState(0);
  const [typeFilter, setTypeFilter] = useState('');

  useEffect(() => {
    loadLedgerEntries();
  }, [page, rowsPerPage]); // eslint-disable-line react-hooks/exhaustive-deps

  const loadLedgerEntries = async () => {
    setLedgerLoading(true);
    try {
      let params = `?page=${page}&size=${rowsPerPage}`;
      if (fromDate && toDate) {
        params += `&fromDate=${fromDate}&toDate=${toDate}`;
      } else if (fromDate) {
        params += `&date=${fromDate}`;
      }
      if (typeFilter) {
        params += `&type=${typeFilter}`;
      }
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
      const progressInterval = setInterval(() => {
        setUploadProgress((prev) => Math.min(prev + 10, 90));
      }, 500);

      const user = JSON.parse(localStorage.getItem('user'));
      const res = await (await fetch('http://localhost:8080/api/v1/wallets/batch-credit', {
        method: 'POST',
        headers: { Authorization: `Bearer ${user?.token}` },
        body: formData
      })).json();

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

  const handleExportExcel = async () => {
    try {
      let params = '';
      if (fromDate && toDate) params = `?fromDate=${fromDate}&toDate=${toDate}`;
      else if (fromDate) params = `?date=${fromDate}`;
      const response = await fetch(`http://localhost:8080/api/v1/accounting/journal-entries/export${params}`, {
        headers: { Authorization: `Bearer ${JSON.parse(localStorage.getItem('user'))?.token}` }
      });
      if (!response.ok) throw new Error('Export failed');
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'journal_entries.csv';
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert('Lỗi xuất file: ' + err.message);
    }
  };

  const handleErpTransfer = async () => {
    if (!fromDate) return;
    try {
      const res = await HttpService.postWithAuth('/accounting/erp-transfer', { date: fromDate });
      alert(`Kết chuyển ERP thành công! Số bút toán: ${res.transferredCount}, Ngày: ${res.date}`);
      loadLedgerEntries();
    } catch (err) {
      alert('Lỗi kết chuyển: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleFilter = () => {
    setPage(0);
    loadLedgerEntries();
  };

  const handleClearFilter = () => {
    setFromDate('');
    setToDate('');
    setTypeFilter('');
    setPage(0);
    setTimeout(loadLedgerEntries, 0);
  };

  return (
    <>
      <Helmet>
        <title>Kế toán | Ví điện tử HUST</title>
      </Helmet>
      <Container maxWidth="xl">
        <Typography variant="h4" sx={{ mb: 3 }}>
          Cổng Kế toán &amp; Quản trị
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
                  <Button
                    variant="text"
                    size="small"
                    onClick={async () => {
                      try {
                        const res = await fetch('http://localhost:8080/api/v1/wallets/batch-credit/template', {
                          headers: { Authorization: `Bearer ${JSON.parse(localStorage.getItem('user'))?.token}` }
                        });
                        const blob = await res.blob();
                        const url = window.URL.createObjectURL(blob);
                        const a = document.createElement('a');
                        a.href = url;
                        a.download = 'batch_credit_template.xlsx';
                        a.click();
                        window.URL.revokeObjectURL(url);
                      } catch (err) { alert('Lỗi tải template'); }
                    }}
                  >
                    📥 Tải file mẫu (template)
                  </Button>

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
                <Typography variant="h6" sx={{ mb: 2 }}>Bút toán (Ledger)</Typography>

                {/* Filter */}
                <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }} flexWrap="wrap">
                  <TextField
                    type="date"
                    size="small"
                    value={fromDate}
                    onChange={(e) => setFromDate(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                    label="Từ ngày"
                    sx={{ width: 160 }}
                  />
                  <TextField
                    type="date"
                    size="small"
                    value={toDate}
                    onChange={(e) => setToDate(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                    label="Đến ngày"
                    sx={{ width: 160 }}
                  />
                  <FormControl size="small" sx={{ minWidth: 140 }}>
                    <InputLabel>Loại GD</InputLabel>
                    <Select value={typeFilter} label="Loại GD" onChange={(e) => setTypeFilter(e.target.value)}>
                      <MenuItem value="">Tất cả</MenuItem>
                      <MenuItem value="CHUYEN_DIEM">Chuyển điểm</MenuItem>
                      <MenuItem value="NAP_DIEM">Nạp điểm</MenuItem>
                      <MenuItem value="RUT_DIEM">Rút điểm</MenuItem>
                      <MenuItem value="DOI_THUONG">Đổi thưởng</MenuItem>
                      <MenuItem value="HOAN_DIEM">Hoàn điểm</MenuItem>
                      <MenuItem value="CAP_DIEM_BATCH">Cấp điểm batch</MenuItem>
                    </Select>
                  </FormControl>
                  <Button variant="contained" size="small" onClick={handleFilter}>
                    Lọc
                  </Button>
                  <Button variant="text" size="small" onClick={handleClearFilter}>
                    Xóa lọc
                  </Button>
                  <Box sx={{ flex: 1 }} />
                  <Button variant="outlined" size="small" color="success" onClick={handleExportExcel}>
                    Xuất Excel
                  </Button>
                  <Button variant="outlined" size="small" color="secondary" onClick={handleErpTransfer} disabled={!fromDate}>
                    Kết chuyển ERP
                  </Button>
                </Stack>

                {ledgerLoading && <LinearProgress sx={{ mb: 1 }} />}

                <TableContainer component={Paper} sx={{ maxHeight: 500 }}>
                  <Table stickyHeader size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>ID</TableCell>
                        <TableCell>Mã GD</TableCell>
                        <TableCell>Loại bút toán</TableCell>
                        <TableCell>Loại GD</TableCell>
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
                          <TableCell>
                            <Typography variant="caption">
                              {entry.transactionType || '-'}
                            </Typography>
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
                          <TableCell colSpan={8} align="center">
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
