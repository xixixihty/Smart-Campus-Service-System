import request from '@/utils/request'

export const getNoticeList = (params) => request.get('/notices/admin', { params })
export const getNoticeDetail = (id) => request.get(`/notices/admin/${id}`)
export const createNotice = (data) => request.post('/notices/admin', data)
export const updateNotice = (id, data) => request.put(`/notices/admin/${id}`, data)
export const withdrawNotice = (id) => request.post(`/notices/admin/${id}/withdraw`)
export const deleteNotice = (id) => request.delete(`/notices/admin/${id}`)
