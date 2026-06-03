import request from '@/utils/request'

// 个人课表
export const getMyTimetable = (semesterId) => request.get('/teacher/timetable', { params: { semesterId } })

// 排课记录（可过滤星期）
export const getTeachingSchedules = (semesterId, weekDay) =>
  request.get('/teacher/teaching-schedules', { params: { semesterId, weekDay } })