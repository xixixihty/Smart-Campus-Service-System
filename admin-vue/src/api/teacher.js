import request from '@/utils/request'

export const getTeacherList = (params) => request.get('/teachers/admin', { params })
export const getTeacherDetail = (id) => request.get(`/teachers/admin/${id}`)
export const createTeacher = (data) => request.post('/teachers/admin', data)
export const updateTeacher = (data) => request.put('/teachers/admin', data)
export const deleteTeacher = (ids) => request.delete(`/teachers/admin/${ids}`)
export const resetTeacherPassword = (id) => request.put(`/teachers/admin/${id}/reset-password`)
