import SvgColor from '../../../components/svg-color';

const icon = (name) => <SvgColor src={`/assets/icons/navbar/${name}.svg`} sx={{ width: 1, height: 1 }} />;

const navConfig = [
  {
    title: 'Trang Chủ',
    path: '/',
    icon: icon('ic_analytics'),
  },
  {
    title: 'Ví Điện Tử',
    path: '/wallets',
    icon: icon('ic_wallet'),
  },
  {
    title: 'Giao dịch',
    path: '/transfers',
    icon: icon('ic_transfer'),
  },
  {
    title: 'Lịch sử giao dịch',
    path: '/transactions',
    icon: icon('ic_transaction'),
  },
  {
    title: 'Quản Lý Giao Dịch',
    path: '/admin/transactions',
    icon: icon('ic_managetransaction'),
    roles: ['ROLE_ADMIN', 'ROLE_ACCOUNTANT']
  },
  {
    title: 'Quản Lý Ví Điện Tử',
    path: '/admin/wallets',
    icon: icon('ic_managewallet'),
    roles: ['ROLE_ADMIN', 'ROLE_ACCOUNTANT']
  },
  {
    title: 'Quản Lý Hoạt Động',
    path: '/admin/tracking',
    icon: icon('ic_user'),
    roles: ['ROLE_ADMIN']
  },
  {
    title: 'Quản lý cấu hình',
    path: '/admin/fraud-config',
    icon: icon('ic_lock'),
    roles: ['ROLE_ADMIN'],
  },
];

export default navConfig;
