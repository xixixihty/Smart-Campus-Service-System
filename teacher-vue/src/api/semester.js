import request from '@/utils/request'

// 学期列表
export const getSemesterList = (params) => request.get('/teacher/semesters', { params })

// 当前学期
export const getCurrentSemester = () => request.get('/teacher/semester/current')

// 学期详情
export const getSemesterDetail = (id) => request.get(`/teacher/semester/${id}`)