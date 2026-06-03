<template>
  <div class="dashboard">
    <h2 class="page-title">
      <el-icon><Odometer /></el-icon>
      教师工作台
    </h2>

    <!-- 学期选择 -->
    <div class="toolbar">
      <span class="toolbar-label">当前学期：</span>
      <el-select v-model="currentSemesterId" placeholder="请选择学期" @change="onSemesterChange" style="width: 240px">
        <el-option
          v-for="s in semesterList"
          :key="s.id"
          :label="s.semesterName"
          :value="s.id"
        />
      </el-select>
    </div>

    <div v-if="loading" class="loading-area">
      <el-skeleton :rows="3" animated />
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12"><el-skeleton :rows="6" animated /></el-col>
        <el-col :span="12"><el-skeleton :rows="6" animated /></el-col>
      </el-row>
    </div>

    <div v-else-if="loadError" class="error-area">
      <el-result icon="error" title="数据加载失败" sub-title="无法获取工作台统计数据">
        <template #extra>
          <el-button type="primary" @click="fetchStats">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <template v-else>
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
                <el-icon :size="28" color="#fff"><Notebook /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.teachingCourseCount || 0 }}</div>
                <div class="stat-label">授课课程</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
                <el-icon :size="28" color="#fff"><Grid /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.teachingClassCount || 0 }}</div>
                <div class="stat-label">授课班级</div>
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
                <div class="stat-value">{{ stats.teachingStudentCount || 0 }}</div>
                <div class="stat-label">授课学生</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
                <el-icon :size="28" color="#fff"><Checked /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.pendingLeaveCount || 0 }}</div>
                <div class="stat-label">待审批请假</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
                <el-icon :size="28" color="#fff"><RefreshRight /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.pendingRescheduleCount || 0 }}</div>
                <div class="stat-label">待处理调课</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)">
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

      <!-- 图表区域 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><Histogram /></el-icon> 班级学生分布</span>
              </div>
            </template>
            <div ref="classChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><PieChart /></el-icon> 成绩分布</span>
              </div>
            </template>
            <div ref="scoreChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 快捷操作 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="24">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><Promotion /></el-icon> 快捷操作</span>
              </div>
            </template>
            <el-row :gutter="12">
              <el-col :span="3" v-for="item in quickActions" :key="item.path">
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
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardStats } from '@/api/dashboard'
import { getSemesterList } from '@/api/semester'
import * as echarts from 'echarts'
import {
  Odometer, Notebook, Grid, User, Checked, RefreshRight, Tickets,
  Histogram, PieChart, Promotion, Calendar, Bell
} from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(true)
const loadError = ref(false)
const currentSemesterId = ref(null)
const semesterList = ref([])

const stats = reactive({
  teachingCourseCount: 0,
  teachingClassCount: 0,
  teachingStudentCount: 0,
  pendingLeaveCount: 0,
  pendingRescheduleCount: 0,
  scoreEntryRate: 0,
  classStudentDistribution: [],
  scoreDistribution: []
})

const scoreEntryPercent = computed(() => {
  const rate = stats.scoreEntryRate
  if (rate == null || rate === undefined) return '--'
  return Number(rate).toFixed(1)
})

const classChartRef = ref(null)
const scoreChartRef = ref(null)
let classChart = null
let scoreChart = null

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

const fetchSemesters = async () => {
  try {
    const res = await getSemesterList({ pageNum: 1, pageSize: 50 })
    semesterList.value = res.data?.list || []
    if (semesterList.value.length > 0) {
      currentSemesterId.value = semesterList.value[0].id
    }
  } catch {
    // ignore
  }
}

const fetchStats = async () => {
  if (!currentSemesterId.value) return
  loading.value = true
  loadError.value = false
  try {
    const res = await getDashboardStats(currentSemesterId.value)
    const data = res.data || {}
    Object.assign(stats, data)
    await nextTick()
    renderCharts()
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  // 班级学生分布图
  if (classChartRef.value) {
    if (!classChart) {
      classChart = echarts.init(classChartRef.value)
    }
    const classData = stats.classStudentDistribution || []
    classChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: classData.map(d => d.className || ''),
        axisLabel: { rotate: 30 }
      },
      yAxis: { type: 'value', name: '学生人数' },
      series: [{
        type: 'bar',
        data: classData.map(d => d.studentCount || 0),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#66b1ff' }
          ])
        },
        barMaxWidth: 40
      }],
      grid: { left: 50, right: 20, bottom: 60, top: 20 }
    }, true)
  }

  // 成绩分布图
  if (scoreChartRef.value) {
    if (!scoreChart) {
      scoreChart = echarts.init(scoreChartRef.value)
    }
    const scoreData = stats.scoreDistribution || []
    scoreChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: scoreData.map(d => ({ name: d.rangeLabel || d.range || '', value: d.count || 0 })),
        label: { show: true, formatter: '{b}: {c}' },
        emphasis: {
          label: { fontSize: 16, fontWeight: 'bold' }
        }
      }]
    }, true)
  }
}

const onResize = () => {
  classChart?.resize()
  scoreChart?.resize()
}

const onSemesterChange = () => {
  fetchStats()
}

onMounted(async () => {
  await fetchSemesters()
  if (currentSemesterId.value) {
    await fetchStats()
  }
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  classChart?.dispose()
  scoreChart?.dispose()
})
</script>