<template>
  <router-view v-if="isLoginPage" />
  <div class="common-layout" v-else>
    <el-container>
      <el-header>
        <div class="header-left">
          <img src="/logo.svg" class="header-logo" alt="Logo" />
          <span class="header-title">智慧校园服务系统-管理端</span>
        </div>
        <div class="header-right">
          <el-badge :value="noticeCount" :hidden="noticeCount === 0">
            <el-icon :size="22" style="cursor: pointer" @click="handleBellClick"><Bell /></el-icon>
          </el-badge>
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
      <el-container>
        <el-aside width="220px">
          <el-menu
            :default-active="activeMenu"
            :default-openeds="defaultOpeneds"
            router
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/dashboard">
              <el-icon><Odometer /></el-icon>
              <span>工作台</span>
            </el-menu-item>

            <el-sub-menu index="org">
              <template #title>
                <el-icon><OfficeBuilding /></el-icon>
                <span>组织架构</span>
              </template>
              <el-menu-item index="/college">
                <el-icon><School /></el-icon>
                <span>学院管理</span>
              </el-menu-item>
              <el-menu-item index="/major">
                <el-icon><Collection /></el-icon>
                <span>专业管理</span>
              </el-menu-item>
              <el-menu-item index="/class">
                <el-icon><Grid /></el-icon>
                <span>班级管理</span>
              </el-menu-item>
              <el-menu-item index="/classroom">
                <el-icon><HomeFilled /></el-icon>
                <span>教室管理</span>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="people">
              <template #title>
                <el-icon><UserFilled /></el-icon>
                <span>人员管理</span>
              </template>
              <el-menu-item index="/teacher">
                <el-icon><Avatar /></el-icon>
                <span>教师管理</span>
              </el-menu-item>
              <el-menu-item index="/student">
                <el-icon><User /></el-icon>
                <span>学生管理</span>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="teaching">
              <template #title>
                <el-icon><Reading /></el-icon>
                <span>教学管理</span>
              </template>
              <el-menu-item index="/semester">
                <el-icon><Calendar /></el-icon>
                <span>学期管理</span>
              </el-menu-item>
              <el-menu-item index="/course">
                <el-icon><Notebook /></el-icon>
                <span>课程管理</span>
              </el-menu-item>
              <el-menu-item index="/course-schedule">
                <el-icon><Timer /></el-icon>
                <span>排课管理</span>
              </el-menu-item>
              <el-menu-item index="/course-selection-period">
                <el-icon><Clock /></el-icon>
                <span>选课时段</span>
              </el-menu-item>
              <el-menu-item index="/score-entry">
                <el-icon><Tickets /></el-icon>
                <span>成绩管理</span>
              </el-menu-item>
              <el-menu-item index="/makeup-exam">
                <el-icon><EditPen /></el-icon>
                <span>补考管理</span>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="library">
              <template #title>
                <el-icon><Reading /></el-icon>
                <span>图书管理</span>
              </template>
              <el-menu-item index="/book-category">
                <el-icon><FolderOpened /></el-icon>
                <span>图书分类</span>
              </el-menu-item>
              <el-menu-item index="/book">
                <el-icon><CollectionTag /></el-icon>
                <span>图书管理</span>
              </el-menu-item>
              <el-menu-item index="/borrow-record">
                <el-icon><Document /></el-icon>
                <span>借阅记录</span>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="facility">
              <template #title>
                <el-icon><Monitor /></el-icon>
                <span>设施管理</span>
              </template>
              <el-menu-item index="/seat">
                <el-icon><Place /></el-icon>
                <span>座位管理</span>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="campus">
              <template #title>
                <el-icon><Management /></el-icon>
                <span>校园管理</span>
              </template>
              <el-menu-item index="/notice">
                <el-icon><Bell /></el-icon>
                <span>通知管理</span>
              </el-menu-item>
              <el-menu-item index="/leave-approval">
                <el-icon><Checked /></el-icon>
                <span>请假审批</span>
              </el-menu-item>
            </el-sub-menu>

            <el-menu-item index="/ai-assistant">
              <el-icon><Cpu /></el-icon>
              <span>AI 智慧助手</span>
            </el-menu-item>
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

const username = ref(localStorage.getItem('username') || '管理员')
const noticeCount = ref(0)

const activeMenu = computed(() => route.path)
const isLoginPage = computed(() => route.path === '/login')
const defaultOpeneds = ref([])

watch(() => route.path, () => {
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

const initWebSocket = async () => {
  try {
    await connect()

    subscribe('/queue/leave/apply', (payload) => {
      noticeCount.value++
      playNotificationSound()
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

const handleBellClick = async () => {
  noticeCount.value = 0
  try {
    await markAllNotificationsAsRead()
  } catch {}
  router.push('/leave-approval')
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
}

.el-aside .el-menu {
  border-right: none;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  min-height: 0;
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
</style>
