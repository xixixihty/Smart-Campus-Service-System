import request from '@/utils/request'

export const getCourseList = (params) => request.get('/courses/admin', { params })
export const getCourseDetail = (id) => request.get(`/courses/admin/${id}`)
export const createCourse = (data) => request.post('/courses/admin', data)
export const updateCourse = (data) => request.put('/courses/admin', data)
export const deleteCourse = (ids) => request.delete(`/courses/admin/${ids}`)
