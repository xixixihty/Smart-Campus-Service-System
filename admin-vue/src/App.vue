<template>
  <router-view v-if="isLoginPage" />
  <div class="common-layout" v-else>
    <el-container>
      <el-header>
        <div class="header-left">
          <img src="/logo.svg" class="header-logo" alt="Logo" />
          <span class="header-title">{{ headerTitle }}</span>
        </div>
        <div class="header-right">
          <el-popover
            placement="bottom"
            :width="320"
            trigger="click"
            :visible="notificationPopoverVisible"
          >
            <template #reference>
              <el-badge :value="noticeCount" :hidden="noticeCount === 0" class="notice-badge">
                <el-icon :size="22" style="cursor: pointer" @click="toggleNotificationPopover"><Bell /></el-icon>
              </el-badge>
            </template>
            <div class="notification-popover">
              <div class="notif-header">
                <span style="font-weight: 600; font-size: 14px;">通知消息</span>
                <el-button text size="small" @click="markAllRead">全部已读</el-button>
              </div>
              <div v-if="recentNotifications.length === 0" class="notif-empty">暂无新通知</div>
              <div v-else class="notif-list">
                <div v-for="(n, i) in recentNotifications" :key="i" class="notif-item">
                  <div class="notif-title">{{ n.title || getNotifTypeLabel(n.type) }}</div>
                  <div class="notif-time">{{ formatTime(n.createTime) }}</div>
                </div>
              </div>
              <div class="notif-footer" @click="goToNotificationPage">查看全部通知 →</div>
            </div>
          </el-popover>
          <el-button :icon="isDark ? Sunny : Moon" circle size="small" class="theme-toggle" @click="toggleTheme" />
          <el-dropdown @command="handleUserCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              <span>{{ username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container class="main-container">
        <el-aside width="220px">
          <el-menu
            :key="'menu-' + userType"
            :default-active="activeMenu"
            router
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <!-- ========== 管理员菜单 ========== -->
            <template v-if="userType === 'admin'">
              <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><span>工作台</span></el-menu-item>
              <el-menu-item index="/college"><el-icon><School /></el-icon><span>学院管理</span></el-menu-item>
              <el-menu-item index="/major"><el-icon><Collection /></el-icon><span>专业管理</span></el-menu-item>
              <el-menu-item index="/class"><el-icon><Grid /></el-icon><span>班级管理</span></el-menu-item>
              <el-menu-item index="/classroom"><el-icon><HomeFilled /></el-icon><span>教室管理</span></el-menu-item>
              <el-menu-item index="/teacher"><el-icon><Avatar /></el-icon><span>教师管理</span></el-menu-item>
              <el-menu-item index="/student"><el-icon><User /></el-icon><span>学生管理</span></el-menu-item>
              <el-menu-item index="/semester"><el-icon><Calendar /></el-icon><span>学期管理</span></el-menu-item>
              <el-menu-item index="/course"><el-icon><Notebook /></el-icon><span>课程管理</span></el-menu-item>
              <el-menu-item index="/course-schedule"><el-icon><Timer /></el-icon><span>排课管理</span></el-menu-item>
              <el-menu-item index="/course-selection-period"><el-icon><Clock /></el-icon><span>选课时段</span></el-menu-item>
              <el-menu-item index="/score-entry"><el-icon><Tickets /></el-icon><span>成绩管理</span></el-menu-item>
              <el-menu-item index="/makeup-exam"><el-icon><EditPen /></el-icon><span>补考管理</span></el-menu-item>
              <el-menu-item index="/book-category"><el-icon><FolderOpened /></el-icon><span>图书分类</span></el-menu-item>
              <el-menu-item index="/book"><el-icon><CollectionTag /></el-icon><span>图书管理</span></el-menu-item>
              <el-menu-item index="/borrow-record"><el-icon><Document /></el-icon><span>借阅记录</span></el-menu-item>
              <el-menu-item index="/seat"><el-icon><Place /></el-icon><span>座位管理</span></el-menu-item>
              <el-menu-item index="/notice"><el-icon><Bell /></el-icon><span>通知管理</span></el-menu-item>
              <el-menu-item index="/leave-approval"><el-icon><Checked /></el-icon><span>请假审批</span></el-menu-item>
              <el-menu-item index="/reschedule-approval"><el-icon><RefreshRight /></el-icon><span>调课审批</span></el-menu-item>
              <el-menu-item index="/ai-assistant"><el-icon><Cpu /></el-icon><span>AI 智慧助手</span></el-menu-item>
            </template>

            <!-- ========== 教师菜单 ========== -->
            <template v-else-if="userType === 'teacher'">
              <el-menu-item index="/teacher/dashboard"><el-icon><Odometer /></el-icon><span>工作台</span></el-menu-item>
              <el-menu-item index="/teacher/timetable"><el-icon><Timer /></el-icon><span>我的课表</span></el-menu-item>
              <el-menu-item index="/teacher/score"><el-icon><Tickets /></el-icon><span>成绩录入</span></el-menu-item>
              <el-menu-item index="/teacher/leave-approval"><el-icon><Checked /></el-icon><span>请假审批</span></el-menu-item>
              <el-menu-item index="/teacher/reschedule"><el-icon><Refresh /></el-icon><span>调课申请</span></el-menu-item>
              <el-menu-item index="/teacher/notices"><el-icon><Bell /></el-icon><span>通知公告</span></el-menu-item>
              <el-menu-item index="/teacher/ai-assistant"><el-icon><Cpu /></el-icon><span>AI 智慧助手</span></el-menu-item>
            </template>

            <!-- ========== 学生菜单 ========== -->
            <template v-else-if="userType === 'student'">
              <el-menu-item index="/student/dashboard"><el-icon><Odometer /></el-icon><span>工作台</span></el-menu-item>
              <el-menu-item index="/student/timetable"><el-icon><Timer /></el-icon><span>我的课表</span></el-menu-item>
              <el-menu-item index="/student/courses"><el-icon><Notebook /></el-icon><span>选课中心</span></el-menu-item>
              <el-menu-item index="/student/scores"><el-icon><Tickets /></el-icon><span>成绩查询</span></el-menu-item>
              <el-menu-item index="/student/leave"><el-icon><EditPen /></el-icon><span>请假申请</span></el-menu-item>
              <el-menu-item index="/student/books"><el-icon><Reading /></el-icon><span>图书借阅</span></el-menu-item>
              <el-menu-item index="/student/seats"><el-icon><Place /></el-icon><span>座位预约</span></el-menu-item>
              <el-menu-item index="/student/notices"><el-icon><Bell /></el-icon><span>通知公告</span></el-menu-item>
              <el-menu-item index="/ai-assistant"><el-icon><Cpu /></el-icon><span>AI 智慧助手</span></el-menu-item>
            </template>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { validateToken } from '@/api/auth'
