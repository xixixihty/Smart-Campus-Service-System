import request from '@/utils/request'

export const getClassList = (params) => request.get('/classes/admin', { params })
export const getClassDetail = (id) => request.get(`/classes/admin/${id}`)
export const createClass = (data) => request.post('/classes/admin', data)
export const updateClass = (data) => request.put('/classes/admin', data)
export const deleteClass = (ids) => request.delete(`/classes/admin/${ids}`)
export const updateClassStatus = (id) => request.put(`/classes/admin/${id}/status`)
