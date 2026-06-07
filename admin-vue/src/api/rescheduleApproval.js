import request from '@/utils/request'

export const getPendingReschedules = (params) => request.get('/admin/reschedules/pending', { params })
export const getRescheduleDetail = (id) => request.get(`/admin/reschedule/${id}`)
export const approveReschedule = (id) => request.post(`/admin/reschedule/${id}/approve`)
export const rejectReschedule = (id, data) => request.post(`/admin/reschedule/${id}/reject`, data)