import { getUnreadNotificationCount, markAllNotificationsAsRead } from '@/api/notification'
import { useTheme } from '@/composables/useTheme'
import { useWebSocket } from '@/composables/useWebSocket'
import { Sunny, Moon } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const { isDark, toggleTheme } = useTheme()
const { connect, subscribe, disconnect } = useWebSocket()

const userType = ref(localStorage.getItem('userType') || 'admin')
const username = ref(localStorage.getItem('username') || '管理员')
const noticeCount = ref(0)
const notificationPopoverVisible = ref(false)
const recentNotifications = ref([])

const headerTitle = computed(() => {
  const titles = { admin: '智慧校园服务系统-管理端', teacher: '智慧校园服务系统-教师端', student: '智慧校园服务系统-学生端' }
  return titles[userType.value] || '智慧校园服务系统'
})

const activeMenu = computed(() => route.path)
const isLoginPage = computed(() => route.path === '/login')

watch(() => route.path, () => {
  userType.value = localStorage.getItem('userType') || 'admin'
  username.value = localStorage.getItem('username') || '管理员'
})

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (token && token !== 'undefined' && token !== 'null') {
    try {
      await validateToken()
    } catch {
      localStorage.clear()
      username.value = '管理员'
      if (route.path !== '/login') {
        router.replace('/login')
      }
    }
  }
  fetchUnreadCount()
  initWebSocket()
})

onUnmounted(() => {
  disconnect()
})

function getNotifTypeLabel(type) {
  const map = {
    'LEAVE_APPLY': '新请假申请',
    'LEAVE_APPROVED': '请假已批准',
    'LEAVE_REJECTED': '请假已拒绝',
    'NEED_RESCHEDULE': '需重新调课',
    'COURSE_RESCHEDULED': '课程已调换',
    'NOTIFICATION': '系统通知',
    'BOOK_OVERDUE': '图书逾期提醒',
    'SEAT_REMINDER': '座位预约提醒'
  }
  return map[type] || '系统通知'
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return (d.getMonth() + 1) + '月' + d.getDate() + '日'
}

