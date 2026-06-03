import request from '@/utils/request'

// 授课班级列表
export const getTeachingClasses = (semesterId) => request.get('/teacher/classes', { params: { semesterId } })

// 班级详情
export const getClassDetail = (classId) => request.get(`/teacher/class/${classId}`)

// 班级学生花名册
export const getClassStudents = (classId) => request.get(`/teacher/class/${classId}/students`)