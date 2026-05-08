<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><DataAnalysis /></el-icon> 阅读分析</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">总借阅量</div>
            <div class="stat-value">{{ analysis.totalBorrows || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">本月借阅</div>
            <div class="stat-value">{{ analysis.monthBorrows || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">阅读偏好</div>
            <div class="stat-value" style="font-size: 18px">{{ analysis.preference || '--' }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" style="margin-top: 20px">
        <template #header><span>阅读趋势</span></template>
        <div class="chart-placeholder">
          <el-icon :size="48" color="#c0c4cc"><TrendCharts /></el-icon>
          <p>阅读趋势图表区域</p>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReadingAnalysis } from '@/api/ai'

const loading = ref(false)
const analysis = ref({})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getReadingAnalysis()
    analysis.value = res.data || {}
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }

.stat-card { text-align: center; padding: 16px 0; }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 700; color: #303133; }

.chart-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 250px;
  background: #fafafa;
  border-radius: 8px;
  color: #c0c4cc;
}

.chart-placeholder p { margin-top: 12px; font-size: 14px; }
</style>
