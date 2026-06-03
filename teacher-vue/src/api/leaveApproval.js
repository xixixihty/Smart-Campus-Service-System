import request from '@/utils/request'

// 待审批请假列表
export const getPendingLeaveRequests = (params) => request.get('/teacher/leave-requests/pending', { params })

// 请假申请详情
export const getLeaveRequestDetail = (id) => request.get(`/teacher/leave-request/${id}`)

// 审批请假
export const approveLeaveRequest = (id, data) => request.post(`/teacher/leave-request/${id}/approve`, data)

// 审批日志
export const getApprovalLogs = (id) => request.get(`/teacher/leave-request/${id}/logs`)