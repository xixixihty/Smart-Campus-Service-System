import request from '@/utils/request'

export const getCourseScheduleList = (params) => request.get('/course-schedules/admin', { params })
export const getCourseScheduleDetail = (id) => request.get(`/course-schedules/admin/${id}`)
export const createCourseSchedule = (data) => request.post('/course-schedules/admin', data)
export const updateCourseSchedule = (data) => request.put('/course-schedules/admin', data)
export const deleteCourseSchedule = (ids) => request.delete(`/course-schedules/admin/${ids}`)
export const conflictCheck = (data) => request.post('/course-schedules/admin/conflict-check', data)
