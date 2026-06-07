import request from '@/utils/request'

// 创建调课申请
export const createReschedule = (data) => request.post('/teacher/reschedule', data)

// 我的调课记录列表
export const getMyReschedules = () => request.get('/teacher/reschedules')

// 调课详情
export const getRescheduleDetail = (id) => request.get(`/teacher/reschedule/${id}`)

// 授课排课记录（用于调课参考）
export const getTeachingCourseSchedules = () => request.get('/teacher/reschedules/teaching')

// 取消调课申请
export const cancelReschedule = (id) => request.put(`/teacher/reschedule/${id}/cancel`)

// 获取教室列表（用于调课选择）
export const getClassroomList = () => request.get('/teacher/classrooms', { params: { pageSize: 200 } })