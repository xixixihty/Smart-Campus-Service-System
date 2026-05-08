import request from '@/utils/request'

export const getSemesterList = (params) => request.get('/semesters/admin', { params })
export const getSemesterDetail = (id) => request.get(`/semesters/admin/${id}`)
export const getCurrentSemester = () => request.get('/semesters/admin/current')
export const createSemester = (data) => request.post('/semesters/admin', data)
export const updateSemester = (data) => request.put('/semesters/admin', data)
export const deleteSemester = (ids) => request.delete(`/semesters/admin/${ids}`)
