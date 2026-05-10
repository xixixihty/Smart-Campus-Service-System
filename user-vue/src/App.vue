<template>
  <div class="common-layout">
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
          <el-dropdown @command="handleUserCommand">
            <span class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
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
                <el-icon><Chair /></el-icon>
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

            <el-sub-menu index="ai">
              <template #title>
                <el-icon><Cpu /></el-icon>
                <span>AI 校园智慧助手</span>
              </template>
              <el-menu-item index="/ai-book-recommend">
                <el-icon><Star /></el-icon>
                <span>图书推荐</span>
              </el-menu-item>
              <el-menu-item index="/ai-hot-books">
                <el-icon><TrendCharts /></el-icon>
                <span>热门图书</span>
              </el-menu-item>
              <el-menu-item index="/ai-reading-analysis">
                <el-icon><DataAnalysis /></el-icon>
                <span>阅读分析</span>
              </el-menu-item>
              <el-menu-item index="/ai-score-analysis">
                <el-icon><PieChart /></el-icon>
                <span>成绩分析</span>
              </el-menu-item>
              <el-menu-item index="/ai-course-recommend">
                <el-icon><Guide /></el-icon>
                <span>课程推荐</span>
              </el-menu-item>
            </el-sub-menu>
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
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const username = ref(localStorage.getItem('username') || '同学')
const unreadNoticeCount = ref(0)

const activeMenu = computed(() => route.path)

const handleUserCommand = (command) => {
  if (command === 'logout') {
    localStorage.clear()
    router.push('/login')
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
  font-size: 14px;
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
</style>
