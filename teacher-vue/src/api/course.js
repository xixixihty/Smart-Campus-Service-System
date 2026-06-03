import request from '@/utils/request'

// 所授课程列表
export const getTeachingCourses = (semesterId) => request.get('/teacher/courses', { params: { semesterId } })

// 课程详情
export const getCourseDetail = (id) => request.get(`/teacher/course/${id}`)