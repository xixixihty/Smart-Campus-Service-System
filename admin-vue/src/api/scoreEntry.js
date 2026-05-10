import request from '@/utils/request'

export const getScoreList = (params) => request.get('/score-entries/admin', { params })
export const getScoreDetail = (id) => request.get(`/score-entries/admin/${id}`)
export const createScore = (data) => request.post('/score-entries/admin', data)
export const updateScore = (data) => request.put('/score-entries/admin', data)
export const deleteScore = (id) => request.delete(`/score-entries/admin/${id}`)
export const batchScore = (data) => request.post('/score-entries/admin/batch', data)
