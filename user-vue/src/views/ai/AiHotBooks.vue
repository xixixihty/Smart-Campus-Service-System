<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><TrendCharts /></el-icon> 热门图书</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableData" stripe border>
        <el-table-column type="index" label="排名" width="60" align="center" />
        <el-table-column prop="bookName" label="书名" min-width="200" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="borrowCount" label="借阅次数" width="110" align="center" sortable />
        <el-table-column label="热度" width="200">
          <template #default="{ row }">
            <el-progress :percentage="row.hotScore || 0" :stroke-width="10" :color="getHotColor(row.hotScore)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHotBooks } from '@/api/ai'

const loading = ref(false)
const tableData = ref([])

const getHotColor = (score) => {
  if (score >= 80) return '#F56C6C'
  if (score >= 60) return '#E6A23C'
  if (score >= 40) return '#409EFF'
  return '#909399'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getHotBooks()
    tableData.value = res.data || []
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
</style>
