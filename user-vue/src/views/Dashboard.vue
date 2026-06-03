<template>
  <div class="dashboard">
    <div class="welcome-section">
      <h2><el-icon><Sunny /></el-icon> {{ greeting }}，{{ username }}同学！</h2>
      <p>今天是 {{ today }}，祝你生活愉快！</p>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card shimmer" @click="$router.push('/my-courses')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6f7ff">
              <el-icon :size="28" color="#1890ff"><Notebook /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value" ref="countCourse">{{ stats.courseCount }}</div>
              <div class="stat-label">已选课程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card shimmer" @click="$router.push('/my-borrows')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fff7e6">
              <el-icon :size="28" color="#fa8c16"><CollectionTag /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value" ref="countBorrow">{{ stats.borrowCount }}</div>
              <div class="stat-label">借阅中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card shimmer" @click="$router.push('/my-reservations')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f6ffed">
              <el-icon :size="28" color="#52c41a"><Chair /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value" ref="countReserve">{{ stats.reservationCount }}</div>
              <div class="stat-label">座位预约</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card shimmer" @click="$router.push('/my-scores')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fff0f6">
              <el-icon :size="28" color="#eb2f96"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value" ref="countGpa">{{ stats.gpa }}</div>
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
                <span class="notice-dot" :class="{ 'dot-unread': isRecentNotice(item.publishTime) }"></span>
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
          <el-row :gutter="16">
            <el-col :span="6" v-for="item in aiActions" :key="item.path">
              <div class="ai-card" @click="$router.push(item.path)">
                <div class="ai-card-icon" :style="{ background: item.bg }">
                  <el-icon :size="32" :color="item.color"><component :is="item.icon" /></el-icon>
                </div>
                <div class="ai-card-label">{{ item.label }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { getTimetable } from '@/api/courseSchedule'
import { getCurrentSemester } from '@/api/semester'
import { getMyCourses } from '@/api/courseSelection'
import { getMyBorrows } from '@/api/borrowRecord'
import { getReservationList } from '@/api/seatReservation'
import { getMyGPA } from '@/api/scoreEntry'
import { getMyNotices } from '@/api/notice'

const username = ref(localStorage.getItem('username') || '同学')
const loading = ref(true)

const countCourse = ref(null)
const countBorrow = ref(null)
const countReserve = ref(null)
const countGpa = ref(null)

const animateCount = (el, target) => {
  if (!el || target === '--') return
  const isFloat = typeof target === 'number' && target % 1 !== 0
  const start = 0
  const end = parseFloat(target)
  if (isNaN(end)) return
  const duration = 600
  const startTime = performance.now()
  const animate = (now) => {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    const current = start + (end - start) * eased
    el.textContent = isFloat ? current.toFixed(2) : Math.floor(current)
    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      el.textContent = target
    }
  }
  requestAnimationFrame(animate)
}

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
  courseCount: '--',
  borrowCount: '--',
  reservationCount: '--',
  gpa: '--'
})

const todayCourses = ref([])
const recentNotices = ref([])

const aiActions = [
  { label: 'AI 学习助手', path: '/ai-assistant', icon: 'Cpu', bg: 'linear-gradient(135deg, #e6f7ff, #bae7ff)', color: '#1890ff' },
  { label: '成绩分析', path: '/ai-assistant', icon: 'TrendCharts', bg: 'linear-gradient(135deg, #f6ffed, #d9f7be)', color: '#52c41a' },
  { label: '图书推荐', path: '/ai-assistant', icon: 'Reading', bg: 'linear-gradient(135deg, #fff7e6, #ffe7ba)', color: '#fa8c16' },
  { label: '选课建议', path: '/ai-assistant', icon: 'EditPen', bg: 'linear-gradient(135deg, #fff0f6, #ffd6e7)', color: '#eb2f96' }
]

const isRecentNotice = (publishTime) => {
  if (!publishTime) return false
  const pubDate = new Date(publishTime)
  const now = new Date()
  return (now - pubDate) < 24 * 60 * 60 * 1000
}

