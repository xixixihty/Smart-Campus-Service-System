import request from '@/utils/request'

// ========== 新接口（补考场次） ==========
export const getSessionList = (params) => request.get('/makeup-sessions/admin', { params })
export const getSessionDetail = (id) => request.get(`/makeup-sessions/admin/${id}`)
export const createSession = (data) => request.post('/makeup-sessions/admin', data)
export const updateSession = (id, data) => request.put(`/makeup-sessions/admin/${id}`, data)
export const deleteSession = (id) => request.delete(`/makeup-sessions/admin/${id}`)
export const cancelSession = (id) => request.put(`/makeup-sessions/admin/${id}/cancel`)
export const scanCandidates = (params) => request.get('/makeup-sessions/admin/candidates', { params })
export const addStudentToSession = (sessionId, studentId) => request.post(`/makeup-sessions/admin/${sessionId}/students/${studentId}`)
export const removeStudentFromSession = (sessionId, studentId) => request.delete(`/makeup-sessions/admin/${sessionId}/students/${studentId}`)
export const notifyStudents = (sessionId) => request.post(`/makeup-sessions/admin/${sessionId}/notify`)
export const batchInputScore = (sessionId, data) => request.put(`/makeup-sessions/admin/${sessionId}/scores`, data)
export const inputSingleScore = (sessionId, studentId, data) => request.put(`/makeup-sessions/admin/${sessionId}/students/${studentId}/score`, data)

// ========== 旧接口（保留兼容） ==========
export const getMakeupExamList = (params) => request.get('/make-up-exams/admin', { params })
export const getMakeupExamDetail = (id) => request.get(`/make-up-exams/admin/${id}`)
export const createMakeupExam = (data) => request.post('/make-up-exams/admin', data)
export const updateMakeupExam = (data) => request.put('/make-up-exams/admin', data)
export const deleteMakeupExam = (id) => request.delete(`/make-up-exams/admin/${id}`)