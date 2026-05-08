import request from '@/utils/request'

export const getMyMakeupExams = (params) => request.get('/make-up-exams/user/my', { params })
export const getMakeupExamDetail = (id) => request.get(`/make-up-exams/user/${id}`)
