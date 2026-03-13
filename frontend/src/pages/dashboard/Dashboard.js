import { Container, Grid, Typography } from '@mui/material';
import { Helmet } from 'react-helmet-async';
import { AppWidgetSummary } from '../../sections/@dashboard/app';
import { fCurrency } from '../../utils/formatNumber';

export default function Dashboard() {
  return (
    <>
      <Helmet>
        <title> Trang tổng quan | Ví điện tử HUST </title>
      </Helmet>
      <Container maxWidth="xl">
        <Typography variant="h4" sx={{ mb: 5 }}>
          Trang tổng quan
        </Typography>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6} md={3}>
            <AppWidgetSummary title="Ví điện tử" total={714000} icon={'ant-design:wallet-outlined'} />
            <Typography variant="body2" sx={{ my: 2 }}>
              (*) Dữ liệu dashboard giả lập
            </Typography>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <AppWidgetSummary title="Người dùng" total={253000} color="warning" icon={'ant-design:user-outlined'} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <AppWidgetSummary
              title="Tổng số Điểm giao dịch"
              total={1352831}
              color="info"
              icon={'ant-design:transaction-outlined'}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <AppWidgetSummary
              title="Giao dịch (hàng tháng)"
              total={123000}
              color="error"
              icon={'ant-design:euro-outlined'}
            />
          </Grid>
        </Grid>
      </Container>
    </>
  );
}
