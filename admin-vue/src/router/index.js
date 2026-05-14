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
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '工作台' }
  },
  {
    path: '/college',
    name: 'College',
    component: () => import('@/views/org/College.vue'),
    meta: { title: '学院管理' }
  },
  {
    path: '/major',
    name: 'Major',
    component: () => import('@/views/org/Major.vue'),
    meta: { title: '专业管理' }
  },
  {
    path: '/class',
    name: 'Class',
    component: () => import('@/views/org/Class.vue'),
    meta: { title: '班级管理' }
  },
  {
    path: '/classroom',
    name: 'Classroom',
    component: () => import('@/views/org/Classroom.vue'),
    meta: { title: '教室管理' }
  },
  {
    path: '/teacher',
    name: 'Teacher',
    component: () => import('@/views/people/Teacher.vue'),
    meta: { title: '教师管理' }
  },
  {
    path: '/student',
    name: 'Student',
    component: () => import('@/views/people/Student.vue'),
    meta: { title: '学生管理' }
  },
  {
    path: '/semester',
    name: 'Semester',
    component: () => import('@/views/teaching/Semester.vue'),
    meta: { title: '学期管理' }
  },
  {
    path: '/course',
    name: 'Course',
    component: () => import('@/views/teaching/Course.vue'),
    meta: { title: '课程管理' }
  },
  {
    path: '/course-schedule',
    name: 'CourseSchedule',
    component: () => import('@/views/teaching/CourseSchedule.vue'),
    meta: { title: '排课管理' }
  },
  {
    path: '/course-selection-period',
    name: 'CourseSelectionPeriod',
    component: () => import('@/views/teaching/CourseSelectionPeriod.vue'),
    meta: { title: '选课时段' }
  },
  {
    path: '/score-entry',
    name: 'ScoreEntry',
    component: () => import('@/views/teaching/ScoreEntry.vue'),
    meta: { title: '成绩管理' }
  },
  {
    path: '/makeup-exam',
    name: 'MakeupExam',
    component: () => import('@/views/teaching/MakeupExam.vue'),
    meta: { title: '补考管理' }
  },
  {
    path: '/book-category',
    name: 'BookCategory',
    component: () => import('@/views/library/BookCategory.vue'),
    meta: { title: '图书分类' }
  },
  {
    path: '/book',
    name: 'Book',
    component: () => import('@/views/library/Book.vue'),
    meta: { title: '图书管理' }
  },
  {
    path: '/borrow-record',
    name: 'BorrowRecord',
    component: () => import('@/views/library/BorrowRecord.vue'),
    meta: { title: '借阅记录' }
  },
  {
    path: '/seat',
    name: 'Seat',
    component: () => import('@/views/facility/Seat.vue'),
    meta: { title: '座位管理' }
  },
  {
    path: '/notice',
    name: 'Notice',
    component: () => import('@/views/campus/Notice.vue'),
    meta: { title: '通知管理' }
  },
  {
    path: '/leave-approval',
    name: 'LeaveApproval',
    component: () => import('@/views/campus/LeaveApproval.vue'),
    meta: { title: '请假审批' }
  },
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

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 智慧校园` : '智慧校园管理系统'
  const token = localStorage.getItem('token')

  if (to.path === '/login') {
    if (token && !isTokenExpired(token)) {
      next('/dashboard')
    } else {
      if (token) localStorage.removeItem('token')
      next()
    }
    return
  }

  if (!token || isTokenExpired(token)) {
    if (token) localStorage.removeItem('token')
    if (to.path !== '/login') {
      next('/login')
    } else {
      next()
    }
    return
  }

  next()
})

export default router