const parseWeekRange = (rangeStr) => {
  if (!rangeStr) return { startWeek: 0, endWeek: 0 }
  const parts = rangeStr.split('-')
  return {
    startWeek: parseInt(parts[0]) || 0,
    endWeek: parseInt(parts[1]) || 0
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const todayWeekDay = new Date().getDay() === 0 ? 7 : new Date().getDay()

    let currentWeekNum = 0
    try {
      const semRes = await getCurrentSemester()
      if (semRes.data && semRes.data.startDate) {
        const startDate = new Date(semRes.data.startDate)
        const today = new Date()
        const diffDays = Math.floor((today - startDate) / (1000 * 60 * 60 * 24))
        currentWeekNum = Math.max(1, Math.floor(diffDays / 7) + 1)
      }
    } catch (e) {
      console.warn('获取当前学期信息失败:', e)
    }

    const [courseRes, timetableRes, borrowRes, reserveRes, gpaRes, noticeRes] = await Promise.allSettled([
      getMyCourses({ pageNum: 1, pageSize: 1 }),
      getTimetable(),
      getMyBorrows({ status: '借出中', pageNum: 1, pageSize: 1 }),
      getReservationList({ status: 'RESERVED', pageNum: 1, pageSize: 1 }),
      getMyGPA(),
      getMyNotices({ pageNum: 1, pageSize: 5 })
    ])

    stats.courseCount = courseRes.status === 'fulfilled' ? (courseRes.value.data?.total ?? '--') : '--'
    stats.borrowCount = borrowRes.status === 'fulfilled' ? (borrowRes.value.data?.total ?? '--') : '--'
    stats.reservationCount = reserveRes.status === 'fulfilled' ? (reserveRes.value.data?.total ?? '--') : '--'
    stats.gpa = gpaRes.status === 'fulfilled' ? (gpaRes.value.data?.gpa ?? gpaRes.value.data ?? '--') : '--'

    await nextTick()
    animateCount(countCourse.value, stats.courseCount)
    animateCount(countBorrow.value, stats.borrowCount)
    animateCount(countReserve.value, stats.reservationCount)
    animateCount(countGpa.value, stats.gpa)

    if (timetableRes.status === 'fulfilled') {
      const allCourses = timetableRes.value.data || []
      todayCourses.value = allCourses
        .filter(item => {
          if (item.weekDay !== todayWeekDay) return false
          const { startWeek, endWeek } = parseWeekRange(item.weekRange)
          if (currentWeekNum > 0 && startWeek > 0 && endWeek > 0) {
            if (currentWeekNum < startWeek || currentWeekNum > endWeek) return false
          }
          return true
        })
        .sort((a, b) => a.startSection - b.startSection)
        .map(item => ({
          ...item,
          startPeriod: item.startSection,
          endPeriod: item.endSection
        }))
    }

    if (noticeRes.status === 'fulfilled') {
      const notices = noticeRes.value.data?.list || noticeRes.value.data || []
      recentNotices.value = notices.slice(0, 5)
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
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
  transition: transform 0.25s, box-shadow 0.25s;
  position: relative;
  overflow: hidden;
  border-radius: 12px;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.shimmer::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
  transform: skewX(-20deg);
  transition: left 0.6s;
}

.shimmer:hover::after {
  left: 150%;
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

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dcdfe6;
  flex-shrink: 0;
  margin-right: 6px;
}

.dot-unread {
  background: #409EFF;
  box-shadow: 0 0 6px rgba(64, 158, 255, 0.4);
}

.ai-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s;
  background: #fff;
  border: 1px solid #ebeef5;
}

.ai-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.1);
}

.ai-card-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.ai-card:hover .ai-card-icon {
  transform: scale(1.1);
}

.ai-card-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

@media (max-width: 768px) {
  .welcome-section h2 { font-size: 18px; }
  .stat-value { font-size: 20px; }
  .stat-icon { width: 44px; height: 44px; }
}
</style>
