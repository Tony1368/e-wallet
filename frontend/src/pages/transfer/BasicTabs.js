import { Card, Container, Stack } from '@mui/material';
import Box from '@mui/material/Box';
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
import Typography from '@mui/material/Typography';
import PropTypes from 'prop-types';
import * as React from 'react';
import { Helmet } from 'react-helmet-async';
import AddFunds from './AddFunds';
import WalletToWallet from './WalletToWallet';
import WithdrawFunds from './WithdrawFunds';
import CreateQRCode from './CreateQRCode';
import RedeemRewards from './RedeemRewards';
import AuthService from '../../services/AuthService';

function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 0 }}>
          <Typography>{children}</Typography>
        </Box>
      )}
    </div>
  );
}

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.number.isRequired,
  value: PropTypes.number.isRequired,
};

function a11yProps(index) {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`,
  };
}

export default function BasicTabs() {
  const [value, setValue] = React.useState(0);
  const isAdminOrAccountant = AuthService.isAdminOrAccountant();

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  return (
    <>
      <Helmet>
        <title> Giao dịch | Ví điện tử HUST </title>
      </Helmet>
      <Container sx={{ minWidth: '100%' }}>
        <Stack direction="row" alignItems="center" justifyContent="space-between" mb={1}>
          <Typography variant="h4" gutterBottom>
            Giao dịch
          </Typography>
        </Stack>
        <Card>
          <Box sx={{ width: '100%', padding: 0, pt: 1 }}>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
              <Tabs value={value} onChange={handleChange} aria-label="basic tabs example">
                <Tab label="Chuyển Điểm Thưởng" {...a11yProps(0)} />
                {isAdminOrAccountant && <Tab label="Nạp Điểm Thưởng" {...a11yProps(1)} />}
                {isAdminOrAccountant && <Tab label="Rút Điểm Thưởng" {...a11yProps(2)} />}
                <Tab label="Đổi Điểm Thưởng" {...a11yProps(isAdminOrAccountant ? 3 : 1)} />
                <Tab label="Tạo QR Code" {...a11yProps(isAdminOrAccountant ? 4 : 2)} />
              </Tabs>
            </Box>
            <TabPanel value={value} index={0}>
              <WalletToWallet />
            </TabPanel>
            {isAdminOrAccountant && (
              <TabPanel value={value} index={1}>
                <AddFunds />
              </TabPanel>
            )}
            {isAdminOrAccountant && (
              <TabPanel value={value} index={2}>
                <WithdrawFunds />
              </TabPanel>
            )}
            <TabPanel value={value} index={isAdminOrAccountant ? 3 : 1}>
              <RedeemRewards />
            </TabPanel>
            <TabPanel value={value} index={isAdminOrAccountant ? 4 : 2}>
              <CreateQRCode />
            </TabPanel>
          </Box>
        </Card>
      </Container>
    </>
  );
}
