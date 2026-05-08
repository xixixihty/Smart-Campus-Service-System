import request from '@/utils/request'

export const getNoticeDetail = (id) => request.get(`/notices/user/${id}`)
export const getMyNotices = (params) => request.get('/notices/user/my', { params })
