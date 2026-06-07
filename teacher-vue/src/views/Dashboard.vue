<template>
  <div class="page-container">
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
          <el-button type="primary" @click="fetchStats">重新加载</el-button>
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
                <el-icon :size="28" color="#fff"><RefreshRight /></el-icon>
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
                <el-icon :size="28" color="#fff"><Tickets /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ scoreEntryPercent }}%</div>
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

      <!-- 快捷操作 -->
      <el-row :gutter="20" class="section-gap">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Promotion /></el-icon><span>快捷操作</span>
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
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardStats } from '@/api/dashboard'
import * as echarts from 'echarts'
import {
  Odometer, Notebook, Grid, User, Checked, RefreshRight, Tickets,
  Histogram, PieChart, Promotion, Calendar, TrendCharts, Bell
} from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(true)
const loadError = ref(false)
const currentSemesterName = ref('')

const stats = reactive({
  teachingCourseCount: 0,
  teachingClassCount: 0,
  teachingStudentCount: 0,
  pendingLeaveCount: 0,
  pendingRescheduleCount: 0,
  scoreEntryRate: 0,
  classStudentDistribution: [],
  scoreDistribution: [],
  leaveTrend: []
})

const scoreEntryPercent = computed(() => {
  const rate = stats.scoreEntryRate
  if (rate == null || rate === undefined) return '--'
  return Number(rate).toFixed(1)
})

const classChartRef = ref(null)
const scoreChartRef = ref(null)
const leaveChartRef = ref(null)
let classChart = null
let scoreChart = null
let leaveChart = null

const quickActions = [
  { path: '/timetable', label: '我的课表', icon: 'Calendar', color: '#409EFF' },
  { path: '/my-classes', label: '我的班级', icon: 'Grid', color: '#67C23A' },
  { path: '/my-courses', label: '我的课程', icon: 'Notebook', color: '#E6A23C' },
  { path: '/score-entry', label: '成绩录入', icon: 'Tickets', color: '#F56C6C' },
  { path: '/course-reschedule', label: '调课管理', icon: 'RefreshRight', color: '#909399' },
  { path: '/leave-approval', label: '请假审批', icon: 'Checked', color: '#67C23A' },
  { path: '/notice', label: '通知中心', icon: 'Bell', color: '#409EFF' },
  { path: '/profile', label: '个人信息', icon: 'User', color: '#E6A23C' }
]

const fetchStats = async () => {
  loading.value = true
  loadError.value = false
  try {
    const res = await getDashboardStats()
    const data = res.data || {}
    Object.assign(stats, data)
    currentSemesterName.value = data.semesterName || ''
    await nextTick()
    renderCharts()
  } catch (e) {
    console.error('加载教师工作台数据失败:', e)
    loadError.value = true
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  // 班级学生分布图 - 柱状图
  if (classChartRef.value) {
    if (!classChart) classChart = echarts.init(classChartRef.value)
    const classData = stats.classStudentDistribution || []
    classChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: classData.map(d => d.className || '未知班级'),
        axisLabel: { interval: 0, rotate: 30 }
      },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: classData.map(d => d.studentCount || 0),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' }, { offset: 0.5, color: '#188df0' }, { offset: 1, color: '#188df0' }
          ])
        },
        barWidth: '50%'
      }]
    }, true)
  }

  // 成绩分布图 - 饼图
  if (scoreChartRef.value) {
    if (!scoreChart) scoreChart = echarts.init(scoreChartRef.value)
    const scoreData = stats.scoreDistribution || []
    const rangeMap = { '0-59': '不及格', '60-69': '及格', '70-79': '中等', '80-89': '良好', '90-100': '优秀' }
    const pieData = scoreData.map(d => ({
      name: rangeMap[d.scoreRange] || d.scoreRange || '',
      value: d.count || 0
    }))
    scoreChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 18, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: pieData,
        color: ['#ee6666', '#fac858', '#91cc75', '#5470c6', '#73c0de']
      }]
    }, true)
  }

  // 请假趋势图 - 折线图
  if (leaveChartRef.value) {
    if (!leaveChart) leaveChart = echarts.init(leaveChartRef.value)
    const trendData = stats.leaveTrend || []
    const months = trendData.map(d => d.month || '')
    const approved = trendData.map(d => d.approved || 0)
    const pending = trendData.map(d => d.pending || 0)
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
    }, true)
  }
}

const handleResize = () => {
  classChart?.resize()
  scoreChart?.resize()
  leaveChart?.resize()
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  classChart?.dispose()
  scoreChart?.dispose()
  leaveChart?.dispose()
})
</script>
