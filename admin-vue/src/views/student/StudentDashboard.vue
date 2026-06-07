<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Odometer /></el-icon> 学生工作台</h2>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon stat-icon-gradient-1">
              <el-icon :size="28" color="#fff"><Notebook /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.courseCount ?? '-' }}</div>
              <div class="stat-label">已选课程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon stat-icon-gradient-2">
              <el-icon :size="28" color="#fff"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.avgScore ?? '-' }}</div>
              <div class="stat-label">平均成绩</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon stat-icon-gradient-3">
              <el-icon :size="28" color="#fff"><EditPen /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingLeaveCount ?? '-' }}</div>
              <div class="stat-label">待批请假</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon stat-icon-gradient-4">
              <el-icon :size="28" color="#fff"><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayCourseCount ?? '-' }}</div>
              <div class="stat-label">今日课程</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

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
            <el-table-column prop="teacherName" label="教师" width="100" />
            <el-table-column prop="classroomName" label="教室" width="120" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStudentDashboardStats, getStudentTimetable, getStudentNoticeList } from '@/api/student'

const loading = ref(false)
const stats = reactive({})
const todayTimetable = ref([])
const recentNotices = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const [statsRes, timetableRes, noticesRes] = await Promise.all([
      getStudentDashboardStats(),
      getStudentTimetable({ today: true }),
      getStudentNoticeList({ pageNum: 1, pageSize: 5 })
    ])
    Object.assign(stats, statsRes.data || {})
    todayTimetable.value = timetableRes.data?.records || timetableRes.data || []
    recentNotices.value = noticesRes.data?.records || noticesRes.data || []
  } catch (e) {
    console.error('加载学生工作台数据失败:', e)
  } finally {
    loading.value = false
  }
})
</script>