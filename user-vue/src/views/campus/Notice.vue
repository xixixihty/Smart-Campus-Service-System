<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Bell /></el-icon> 通知公告</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableData" stripe border @row-click="showDetail">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="标题" min-width="300" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column prop="publishTime" label="发布时间" width="170" />
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="通知详情" width="600px">
      <template v-if="currentNotice">
        <h3 style="margin-bottom: 16px">{{ currentNotice.title }}</h3>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="发布人">{{ currentNotice.publisherName }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentNotice.publishTime }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px; line-height: 1.8; white-space: pre-wrap">{{ currentNotice.content }}</div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyNotices, getNoticeDetail } from '@/api/notice'

const loading = ref(false)
const detailVisible = ref(false)
const currentNotice = ref(null)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({ pageNum: 1, pageSize: 10 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyNotices(queryForm)
    tableData.value = res.data.list || res.data || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const showDetail = async (row) => {
  try {
    const res = await getNoticeDetail(row.id)
    currentNotice.value = res.data || res
    detailVisible.value = true
  } catch {
    currentNotice.value = row
    detailVisible.value = true
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
