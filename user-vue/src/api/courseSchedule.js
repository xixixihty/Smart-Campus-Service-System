import request from '@/utils/request'

export const getTimetable = (params) => request.get('/course-schedules/user/timetable', { params })
export const getMyAllCourses = (params) => request.get('/course-schedules/user/my-courses', { params })
