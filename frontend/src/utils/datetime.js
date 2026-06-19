export const FMT = Object.freeze({
  MD_HM: 'MM/DD HH:mm',
  MD: 'MM/DD',
  HM: 'HH:mm',
  HMS: 'HH:mm:ss',
});

export function fmtMDHM(dayjs, value) {
  return value ? dayjs(value).format(FMT.MD_HM) : '';
}

export function fmtMD(dayjs, value) {
  return value ? dayjs(value).format(FMT.MD) : '';
}

export function fmtHM(dayjs, value) {
  return value ? dayjs(value).format(FMT.HM) : '';
}

export function fmtHMS(dayjs, value) {
  return value ? dayjs(value).format(FMT.HMS) : '';
}

