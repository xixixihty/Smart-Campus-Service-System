import request from '@/utils/request'

export const createLeaveRequest = (data) => request.post('/leave-requests/user', data)
export const cancelLeaveRequest = (id) => request.delete(`/leave-requests/user/${id}`)
export const getLeaveDetail = (id) => request.get(`/leave-requests/user/${id}`)
export const getMyLeaveRequests = (params) => request.get('/leave-requests/user/my', { params })
