import request from '@/utils/request'

export const getScoreDetail = (id) => request.get(`/score-entries/user/${id}`)
export const getMyScores = (params) => request.get('/score-entries/user/my', { params })
export const getMyGPA = () => request.get('/score-entries/user/gpa')
