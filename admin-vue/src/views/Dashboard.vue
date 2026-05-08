<template>
  <div class="dashboard">
    <h2 class="page-title">
      <el-icon><Odometer /></el-icon>
      工作台
    </h2>

    <div v-if="loading" class="loading-area">
      <el-skeleton :rows="3" animated />
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12"><el-skeleton :rows="6" animated /></el-col>
        <el-col :span="12"><el-skeleton :rows="6" animated /></el-col>
      </el-row>
    </div>

    <div v-else-if="loadError" class="error-area">
      <el-result icon="error" title="数据加载失败" sub-title="无法获取工作台统计数据，请检查网络或重新登录">
        <template #extra>
          <el-button type="primary" @click="fetchStats">重新加载</el-button>
          <el-button @click="router.push('/login')">返回登录</el-button>
        </template>
      </el-result>
    </div>

    <template v-else>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28" color="#fff"><School /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.collegeCount }}</div>
              <div class="stat-label">学院总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28" color="#fff"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.teacherCount }}</div>
              <div class="stat-label">教师总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="28" color="#fff"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.studentCount }}</div>
              <div class="stat-label">学生总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28" color="#fff"><Notebook /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.courseCount }}</div>
              <div class="stat-label">课程总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
              <el-icon :size="28" color="#fff"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.borrowCount }}</div>
              <div class="stat-label">借阅记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)">
              <el-icon :size="28" color="#fff"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.leaveCount }}</div>
              <div class="stat-label">请假申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><Histogram /></el-icon> 各学院学生分布</span>
            </div>
          </template>
          <div ref="collegeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><PieChart /></el-icon> 课程类型分布</span>
            </div>
          </template>
          <div ref="courseTypeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><TrendCharts /></el-icon> 近7天借阅趋势</span>
            </div>
          </template>
          <div ref="borrowTrendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><DataBoard /></el-icon> 教师职称分布</span>
            </div>
          </template>
          <div ref="teacherTitleChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><Notebook /></el-icon> 请假状态统计</span>
            </div>
          </template>
          <div ref="leaveStatusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><Bell /></el-icon> 快捷操作</span>
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
    </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardStats } from '@/api/dashboard'
import * as echarts from 'echarts'
import {
  Odometer, School, UserFilled, User, Notebook, Reading, Document,
  Histogram, PieChart, TrendCharts, DataBoard, Bell, Place,
  Tickets, Checked, CollectionTag, Timer
} from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(true)
const loadError = ref(false)

const stats = reactive({
  collegeCount: 0,
  teacherCount: 0,
  studentCount: 0,
  courseCount: 0,
  borrowCount: 0,
  leaveCount: 0,
  noticeCount: 0
})

const collegeChartRef = ref(null)
const courseTypeChartRef = ref(null)
const borrowTrendChartRef = ref(null)
const teacherTitleChartRef = ref(null)
const leaveStatusChartRef = ref(null)

let collegeChart = null
let courseTypeChart = null
let borrowTrendChart = null
let teacherTitleChart = null
let leaveStatusChart = null

const colorPalette = [
  '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
  '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#5470c6'
]

const initCollegeChart = (data) => {
  if (!collegeChart) {
    collegeChart = echarts.init(collegeChartRef.value)
  }
  const names = data.map(item => item.name || '未知学院')
  const values = data.map(item => item.value || 0)

  collegeChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: names, axisLabel: { interval: 0, rotate: 30 } },
    yAxis: { type: 'value' },
    series: [{
      name: '学生人数',
      type: 'bar',
      data: values,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2378f7' },
            { offset: 0.7, color: '#2378f7' },
            { offset: 1, color: '#83bff6' }
          ])
        }
      },
      barWidth: '50%'
    }]
  })
}

const initCourseTypeChart = (data) => {
  if (!courseTypeChart) {
    courseTypeChart = echarts.init(courseTypeChartRef.value)
  }
  const pieData = data.map(item => ({ name: item.name || '未知', value: item.value || 0 }))

  courseTypeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      name: '课程类型',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: 18, fontWeight: 'bold' }
      },
      labelLine: { show: false },
      data: pieData,
      color: colorPalette
    }]
  })
}

