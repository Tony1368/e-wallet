import React, { useEffect, useState } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  Button,
  Snackbar,
  Alert,
  CircularProgress,
  Box
} from '@mui/material';
import AuthService from '../../services/AuthService';
import axios from '../../services/axios';

const FraudConfig = () => {
  const [configs, setConfigs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editIdx, setEditIdx] = useState(null);
  const [editValue, setEditValue] = useState('');
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  useEffect(() => {
    fetchConfigs();
  }, []);

  const fetchConfigs = async () => {
    setLoading(true);
    try {
      const res = await axios.get('/admin/fraud-config');
      setConfigs(res.data);
    } catch (err) {
      console.error('Error fetching fraud configs:', err);
      setSnackbar({ open: true, message: 'Lỗi khi tải cấu hình.', severity: 'error' });
    }
    setLoading(false);
  };

  const handleEdit = (idx) => {
    setEditIdx(idx);
    setEditValue(configs[idx].value);
  };

  const handleCancel = () => {
    setEditIdx(null);
    setEditValue('');
  };

  const handleSave = async (idx) => {
    const config = configs[idx];
    try {
      await axios.put(`/admin/fraud-config/${config.id}`, {
        ruleName: config.ruleName,
        value: editValue
      });
      setSnackbar({ open: true, message: 'Cập nhật thành công!', severity: 'success' });
      setEditIdx(null);
      fetchConfigs();
    } catch (err) {
      setSnackbar({ open: true, message: 'Cập nhật thất bại.', severity: 'error' });
    }
  };

  const handleSnackbarClose = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  if (!AuthService.isAdmin()) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Bạn không có quyền truy cập trang này.</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Quản lý cấu hình phát hiện gian lận
      </Typography>
      <Card>
        <CardContent>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell><b>Tên tham số</b></TableCell>
                    <TableCell><b>Mô tả</b></TableCell>
                    <TableCell><b>Giá trị</b></TableCell>
                    <TableCell><b>Hành động</b></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {configs.map((config, idx) => (
                    <TableRow key={`${config.id}-${config.ruleName}`}>
                      <TableCell>{config.ruleName}</TableCell>
                      <TableCell>{config.description}</TableCell>
                      <TableCell>
                        {editIdx === idx ? (
                          <TextField
                            value={editValue}
                            onChange={e => setEditValue(e.target.value)}
                            size="small"
                          />
                        ) : (
                          config.value
                        )}
                      </TableCell>
                      <TableCell>
                        {editIdx === idx ? (
                          <>
                            <Button
                              variant="contained"
                              color="primary"
                              size="small"
                              sx={{ mr: 1 }}
                              onClick={() => handleSave(idx)}
                            >
                              Lưu
                            </Button>
                            <Button
                              variant="outlined"
                              color="secondary"
                              size="small"
                              onClick={handleCancel}
                            >
                              Hủy
                            </Button>
                          </>
                        ) : (
                          <Button
                            variant="text"
                            color="primary"
                            size="small"
                            onClick={() => handleEdit(idx)}
                          >
                            Sửa
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={handleSnackbarClose} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default FraudConfig;