const playNotificationSound = () => {
  try {
    const audioCtx = new (window.AudioContext || window.webkitAudioContext)()
    const oscillator = audioCtx.createOscillator()
    const gainNode = audioCtx.createGain()
    oscillator.connect(gainNode)
    gainNode.connect(audioCtx.destination)
    oscillator.type = 'sine'
    gainNode.gain.setValueAtTime(0.3, audioCtx.currentTime)
    gainNode.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.3)
    oscillator.frequency.setValueAtTime(880, audioCtx.currentTime)
    oscillator.frequency.setValueAtTime(1100, audioCtx.currentTime + 0.1)
    oscillator.start(audioCtx.currentTime)
    oscillator.stop(audioCtx.currentTime + 0.3)
  } catch {}
}

const handleNewNotification = (payload) => {
  noticeCount.value++
  // 只缓存最近的 20 条
  if (payload) {
    recentNotifications.value.unshift(payload)
    if (recentNotifications.value.length > 20) {
      recentNotifications.value.pop()
    }
  }
  playNotificationSound()
}

const initWebSocket = async () => {
  try {
    await connect()

    // 订阅多个通知主题
    const topics = [
      '/queue/leave/apply',
      '/queue/leave/approved',
      '/queue/leave/rejected',
      '/queue/leave/reschedule',
      '/queue/course/rescheduled',
      '/queue/notification'
    ]
    topics.forEach(topic => {
      subscribe(topic, handleNewNotification)
    })
  } catch (e) {
    console.warn('WebSocket连接失败:', e.message)
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadNotificationCount()
    noticeCount.value = res.data ?? 0
  } catch {
    noticeCount.value = 0
  }
}

const toggleNotificationPopover = () => {
  notificationPopoverVisible.value = !notificationPopoverVisible.value
}

const markAllRead = async () => {
  try {
    await markAllNotificationsAsRead()
    noticeCount.value = 0
    recentNotifications.value = []
    notificationPopoverVisible.value = false
  } catch {}
}

const goToNotificationPage = () => {
  notificationPopoverVisible.value = false
  const redirectMap = { admin: '/leave-approval', teacher: '/teacher/leave-approval', student: '/student/notices' }
  router.push(redirectMap[userType.value] || '/leave-approval')
}

const handleBellClick = () => {
  // 旧兼容
  toggleNotificationPopover()
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    localStorage.clear()
    username.value = '管理员'
    router.replace('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'password') {
    router.push('/password')
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
}

.common-layout {
  height: 100%;
}

.common-layout > .el-container {
  height: 100%;
}

.el-header {
  background: linear-gradient(135deg, #1a3a5c 0%, #2d6aa0 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
}

.header-logo {
  height: 32px;
  width: auto;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
  color: #fff;
}

.header-right .el-badge {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.el-aside {
  background-color: #304156;
  overflow-y: auto;
  overflow-x: hidden;
  height: 100%;
}

.el-aside .el-menu {
  border-right: none;
}

/* 核心修复：内层容器锁定高度，不让溢出传播到页面层级 */
.main-container {
  overflow: hidden;
  flex: 1;
  min-height: 0;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-x: hidden;
  overflow-y: auto;
  min-width: 0;
}

.theme-toggle {
  margin-left: 6px;
  background: rgba(255,255,255,0.12) !important;
  border: none !important;
  color: #fff !important;
}

.theme-toggle:hover {
  background: rgba(255,255,255,0.25) !important;
}

html.dark .theme-toggle {
  background: rgba(255,255,255,0.06) !important;
}

html.dark .theme-toggle:hover {
  background: rgba(255,255,255,0.15) !important;
}

.notice-badge {
  margin-right: 12px;
}
.notice-badge :deep(.el-badge__content) {
  top: 6px;
  right: 4px;
}
.notification-popover {
  font-size: 13px;
}
.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.notif-empty {
  padding: 24px 0;
  text-align: center;
  color: #c0c4cc;
}
.notif-list {
  max-height: 280px;
  overflow-y: auto;
}
.notif-item {
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}
.notif-item:last-child {
  border-bottom: none;
}
.notif-title {
  font-size: 13px;
  color: #303133;
}
.notif-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 2px;
}
.notif-footer {
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
  text-align: center;
  color: #409EFF;
  cursor: pointer;
  font-size: 13px;
}
.notif-footer:hover {
  background: #f5f7fa;
}
</style>
