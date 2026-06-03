import request from '@/utils/request'

// 录入单个成绩
export const insertScore = (data) => request.post('/teacher/score', data)

// 批量录入成绩
export const batchInsertScore = (data) => request.post('/teacher/scores/batch', data)

// 修改成绩
export const updateScore = (data) => request.put('/teacher/score', data)

// 成绩列表
export const getScoreList = (params) => request.get('/teacher/scores', { params })

// 成绩详情
export const getScoreDetail = (id) => request.get(`/teacher/score/${id}`)

// 未录入成绩的学生
export const getUnrecordedStudents = (courseId, semesterId) =>
  request.get('/teacher/scores/unrecorded', { params: { courseId, semesterId } })

// 成绩统计
export const getScoreStats = (semesterId) => request.get('/teacher/scores/stats', { params: { semesterId } })