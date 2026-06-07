import request from '@/utils/request'

// 学生工作台统计
export const getStudentDashboardStats = () => request.get('/course-schedules/user/dashboard')

// 我的课表
export const getStudentTimetable = (params) => request.get('/course-schedules/user/timetable', { params })

// 选课中心
export const getStudentCourseList = (params) => request.get('/course-selections/user/available', { params })
export const selectCourse = (data) => request.post('/course-selections/user', data)
export const dropCourse = (courseId) => request.delete(`/course-selections/user/${courseId}`)
// 我的选课
export const getMyCourseSelection = (params) => request.get('/course-selections/user/my', { params })

// 成绩查询
export const getStudentScoreList = (params) => request.get('/score-entries/user/my', { params })

// 请假申请
export const getStudentLeaveList = (params) => request.get('/leave-requests/user/my', { params })
export const getStudentLeaveDetail = (id) => request.get(`/leave-requests/user/${id}`)
export const createStudentLeave = (data) => request.post('/leave-requests/user', data)

// 通知公告
export const getStudentNoticeList = (params) => request.get('/notices/user/my', { params })
export const getStudentNoticeDetail = (id) => request.get(`/notices/user/${id}`)

// 座位预约
export const getStudentSeatList = (params) => request.get('/seat-reservations/user/available', { params })
export const getStudentSeatSchedule = (seatId, date) => request.get(`/seat-reservations/user/${seatId}/schedule/${date}`)
export const getMySeatReservations = (params) => request.get('/seat-reservations/user/my', { params })
export const reserveSeat = (data) => request.post('/seat-reservations/user', data)
export const cancelSeatReservation = (id) => request.delete(`/seat-reservations/user/${id}`)
export const checkInSeat = (id) => request.post(`/seat-reservations/user/${id}/check-in`)
export const checkOutSeat = (id) => request.post(`/seat-reservations/user/${id}/check-out`)

// 图书借阅
export const getStudentBookList = (params) => request.get('/books/user', { params })
export const getStudentBookDetail = (id) => request.get(`/books/user/${id}`)
export const borrowBook = (data) => request.post('/borrow-records/user', data)
export const returnBook = (id) => request.post(`/borrow-records/user/${id}/return`)
export const getMyBorrowRecords = (params) => request.get('/borrow-records/user/my', { params })

// ========== 管理员-学生管理 ==========
export const getStudentList = (params) => request.get('/students/admin', { params })
export const getStudentDetail = (id) => request.get(`/students/admin/${id}`)
export const createStudent = (data) => request.post('/students/admin', data)
export const updateStudent = (data) => request.put('/students/admin', data)
export const deleteStudent = (ids) => request.delete(`/students/admin/${ids}`)
export const resetStudentPassword = (id) => request.put(`/students/admin/${id}/reset-password`)