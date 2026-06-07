<template>
  <div class="page-container teacher-dashboard">
    <div class="page-header">
      <h2><el-icon><Odometer /></el-icon> 教师工作台</h2>
      <el-tag type="primary" size="large" effect="plain">
        <el-icon><Calendar /></el-icon>
        {{ currentSemesterName || '加载中...' }}
      </el-tag>
    </div>

    <!-- 骨架屏加载态 -->
    <div v-if="loading" class="loading-area">
      <el-skeleton :rows="3" animated />
      <el-row :gutter="20" class="section-gap">
        <el-col :span="12"><el-skeleton :rows="6" animated /></el-col>
        <el-col :span="12"><el-skeleton :rows="6" animated /></el-col>
      </el-row>
    </div>

    <!-- 错误态 -->
    <div v-else-if="loadError" class="error-area">
      <el-result icon="error" title="数据加载失败" sub-title="无法获取工作台统计数据，请检查网络或重新登录">
        <template #extra>
          <el-button type="primary" @click="fetchAllData">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <template v-else>
      <!-- 统计卡片 -->
      <el-row :gutter="20">
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon stat-icon-gradient-1">
                <el-icon :size="28" color="#fff"><Notebook /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.teachingCourseCount ?? '-' }}</div>
                <div class="stat-label">所授课程</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon stat-icon-gradient-2">
                <el-icon :size="28" color="#fff"><Grid /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.teachingClassCount ?? '-' }}</div>
                <div class="stat-label">授课班级</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon stat-icon-gradient-3">
                <el-icon :size="28" color="#fff"><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.teachingStudentCount ?? '-' }}</div>
                <div class="stat-label">授课学生</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon stat-icon-gradient-4">
                <el-icon :size="28" color="#fff"><Checked /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.pendingLeaveCount ?? '-' }}</div>
                <div class="stat-label">待批请假</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon stat-icon-gradient-5">
                <el-icon :size="28" color="#fff"><Refresh /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.pendingRescheduleCount ?? '-' }}</div>
                <div class="stat-label">待处理调课</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon stat-icon-gradient-6">
                <el-icon :size="28" color="#fff"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.scoreEntryRate ?? '-' }}%</div>
                <div class="stat-label">成绩录入率</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区 -->
      <el-row :gutter="20" class="section-gap">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Histogram /></el-icon><span>班级学生分布</span>
              </div>
            </template>
            <div ref="classChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><PieChart /></el-icon><span>成绩分布</span>
              </div>
            </template>
            <div ref="scoreChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 今日课表 + 最新通知 -->
      <el-row :gutter="20" class="section-gap">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><Timer /></el-icon><span>今日课表</span>
              </div>
            </template>
            <el-table :data="todayTimetable" stripe border max-height="300" size="small">
              <el-table-column prop="period" label="节次" width="80" align="center" />
              <el-table-column prop="courseName" label="课程名称" min-width="150" />
              <el-table-column prop="classroomName" label="教室" width="120" />
              <el-table-column prop="classNames" label="班级" min-width="120" />
            </el-table>
            <el-empty v-if="todayTimetable.length === 0" description="今日无课" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><Bell /></el-icon><span>最新通知</span>
              </div>
            </template>
            <div v-if="recentNotices.length > 0" class="notice-list">
              <div v-for="item in recentNotices" :key="item.id" class="notice-item">
                <span class="notice-title">{{ item.title }}</span>
                <span class="notice-time">{{ item.publishTime?.substring(0, 10) }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无通知" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 快捷操作 -->
      <el-row :gutter="20" class="section-gap">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><DataBoard /></el-icon><span>快捷操作</span>
              </div>
            </template>
            <el-row :gutter="12">
              <el-col :span="8" v-for="item in quickActions" :key="item.path">
                <div class="quick-action" @click="router.push(item.path)">
                  <el-icon :size="28" :color="item.color"><component :is="item.icon" /></el-icon>
                  <span>{{ item.label }}</span>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><TrendCharts /></el-icon><span>请假趋势</span>
              </div>
            </template>
            <div ref="leaveChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getTeacherDashboardStats, getTeacherTimetable, getTeacherNoticeList } from '@/api/teacher'
import { getSemesterList } from '@/api/semester'
import * as echarts from 'echarts'
import {
  Odometer, Notebook, Grid, User, Checked, Timer, Bell,
  Histogram, PieChart, TrendCharts, DataBoard, Tickets, EditPen, Refresh,
  Document, Calendar
} from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(true)
const loadError = ref(false)
const currentSemesterName = ref('')

const stats = reactive({})
const todayTimetable = ref([])
const recentNotices = ref([])

const classChartRef = ref(null)
const scoreChartRef = ref(null)
const leaveChartRef = ref(null)
let classChart = null
let scoreChart = null
let leaveChart = null

const quickActions = [
  { path: '/teacher/timetable', label: '我的课表', icon: Timer, color: '#409EFF' },
  { path: '/teacher/score', label: '成绩录入', icon: Tickets, color: '#52c41a' },
  { path: '/teacher/leave-approval', label: '请假审批', icon: Checked, color: '#fa8c16' },
  { path: '/teacher/reschedule', label: '调课申请', icon: Refresh, color: '#722ed1' },
  { path: '/teacher/notices', label: '通知公告', icon: Bell, color: '#f5222d' },
  { path: '/ai-assistant', label: 'AI 助手', icon: EditPen, color: '#13c2c2' }
]

const fetchAllData = async () => {
  loading.value = true
  loadError.value = false
  try {
    const [statsRes, timetableRes, noticesRes] = await Promise.all([
      getTeacherDashboardStats(),
      getTeacherTimetable({ today: true }),
      getTeacherNoticeList({ pageNum: 1, pageSize: 5 })
    ])
    const data = statsRes.data || {}
    Object.assign(stats, data)
    currentSemesterName.value = data.semesterName || ''
    todayTimetable.value = timetableRes.data?.records || timetableRes.data || []
    recentNotices.value = noticesRes.data?.records || noticesRes.data || []

    await nextTick()
    initClassChart(data.classStudentDistribution || [])
    initScoreChart(data.scoreDistribution || [])
    initLeaveChart(data.leaveTrend || [])
  } catch (e) {
    console.error('加载教师工作台数据失败:', e)
    loadError.value = true
  } finally {
    loading.value = false
  }
}

/** 班级学生分布 - 柱状图 */
const initClassChart = (data) => {
  if (!classChartRef.value) return
  if (!classChart) classChart = echarts.init(classChartRef.value)
  const names = data.map(item => item.className || '未知班级')
  const values = data.map(item => item.studentCount || 0)
  classChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: names, axisLabel: { interval: 0, rotate: 30 } },
    yAxis: { type: 'value' },
    series: [{
      name: '学生人数', type: 'bar', data: values,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' }, { offset: 0.5, color: '#188df0' }, { offset: 1, color: '#188df0' }
        ])
      },
      barWidth: '50%'
    }]
  })
}

