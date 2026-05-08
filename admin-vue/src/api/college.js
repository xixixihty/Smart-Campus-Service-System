import request from '@/utils/request'

export const getCollegeList = (params) => request.get('/colleges/admin', { params })
export const getCollegeDetail = (id) => request.get(`/colleges/admin/${id}`)
export const createCollege = (data) => request.post('/colleges/admin', data)
export const updateCollege = (data) => request.put('/colleges/admin', data)
export const deleteCollege = (ids) => request.delete(`/colleges/admin/${ids}`)
