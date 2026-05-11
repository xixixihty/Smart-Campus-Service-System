import request from '@/utils/request'

export const borrowBook = (data) => request.post('/borrow-records/user', data)
export const returnBook = (id) => request.post(`/borrow-records/user/${id}/return`)
export const getBorrowDetail = (id) => request.get(`/borrow-records/user/${id}`)
export const getMyBorrows = (params) => request.get('/borrow-records/user/my', { params })
