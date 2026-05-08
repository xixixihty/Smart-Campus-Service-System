import request from '@/utils/request'

export const getBorrowRecordList = (params) => request.get('/borrow-records/admin', { params })
export const getBorrowRecordDetail = (id) => request.get(`/borrow-records/admin/${id}`)
export const getBorrowStatistics = (params) => request.get('/borrow-records/admin/statistics', { params })
