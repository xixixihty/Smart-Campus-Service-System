<template>
  <div class="dashboard">
    <h2 class="page-title">
      <el-icon><Odometer /></el-icon>
      工作台
    </h2>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6f7ff">
              <el-icon :size="32" color="#1890ff"><School /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.collegeCount }}</div>
              <div class="stat-label">学院总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fff7e6">
              <el-icon :size="32" color="#fa8c16"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.teacherCount }}</div>
              <div class="stat-label">教师总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f6ffed">
              <el-icon :size="32" color="#52c41a"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.studentCount }}</div>
              <div class="stat-label">学生总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fff0f6">
              <el-icon :size="32" color="#eb2f96"><Notebook /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.courseCount }}</div>
              <div class="stat-label">课程总数</div>
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
              <span><el-icon><Bell /></el-icon> 快捷操作</span>
            </div>
          </template>
          <el-row :gutter="12">
            <el-col :span="8" v-for="item in quickActions" :key="item.path">
              <div class="quick-action" @click="$router.push(item.path)">
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
              <span><el-icon><Cpu /></el-icon> AI 智能分析</span>
            </div>
          </template>
          <el-row :gutter="12">
            <el-col :span="8" v-for="item in aiActions" :key="item.path">
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
import { reactive, onMounted } from 'vue'
import { getDashboardStats } from '@/api/dashboard'

const stats = reactive({
  collegeCount: 0,
  teacherCount: 0,
  studentCount: 0,
  courseCount: 0
})

const fetchStats = async () => {
  try {
    const res = await getDashboardStats()
    const data = res.data
    if (data) {
      stats.collegeCount = data.collegeCount || 0
      stats.teacherCount = data.teacherCount || 0
      stats.studentCount = data.studentCount || 0
      stats.courseCount = data.courseCount || 0
    }
  } catch {
    // 保持默认值 0
  }
}

onMounted(fetchStats)

const quickActions = [
  { label: '发布通知', path: '/notice', icon: 'Bell', color: '#409EFF' },
  { label: '录入成绩', path: '/score-entry', icon: 'Tickets', color: '#67C23A' },
  { label: '审批请假', path: '/leave-approval', icon: 'Checked', color: '#E6A23C' },
  { label: '管理图书', path: '/book', icon: 'CollectionTag', color: '#F56C6C' },
  { label: '排课管理', path: '/course-schedule', icon: 'Timer', color: '#909399' },
  { label: '座位管理', path: '/seat', icon: 'Chair', color: '#409EFF' }
]

const aiActions = [
  { label: '校园概览', path: '/ai-campus', icon: 'DataAnalysis', color: '#409EFF' },
  { label: '教学质量', path: '/ai-teaching', icon: 'TrendCharts', color: '#67C23A' },
  { label: '成绩分析', path: '/ai-score', icon: 'PieChart', color: '#E6A23C' }
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
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
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
