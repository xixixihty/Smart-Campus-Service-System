import request from '@/utils/request'

export const getLeaveRequestList = (params) => request.get('/leave-requests/admin', { params })
export const getLeaveRequestDetail = (id) => request.get(`/leave-requests/admin/${id}`)
export const approveLeaveRequest = (id, data) => request.post(`/leave-requests/admin/${id}/approve`, data)
