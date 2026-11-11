import request from '@/utils/request'

// ========== 新接口（我的补考） ==========
export const getMyMakeupSessions = (params) => request.get('/makeup-sessions/user/my', { params })
export const getSessionDetail = (id) => request.get(`/makeup-sessions/user/${id}`)

// ========== 旧接口（保留兼容） ==========
export const getMyMakeupExams = (params) => request.get('/make-up-exams/user/my', { params })
export const getMakeupExamDetail = (id) => request.get(`/make-up-exams/user/${id}`)