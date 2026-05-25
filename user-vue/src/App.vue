<template>
  <router-view v-if="isLoginPage" />
  <div class="common-layout" v-else>
    <el-container>
      <el-header>
        <div class="header-left">
          <el-icon :size="28"><School /></el-icon>
          <span class="header-title">智慧校园服务系统-用户端</span>
        </div>
        <div class="header-right">
          <el-badge :value="unreadNoticeCount" :hidden="unreadNoticeCount === 0">
            <el-icon :size="22" style="cursor: pointer" @click="$router.push('/notice')"><Bell /></el-icon>
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
        <el-aside width="200px">
          <el-menu
            :default-active="activeMenu"
            router
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/dashboard">
              <el-icon><Odometer /></el-icon>
              <span>首页</span>
            </el-menu-item>

            <el-sub-menu index="study">
              <template #title>
                <el-icon><Reading /></el-icon>
                <span>学习中心</span>
              </template>
              <el-menu-item index="/timetable">
                <el-icon><Calendar /></el-icon>
                <span>我的课表</span>
              </el-menu-item>
              <el-menu-item index="/course-selection">
                <el-icon><Plus /></el-icon>
                <span>选课中心</span>
              </el-menu-item>
              <el-menu-item index="/my-courses">
                <el-icon><Notebook /></el-icon>
                <span>我的课程</span>
              </el-menu-item>
              <el-menu-item index="/my-scores">
                <el-icon><Tickets /></el-icon>
                <span>我的成绩</span>
              </el-menu-item>
              <el-menu-item index="/my-makeup-exams">
                <el-icon><EditPen /></el-icon>
                <span>补考信息</span>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="library">
              <template #title>
                <el-icon><CollectionTag /></el-icon>
                <span>图书借阅</span>
              </template>
              <el-menu-item index="/book-borrow">
                <el-icon><Search /></el-icon>
                <span>图书检索</span>
              </el-menu-item>
              <el-menu-item index="/my-borrows">
                <el-icon><Document /></el-icon>
                <span>我的借阅</span>
              </el-menu-item>
              <el-menu-item index="/reading-report">
                <el-icon><Edit /></el-icon>
                <span>阅读报告</span>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="seat">
              <template #title>
                <el-icon><Position /></el-icon>
                <span>座位预约</span>
              </template>
              <el-menu-item index="/seat-reserve">
                <el-icon><Plus /></el-icon>
                <span>预约座位</span>
              </el-menu-item>
              <el-menu-item index="/my-reservations">
                <el-icon><List /></el-icon>
                <span>我的预约</span>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="campus">
              <template #title>
                <el-icon><Management /></el-icon>
                <span>校园服务</span>
              </template>
              <el-menu-item index="/notice">
                <el-icon><Bell /></el-icon>
                <span>通知公告</span>
              </el-menu-item>
              <el-menu-item index="/leave-request">
                <el-icon><EditPen /></el-icon>
                <span>请假申请</span>
              </el-menu-item>
            </el-sub-menu>

            <el-menu-item index="/ai-assistant">
              <el-icon><Cpu /></el-icon>
              <span>AI 学习助手</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'
import { validateToken } from '@/api/auth'
import { getUnreadCount } from '@/api/notice'
import { useTheme } from '@/composables/useTheme'
import { Sunny, Moon } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const { isDark, toggleTheme } = useTheme()

const username = ref(localStorage.getItem('username') || '同学')
const unreadNoticeCount = ref(0)

const activeMenu = computed(() => route.path)
const isLoginPage = computed(() => route.path === '/login')

watch(() => route.path, () => {
  username.value = localStorage.getItem('username') || '同学'
})

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (token && token !== 'undefined' && token !== 'null') {
    try {
      await validateToken()
    } catch {
      localStorage.clear()
      username.value = '同学'
      if (route.path !== '/login') {
        router.replace('/login')
      }
    }
  }
  fetchUnreadCount()
})

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadNoticeCount.value = res.data || res || 0
  } catch {
    unreadNoticeCount.value = 0
  }
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    localStorage.clear()
    username.value = '同学'
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

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
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
