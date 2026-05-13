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
    meta: { title: '首页' }
  },
  {
    path: '/timetable',
    name: 'Timetable',
    component: () => import('@/views/study/Timetable.vue'),
    meta: { title: '我的课表' }
  },
  {
    path: '/course-selection',
    name: 'CourseSelection',
    component: () => import('@/views/study/CourseSelection.vue'),
    meta: { title: '选课中心' }
  },
  {
    path: '/my-courses',
    name: 'MyCourses',
    component: () => import('@/views/study/MyCourses.vue'),
    meta: { title: '我的课程' }
  },
  {
    path: '/my-scores',
    name: 'MyScores',
    component: () => import('@/views/study/MyScores.vue'),
    meta: { title: '我的成绩' }
  },
  {
    path: '/my-makeup-exams',
    name: 'MyMakeupExams',
    component: () => import('@/views/study/MyMakeupExams.vue'),
    meta: { title: '补考信息' }
  },
  {
    path: '/book-borrow',
    name: 'BookBorrow',
    component: () => import('@/views/library/BookBorrow.vue'),
    meta: { title: '图书检索' }
  },
  {
    path: '/my-borrows',
    name: 'MyBorrows',
    component: () => import('@/views/library/MyBorrows.vue'),
    meta: { title: '我的借阅' }
  },
  {
    path: '/reading-report',
    name: 'ReadingReport',
    component: () => import('@/views/library/ReadingReport.vue'),
    meta: { title: '阅读报告' }
  },
  {
    path: '/seat-reserve',
    name: 'SeatReserve',
    component: () => import('@/views/seat/SeatReserve.vue'),
    meta: { title: '预约座位' }
  },
  {
    path: '/my-reservations',
    name: 'MyReservations',
    component: () => import('@/views/seat/MyReservations.vue'),
    meta: { title: '我的预约' }
  },
  {
    path: '/notice',
    name: 'Notice',
    component: () => import('@/views/campus/Notice.vue'),
    meta: { title: '通知公告' }
  },
  {
    path: '/leave-request',
    name: 'LeaveRequest',
    component: () => import('@/views/campus/LeaveRequest.vue'),
    meta: { title: '请假管理' }
  },
  {
    path: '/ai-book-recommend',
    name: 'AiBookRecommend',
    component: () => import('@/views/ai/AiBookRecommend.vue'),
    meta: { title: '图书推荐' }
  },
  {
    path: '/ai-hot-books',
    name: 'AiHotBooks',
    component: () => import('@/views/ai/AiHotBooks.vue'),
    meta: { title: '热门图书' }
  },
  {
    path: '/ai-reading-analysis',
    name: 'AiReadingAnalysis',
    component: () => import('@/views/ai/AiReadingAnalysis.vue'),
    meta: { title: '阅读分析' }
  },
  {
    path: '/ai-score-analysis',
    name: 'AiScoreAnalysis',
    component: () => import('@/views/ai/AiScoreAnalysis.vue'),
    meta: { title: '成绩分析' }
  },
  {
    path: '/ai-course-recommend',
    name: 'AiCourseRecommend',
    component: () => import('@/views/ai/AiCourseRecommend.vue'),
    meta: { title: '课程推荐' }
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

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 智慧校园` : '智慧校园'
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
