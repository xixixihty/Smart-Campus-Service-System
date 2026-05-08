import request from '@/utils/request'

export const getMajorList = (params) => request.get('/majors/admin', { params })
export const getMajorDetail = (id) => request.get(`/majors/admin/${id}`)
export const createMajor = (data) => request.post('/majors/admin', data)
export const updateMajor = (data) => request.put('/majors/admin', data)
export const deleteMajor = (ids) => request.delete(`/majors/admin/${ids}`)
