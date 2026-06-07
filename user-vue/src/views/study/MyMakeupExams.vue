<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><EditPen /></el-icon> 补考信息</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableData" stripe border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column prop="examDate" label="考试日期" width="120" />
        <el-table-column prop="location" label="考试地点" min-width="180" />
        <el-table-column prop="status" label="状态" width="100" align="center" />
        <el-table-column label="详情" width="80" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="补考详情" width="450px">
      <el-descriptions :column="1" border v-if="currentExam">
        <el-descriptions-item label="课程名称">{{ currentExam.courseName }}</el-descriptions-item>
        <el-descriptions-item label="考试日期">{{ currentExam.examDate }}</el-descriptions-item>
        <el-descriptions-item label="考试地点">{{ currentExam.location }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentExam.status }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyMakeupExams } from '@/api/makeupExam'

const loading = ref(false)
const detailVisible = ref(false)
const currentExam = ref(null)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({ pageNum: 1, pageSize: 10 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyMakeupExams(queryForm)
    tableData.value = res.data.list || res.data || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const showDetail = (row) => {
  currentExam.value = row
  detailVisible.value = true
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
