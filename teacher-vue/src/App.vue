<template>
  <router-view v-if="isLoginPage" />
  <div class="common-layout" v-else>
    <el-container>
      <el-header>
        <div class="header-left">
          <img src="/favicon.svg" class="header-logo" alt="Logo" />
          <span class="header-title">智慧校园 - 教师端</span>
        </div>
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0">
            <el-icon :size="22" style="cursor: pointer" @click="router.push('/notice')"><Bell /></el-icon>
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

            <el-sub-menu index="teaching">
              <template #title>
                <el-icon><Reading /></el-icon>
                <span>教学管理</span>
              </template>
              <el-menu-item index="/timetable">
                <el-icon><Calendar /></el-icon>
                <span>我的课表</span>
              </el-menu-item>
              <el-menu-item index="/my-classes">
                <el-icon><Grid /></el-icon>
                <span>我的班级</span>
              </el-menu-item>
              <el-menu-item index="/my-courses">
                <el-icon><Notebook /></el-icon>
                <span>我的课程</span>
              </el-menu-item>
              <el-menu-item index="/course-reschedule">
                <el-icon><RefreshRight /></el-icon>
                <span>调课管理</span>
              </el-menu-item>
              <el-menu-item index="/score-entry">
                <el-icon><Tickets /></el-icon>
                <span>成绩录入</span>
              </el-menu-item>
              <el-menu-item index="/leave-approval">
                <el-icon><Checked /></el-icon>
                <span>请假审批</span>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="campus">
              <template #title>
                <el-icon><Management /></el-icon>
                <span>校园服务</span>
              </template>
              <el-menu-item index="/notice">
                <el-icon><Bell /></el-icon>
                <span>通知中心</span>
              </el-menu-item>
            </el-sub-menu>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Sunny, Moon } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'
import { getUnreadCount } from '@/api/notice'

const router = useRouter()
const route = useRoute()

const { isDark, toggleTheme } = useTheme()

const username = ref(localStorage.getItem('username') || '教师')
const unreadCount = ref(0)

const activeMenu = computed(() => route.path)
const isLoginPage = computed(() => route.path === '/login')
const defaultOpeneds = ref(['teaching', 'campus'])

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch {
    // ignore
  }
}

const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'password') {
    router.push('/password')
  } else if (command === 'logout') {
    localStorage.clear()
    router.replace('/login')
  }
}

onMounted(() => {
  fetchUnreadCount()
})
</script>