const initBorrowTrendChart = (data) => {
  if (!borrowTrendChart) {
    borrowTrendChart = echarts.init(borrowTrendChartRef.value)
  }
  const dates = data.map(item => item.name || '')
  const values = data.map(item => item.value || 0)

  borrowTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value' },
    series: [{
      name: '借阅次数',
      type: 'line',
      data: values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { width: 3, color: '#5470c6' },
      itemStyle: { color: '#5470c6' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(84, 112, 198, 0.5)' },
          { offset: 1, color: 'rgba(84, 112, 198, 0.05)' }
        ])
      }
    }]
  })
}

const initTeacherTitleChart = (data) => {
  if (!teacherTitleChart) {
    teacherTitleChart = echarts.init(teacherTitleChartRef.value)
  }
  const names = data.map(item => item.name || '未知')
  const values = data.map(item => item.value || 0)

  teacherTitleChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: names },
    series: [{
      name: '人数',
      type: 'bar',
      data: values,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#91cc75' },
          { offset: 1, color: '#3ba272' }
        ])
      },
      barWidth: '50%'
    }]
  })
}

const initLeaveStatusChart = (data) => {
  if (!leaveStatusChart) {
    leaveStatusChart = echarts.init(leaveStatusChartRef.value)
  }
  const pieData = data.map(item => ({ name: item.name || '未知', value: item.value || 0 }))

  leaveStatusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      name: '请假状态',
      type: 'pie',
      radius: '65%',
      center: ['60%', '55%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{d}%' },
      data: pieData,
      color: ['#5470c6', '#91cc75', '#fac858', '#ee6666']
    }]
  })
}

const fetchStats = async () => {
  loading.value = true
  loadError.value = false
  try {
    const res = await getDashboardStats()
    const data = res.data
    if (data) {
      stats.collegeCount = data.collegeCount || 0
      stats.teacherCount = data.teacherCount || 0
      stats.studentCount = data.studentCount || 0
      stats.courseCount = data.courseCount || 0
      stats.borrowCount = data.borrowCount || 0
      stats.leaveCount = data.leaveCount || 0
      stats.noticeCount = data.noticeCount || 0
    }
    loading.value = false
    await nextTick()

    if (data) {
      if (data.studentByCollege && data.studentByCollege.length > 0) {
        initCollegeChart(data.studentByCollege)
      }
      if (data.courseByType && data.courseByType.length > 0) {
        initCourseTypeChart(data.courseByType)
      }
      if (data.borrowTrend7Days && data.borrowTrend7Days.length > 0) {
        initBorrowTrendChart(data.borrowTrend7Days)
      }
      if (data.teacherByTitle && data.teacherByTitle.length > 0) {
        initTeacherTitleChart(data.teacherByTitle)
      }
      if (data.leaveByStatus && data.leaveByStatus.length > 0) {
        initLeaveStatusChart(data.leaveByStatus)
      }
    }
  } catch (e) {
    console.error('获取工作台统计数据失败:', e)
    loadError.value = true
    loading.value = false
  }
}

const handleResize = () => {
  collegeChart?.resize()
  courseTypeChart?.resize()
  borrowTrendChart?.resize()
  teacherTitleChart?.resize()
  leaveStatusChart?.resize()
}

onMounted(fetchStats)
onMounted(() => window.addEventListener('resize', handleResize))
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  collegeChart?.dispose()
  courseTypeChart?.dispose()
  borrowTrendChart?.dispose()
  teacherTitleChart?.dispose()
  leaveStatusChart?.dispose()
})

const quickActions = [
  { label: '发布通知', path: '/notice', icon: 'Bell', color: '#409EFF' },
  { label: '录入成绩', path: '/score-entry', icon: 'Tickets', color: '#67C23A' },
  { label: '审批请假', path: '/leave-approval', icon: 'Checked', color: '#E6A23C' },
  { label: '管理图书', path: '/book', icon: 'CollectionTag', color: '#F56C6C' },
  { label: '排课管理', path: '/course-schedule', icon: 'Timer', color: '#909399' },
  { label: '座位管理', path: '/seat', icon: 'Place', color: '#409EFF' }
]
</script>

<style scoped>
.page-title {
  font-size: 22px;
  color: #303133;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-area {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.error-area {
  padding: 40px 20px;
  background: #fff;
  border-radius: 8px;
}

.stats-row {
  margin-bottom: 0;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 15px;
}

.chart-container {
  width: 100%;
  height: 320px;
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