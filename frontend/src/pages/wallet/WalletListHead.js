import { TableCell, TableHead, TableRow } from '@mui/material';
import PropTypes from 'prop-types';

WalletListHead.propTypes = {
  headLabel: PropTypes.array,
};

export default function WalletListHead({ headLabel }) {
  return (
    <TableHead>
      <TableRow>
        {headLabel.map((headCell) => (
          <TableCell
            sx={{ paddingLeft: headCell.firstColumn ? 5 : 2 }}
            key={headCell.id}
            align={headCell.alignRight ? 'right' : 'left'}
          >
            {headCell.label}
          </TableCell>
        ))}
      </TableRow>
    </TableHead>
  );
}
