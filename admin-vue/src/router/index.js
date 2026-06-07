import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    redirect: '/login'
  },

  // ==================== 管理员路由 ====================
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '工作台', roles: ['admin'] }
  },
  {
    path: '/college',
    name: 'College',
    component: () => import('@/views/org/College.vue'),
    meta: { title: '学院管理', roles: ['admin'] }
  },
  {
    path: '/major',
    name: 'Major',
    component: () => import('@/views/org/Major.vue'),
    meta: { title: '专业管理', roles: ['admin'] }
  },
  {
    path: '/class',
    name: 'Class',
    component: () => import('@/views/org/Class.vue'),
    meta: { title: '班级管理', roles: ['admin'] }
  },
  {
    path: '/classroom',
    name: 'Classroom',
    component: () => import('@/views/org/Classroom.vue'),
    meta: { title: '教室管理', roles: ['admin'] }
  },
  {
    path: '/teacher',
    name: 'Teacher',
    component: () => import('@/views/people/Teacher.vue'),
    meta: { title: '教师管理', roles: ['admin'] }
  },
  {
    path: '/student',
    name: 'Student',
    component: () => import('@/views/people/Student.vue'),
    meta: { title: '学生管理', roles: ['admin'] }
  },
  {
    path: '/semester',
    name: 'Semester',
    component: () => import('@/views/teaching/Semester.vue'),
    meta: { title: '学期管理', roles: ['admin'] }
  },
  {
    path: '/course',
    name: 'Course',
    component: () => import('@/views/teaching/Course.vue'),
    meta: { title: '课程管理', roles: ['admin'] }
  },
  {
    path: '/course-schedule',
    name: 'CourseSchedule',
    component: () => import('@/views/teaching/CourseSchedule.vue'),
    meta: { title: '排课管理', roles: ['admin'] }
  },
  {
    path: '/course-selection-period',
    name: 'CourseSelectionPeriod',
    component: () => import('@/views/teaching/CourseSelectionPeriod.vue'),
    meta: { title: '选课时段', roles: ['admin'] }
  },
  {
    path: '/score-entry',
    name: 'ScoreEntry',
    component: () => import('@/views/teaching/ScoreEntry.vue'),
    meta: { title: '成绩管理', roles: ['admin'] }
  },
  {
    path: '/makeup-exam',
    name: 'MakeupExam',
    component: () => import('@/views/teaching/MakeupExam.vue'),
    meta: { title: '补考管理', roles: ['admin'] }
  },
  {
    path: '/book-category',
    name: 'BookCategory',
    component: () => import('@/views/library/BookCategory.vue'),
    meta: { title: '图书分类', roles: ['admin'] }
  },
  {
    path: '/book',
    name: 'Book',
    component: () => import('@/views/library/Book.vue'),
    meta: { title: '图书管理', roles: ['admin'] }
  },
  {
    path: '/borrow-record',
    name: 'BorrowRecord',
    component: () => import('@/views/library/BorrowRecord.vue'),
    meta: { title: '借阅记录', roles: ['admin'] }
  },
  {
    path: '/seat',
    name: 'Seat',
    component: () => import('@/views/facility/Seat.vue'),
    meta: { title: '座位管理', roles: ['admin'] }
  },
  {
    path: '/notice',
    name: 'Notice',
    component: () => import('@/views/campus/Notice.vue'),
    meta: { title: '通知管理', roles: ['admin'] }
  },
  {
    path: '/leave-approval',
    name: 'LeaveApproval',
    component: () => import('@/views/campus/LeaveApproval.vue'),
    meta: { title: '请假审批', roles: ['admin'] }
  },
  {
    path: '/reschedule-approval',
    name: 'RescheduleApproval',
    component: () => import('@/views/campus/RescheduleApproval.vue'),
    meta: { title: '调课审批', roles: ['admin'] }
  },

  // ==================== 教师路由 ====================
  {
    path: '/teacher/dashboard',
    name: 'TeacherDashboard',
    component: () => import('@/views/teacher/TeacherDashboard.vue'),
    meta: { title: '教师工作台', roles: ['teacher'] }
  },
  {
    path: '/teacher/timetable',
    name: 'TeacherTimetable',
    component: () => import('@/views/teacher/TeacherTimetable.vue'),
    meta: { title: '我的课表', roles: ['teacher'] }
  },
  {
    path: '/teacher/score',
    name: 'TeacherScore',
    component: () => import('@/views/teacher/TeacherScore.vue'),
    meta: { title: '成绩录入', roles: ['teacher'] }
  },
  {
    path: '/teacher/leave-approval',
    name: 'TeacherLeaveApproval',
    component: () => import('@/views/teacher/TeacherLeaveApproval.vue'),
    meta: { title: '请假审批', roles: ['teacher'] }
  },
  {
    path: '/teacher/reschedule',
    name: 'TeacherReschedule',
    component: () => import('@/views/teacher/TeacherReschedule.vue'),
    meta: { title: '调课申请', roles: ['teacher'] }
  },
  {
    path: '/teacher/notices',
    name: 'TeacherNotices',
    component: () => import('@/views/teacher/TeacherNotices.vue'),
    meta: { title: '通知公告', roles: ['teacher'] }
  },

  // ==================== 学生路由 ====================
  {
    path: '/student/dashboard',
    name: 'StudentDashboard',
    component: () => import('@/views/student/StudentDashboard.vue'),
    meta: { title: '学生工作台', roles: ['student'] }
  },
  {
    path: '/student/timetable',
    name: 'StudentTimetable',
    component: () => import('@/views/student/StudentTimetable.vue'),
    meta: { title: '我的课表', roles: ['student'] }
  },
  {
    path: '/student/courses',
    name: 'StudentCourses',
    component: () => import('@/views/student/StudentCourses.vue'),
    meta: { title: '选课中心', roles: ['student'] }
  },
  {
    path: '/student/scores',
    name: 'StudentScores',
    component: () => import('@/views/student/StudentScores.vue'),
    meta: { title: '成绩查询', roles: ['student'] }
  },
  {
    path: '/student/leave',
    name: 'StudentLeave',
    component: () => import('@/views/student/StudentLeave.vue'),
    meta: { title: '请假申请', roles: ['student'] }
  },
  {
    path: '/student/notices',
    name: 'StudentNotices',
    component: () => import('@/views/student/StudentNotices.vue'),
    meta: { title: '通知公告', roles: ['student'] }
  },
  {
    path: '/student/books',
    name: 'StudentBooks',
    component: () => import('@/views/student/StudentBooks.vue'),
    meta: { title: '图书借阅', roles: ['student'] }
  },
  {
    path: '/student/seats',
    name: 'StudentSeats',
    component: () => import('@/views/student/StudentSeats.vue'),
    meta: { title: '座位预约', roles: ['student'] }
  },

  // ==================== 公共路由（所有角色可访问） ====================
  {
    path: '/ai-assistant',
    name: 'AiAssistant',
    component: () => import('@/views/ai/AiAssistant.vue'),
    meta: { title: 'AI 智慧助手' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/MyInfo.vue'),
    meta: { title: '个人信息' }
  },
  {
    path: '/password',
    name: 'ChangePassword',
    component: () => import('@/views/profile/ChangePassword.vue'),
    meta: { title: '修改密码' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function isTokenExpired(token) {
  if (!token || token === 'undefined' || token === 'null') {
    return true
  }
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return true
    const payload = JSON.parse(atob(parts[1]))
    const now = Math.floor(Date.now() / 1000)
    return payload.exp && payload.exp < now
  } catch {
    return true
  }
}

function getDefaultRoute(userType) {
  const routes = { admin: '/dashboard', teacher: '/teacher/dashboard', student: '/student/dashboard' }
  return routes[userType] || '/dashboard'
}

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 智慧校园` : '智慧校园管理系统'
  const token = localStorage.getItem('token')
  const userType = localStorage.getItem('userType') || 'admin'

  if (to.path === '/login') {
    if (token && !isTokenExpired(token)) {
      next(getDefaultRoute(userType))
    } else {
      if (token) localStorage.removeItem('token')
      next()
    }
    return
  }

  if (!token || isTokenExpired(token)) {
    if (token) localStorage.removeItem('token')
    next('/login')
    return
  }

  // 角色权限校验
  const routeRoles = to.meta.roles
  if (routeRoles && !routeRoles.includes(userType)) {
    next(getDefaultRoute(userType))
    return
  }

  next()
})

export default router
