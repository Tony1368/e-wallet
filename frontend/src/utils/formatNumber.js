import numeral from 'numeral';

// ----------------------------------------------------------------------

export function fNumber(number) {
  return numeral(number).format();
}

export function fCurrency(number) {
  const format = number ? numeral(number).format('$0,0.00') : '';

  return result(format, '.00');
}

export function fPercent(number) {
  const format = number ? numeral(Number(number) / 100).format('0.0%') : '';

  return result(format, '.0');
}

export function fShortenNumber(number) {
  const format = number ? numeral(number).format('0.00a') : '';

  return result(format, '.00');
}

export function fData(number) {
  const format = number ? numeral(number).format('0.0 b') : '';

  return result(format, '.0');
}

function result(format, key = '.00') {
  const isInteger = format.includes(key);

  return isInteger ? format.replace(key, '') : format;
}

// ----------------------------------------------------------------------

export function getStatusColor(status) {
  switch (status) {
    case 'SUCCESS':
      return 'success';
    case 'PENDING':
      return 'warning';
    case 'FAILED':
    case 'ERROR':
      return 'error';
    default:
      return 'default';
  }
}

export function getStatusIcon(status) {
  switch (status) {
    case 'SUCCESS':
      return 'eva:checkmark-circle-2-fill';
    case 'PENDING':
      return 'eva:clock-fill';
    case 'FAILED':
    case 'ERROR':
      return 'eva:close-circle-fill';
    default:
      return 'eva:help-circle-fill';
  }
}
