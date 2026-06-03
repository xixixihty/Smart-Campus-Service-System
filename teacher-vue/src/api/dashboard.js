import request from '@/utils/request'

// 教师工作台统计
export const getDashboardStats = (semesterId) => request.get('/teacher/dashboard', { params: { semesterId } })