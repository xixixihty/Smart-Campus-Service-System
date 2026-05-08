import request from '@/utils/request'

export const getAvailableCourses = (params) => request.get('/course-selections/user/available', { params })
export const getMyCourses = (params) => request.get('/course-selections/user/my', { params })
export const selectCourse = (data) => request.post('/course-selections/user', data)
export const dropCourse = (id) => request.delete(`/course-selections/user/${id}`)
export const getSelectionList = (params) => request.get('/course-selections/user', { params })
