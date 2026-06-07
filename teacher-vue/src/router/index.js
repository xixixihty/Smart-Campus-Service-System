import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '教师登录' }
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '教师工作台' }
  },
  {
    path: '/timetable',
    name: 'Timetable',
    component: () => import('@/views/teaching/Timetable.vue'),
    meta: { title: '我的课表' }
  },
  {
    path: '/my-classes',
    name: 'MyClasses',
    component: () => import('@/views/teaching/MyClasses.vue'),
    meta: { title: '我的班级' }
  },
  {
    path: '/class-students/:classId',
    name: 'ClassStudents',
    component: () => import('@/views/teaching/ClassStudents.vue'),
    meta: { title: '班级花名册' }
  },
  {
    path: '/my-courses',
    name: 'MyCourses',
    component: () => import('@/views/teaching/MyCourses.vue'),
    meta: { title: '我的课程' }
  },
  {
    path: '/course-reschedule',
    name: 'CourseReschedule',
    component: () => import('@/views/teaching/CourseReschedule.vue'),
    meta: { title: '调课管理' }
  },
  {
    path: '/leave-approval',
    name: 'LeaveApproval',
    component: () => import('@/views/teaching/LeaveApproval.vue'),
    meta: { title: '请假审批' }
  },
  {
    path: '/score-entry',
    name: 'ScoreEntry',
    component: () => import('@/views/teaching/ScoreEntry.vue'),
    meta: { title: '成绩录入' }
  },
  {
    path: '/notice',
    name: 'Notice',
    component: () => import('@/views/campus/Notice.vue'),
    meta: { title: '通知中心' }
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
  },
  {
    path: '/ai-assistant',
    name: 'AiAssistant',
    component: () => import('@/views/ai/AiChat.vue'),
    meta: { title: 'AI 教学助手' }
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
  document.title = to.meta.title ? `${to.meta.title} - 智慧校园教师端` : '智慧校园教师端'
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
    next('/login')
    return
  }

  next()
})

export default router