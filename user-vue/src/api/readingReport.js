import request from '@/utils/request'

export const createReadingReport = (data) => request.post('/reading-reports/user', data)
export const getReadingReportList = (params) => request.get('/reading-reports/user', { params })
export const getMyReadingReports = (params) => request.get('/reading-reports/user/my', { params })