/** 成绩分布 - 饼图 */
const initScoreChart = (data) => {
  if (!scoreChartRef.value) return
  if (!scoreChart) scoreChart = echarts.init(scoreChartRef.value)
  // 后端返回数组: [{ scoreRange: "0-59", count: 5, percentage: 10.0 }, ...]
  const rangeMap = { '0-59': '不及格', '60-69': '及格', '70-79': '中等', '80-89': '良好', '90-100': '优秀' }
  const pieData = data.map(item => ({
    name: rangeMap[item.scoreRange] || item.scoreRange,
    value: item.count || 0
  }))
  scoreChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      name: '成绩分布', type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: 18, fontWeight: 'bold' } },
      labelLine: { show: false },
      data: pieData,
      color: ['#ee6666', '#fac858', '#91cc75', '#5470c6', '#73c0de']
    }]
  })
}

/** 请假趋势 - 折线图 */
const initLeaveChart = (data) => {
  if (!leaveChartRef.value) return
  if (!leaveChart) leaveChart = echarts.init(leaveChartRef.value)
  const months = data.map(d => d.month || d.name || '')
  const approved = data.map(d => d.approved || 0)
  const pending = data.map(d => d.pending || 0)
  leaveChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['已通过', '待审批'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value' },
    series: [
      { name: '已通过', type: 'line', data: approved, smooth: true, itemStyle: { color: '#52c41a' } },
      { name: '待审批', type: 'line', data: pending, smooth: true, itemStyle: { color: '#fa8c16' } }
    ]
  })
}

const handleResize = () => {
  classChart?.resize()
  scoreChart?.resize()
  leaveChart?.resize()
}

onMounted(() => {
  fetchAllData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  classChart?.dispose()
  scoreChart?.dispose()
  leaveChart?.dispose()
})
</script>
