import request from '@/utils/request'

export const getStudentList = (params) => request.get('/students/admin', { params })
export const getStudentDetail = (id) => request.get(`/students/admin/${id}`)
export const createStudent = (data) => request.post('/students/admin', data)
export const updateStudent = (data) => request.put('/students/admin', data)
export const deleteStudent = (ids) => request.delete(`/students/admin/${ids}`)
