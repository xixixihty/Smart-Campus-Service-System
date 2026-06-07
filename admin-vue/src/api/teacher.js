import request from '@/utils/request'

// 教师工作台统计
export const getTeacherDashboardStats = (params) => request.get('/teacher/dashboard', { params })

// 个人课表
export const getTeacherTimetable = (params) => request.get('/teacher/timetable', { params })

// 所授课程
export const getTeacherCourses = (params) => request.get('/teacher/courses', { params })

// 所授班级
export const getTeacherClasses = (params) => request.get('/teacher/classes', { params })

// 成绩管理
export const getTeacherScoreList = (params) => request.get('/teacher/scores', { params })
export const getTeacherScoreDetail = (id) => request.get(`/teacher/score/${id}`)
export const createTeacherScore = (data) => request.post('/teacher/score', data)
export const updateTeacherScore = (data) => request.put('/teacher/score', data)
export const batchTeacherScore = (data) => request.post('/teacher/scores/batch', data)

// 请假审批
export const getTeacherLeaveList = (params) => request.get('/teacher/leave-requests/pending', { params })
export const getTeacherLeaveDetail = (id) => request.get(`/teacher/leave-request/${id}`)
export const approveTeacherLeave = (id, data) => request.post(`/teacher/leave-request/${id}/approve`, data)

// 调课申请
export const getTeacherRescheduleList = (params) => request.get('/teacher/reschedules', { params })
export const createTeacherReschedule = (data) => request.post('/teacher/reschedule', data)
export const cancelTeacherReschedule = (id) => request.put(`/teacher/reschedule/${id}/cancel`)

// 通知公告
export const getTeacherNoticeList = (params) => request.get('/teacher/notices', { params })
export const getTeacherNoticeDetail = (id) => request.get(`/teacher/notice/${id}`)

// ========== 管理员-教师管理 ==========
export const getTeacherList = (params) => request.get('/teachers/admin', { params })
export const getTeacherDetail = (id) => request.get(`/teachers/admin/${id}`)
export const createTeacher = (data) => request.post('/teachers/admin', data)
export const updateTeacher = (data) => request.put('/teachers/admin', data)
export const deleteTeacher = (ids) => request.delete(`/teachers/admin/${ids}`)
export const resetTeacherPassword = (id) => request.put(`/teachers/admin/${id}/reset-password`)