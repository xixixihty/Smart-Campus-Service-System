<template>
  <div class="dashboard">
    <div class="welcome-section">
      <h2><el-icon><Sunny /></el-icon> {{ greeting }}，{{ username }}同学！</h2>
      <p>今天是 {{ today }}，祝你生活愉快！</p>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/my-courses')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6f7ff">
              <el-icon :size="28" color="#1890ff"><Notebook /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.courseCount }}</div>
              <div class="stat-label">已选课程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/my-borrows')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fff7e6">
              <el-icon :size="28" color="#fa8c16"><CollectionTag /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.borrowCount }}</div>
              <div class="stat-label">借阅中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/my-reservations')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f6ffed">
              <el-icon :size="28" color="#52c41a"><Chair /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.reservationCount }}</div>
              <div class="stat-label">座位预约</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/my-scores')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fff0f6">
              <el-icon :size="28" color="#eb2f96"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.gpa }}</div>
              <div class="stat-label">平均绩点</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><Clock /></el-icon> 今日课表</span>
              <el-button type="primary" link @click="$router.push('/timetable')">查看全部</el-button>
            </div>
          </template>
          <el-timeline v-if="todayCourses.length > 0">
            <el-timeline-item
              v-for="item in todayCourses"
              :key="item.id"
              :timestamp="`第${item.startPeriod}-${item.endPeriod}节`"
              placement="top"
            >
              <el-card shadow="never" class="course-card">
                <div class="course-name">{{ item.courseName }}</div>
                <div class="course-info">
                  <el-icon><Location /></el-icon> {{ item.classroomName }}
                  <el-icon style="margin-left: 12px"><User /></el-icon> {{ item.teacherName }}
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="今天没有课程，好好休息吧~" :image-size="80" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><Bell /></el-icon> 最新通知</span>
              <el-button type="primary" link @click="$router.push('/notice')">查看全部</el-button>
            </div>
          </template>
          <div v-if="recentNotices.length > 0" class="notice-list">
            <div v-for="item in recentNotices" :key="item.id" class="notice-item" @click="$router.push('/notice')">
              <div class="notice-title">
                <el-tag :type="item.noticeType === 'URGENT' ? 'danger' : ''" size="small" class="notice-tag">
                  {{ item.noticeType === 'URGENT' ? '紧急' : item.noticeType === 'TEACHING' ? '教学' : item.noticeType === 'ACTIVITY' ? '活动' : '系统' }}
                </el-tag>
                <span>{{ item.title }}</span>
              </div>
              <div class="notice-time">{{ item.publishTime }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无通知" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><Cpu /></el-icon> AI 智能助手</span>
            </div>
          </template>
          <el-row :gutter="12">
            <el-col :span="4" v-for="item in aiActions" :key="item.path">
              <div class="quick-action" @click="$router.push(item.path)">
                <el-icon :size="28" :color="item.color"><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

const username = ref(localStorage.getItem('username') || '同学')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const today = computed(() => {
  const d = new Date()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekDays[d.getDay()]}`
})

const stats = reactive({
  courseCount: 5,
  borrowCount: 2,
  reservationCount: 1,
  gpa: '3.6'
})

const todayCourses = ref([
  { id: 1, courseName: '高等数学', classroomName: '教学楼A-301', teacherName: '张教授', startPeriod: 1, endPeriod: 2 },
  { id: 2, courseName: '大学英语', classroomName: '教学楼B-205', teacherName: '李老师', startPeriod: 3, endPeriod: 4 }
])

const recentNotices = ref([
  { id: 1, title: '关于2025年春季学期选课通知', noticeType: 'TEACHING', publishTime: '2025-03-01 10:00' },
  { id: 2, title: '图书馆开放时间调整通知', noticeType: 'SYSTEM', publishTime: '2025-02-28 14:30' },
  { id: 3, title: '校园运动会报名通知', noticeType: 'ACTIVITY', publishTime: '2025-02-27 09:00' }
])

const aiActions = [
  { label: 'AI 学习助手', path: '/ai-assistant', icon: 'Cpu', color: '#409EFF' }
]
</script>

<style scoped>
.welcome-section {
  margin-bottom: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
  border-radius: 12px;
}

.welcome-section h2 {
  font-size: 22px;
  color: #1a3a5c;
  display: flex;
  align-items: center;
  gap: 8px;
}

.welcome-section p {
  color: #606266;
  margin-top: 8px;
  font-size: 14px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
}

.course-card {
  border: 1px solid #e4e7ed;
}

.course-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.course-info {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
  display: flex;
  align-items: center;
}

.notice-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.notice-item:hover {
  background: #f5f7fa;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #303133;
}

.notice-tag {
  flex-shrink: 0;
}

.notice-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
  margin-left: 50px;
}

.quick-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.quick-action:hover {
  background: #f5f7fa;
}

.quick-action span {
  font-size: 13px;
  color: #606266;
}
</style>
