<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><PieChart /></el-icon> 成绩分析</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">平均分</div>
            <div class="stat-value">{{ analysis.avgScore || '--' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">最高分</div>
            <div class="stat-value">{{ analysis.maxScore || '--' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">最低分</div>
            <div class="stat-value">{{ analysis.minScore || '--' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">GPA</div>
            <div class="stat-value">{{ analysis.gpa || '--' }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" style="margin-top: 20px">
        <template #header><span>成绩分布</span></template>
        <div class="chart-placeholder">
          <el-icon :size="48" color="#c0c4cc"><PieChart /></el-icon>
          <p>成绩分布图表区域</p>
        </div>
      </el-card>

      <el-card shadow="never" style="margin-top: 20px">
        <template #header><span>AI 学习建议</span></template>
        <div v-if="analysis.suggestion" class="suggestion">
          <el-icon :size="20" color="#409EFF"><Cpu /></el-icon>
          <span>{{ analysis.suggestion }}</span>
        </div>
        <el-empty v-else description="暂无建议" :image-size="60" />
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getScoreAnalysis } from '@/api/ai'

const loading = ref(false)
const analysis = ref({})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getScoreAnalysis()
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

.suggestion {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #f0f5ff;
  border-radius: 8px;
  line-height: 1.8;
  color: #303133;
}
</style>
