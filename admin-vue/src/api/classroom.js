import request from '@/utils/request'

export const getClassroomList = (params) => request.get('/classrooms/admin', { params })
export const getClassroomDetail = (id) => request.get(`/classrooms/admin/${id}`)
export const createClassroom = (data) => request.post('/classrooms/admin', data)
export const updateClassroom = (data) => request.put('/classrooms/admin', data)
export const deleteClassroom = (ids) => request.delete(`/classrooms/admin/${ids}`)
