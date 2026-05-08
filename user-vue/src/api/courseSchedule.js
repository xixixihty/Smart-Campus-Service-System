import request from '@/utils/request'

export const getTimetable = (params) => request.get('/course-schedules/user/timetable', { params })
