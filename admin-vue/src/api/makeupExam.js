import request from '@/utils/request'

export const getMakeupExamList = (params) => request.get('/make-up-exams/admin', { params })
export const getMakeupExamDetail = (id) => request.get(`/make-up-exams/admin/${id}`)
export const createMakeupExam = (data) => request.post('/make-up-exams/admin', data)
export const updateMakeupExam = (data) => request.put('/make-up-exams/admin', data)
export const deleteMakeupExam = (id) => request.delete(`/make-up-exams/admin/${id}`)
