<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Document /></el-icon> 我的借阅</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 200px">
            <el-option label="借出中" value="借出中" />
            <el-option label="已归还" value="已归还" />
            <el-option label="逾期" value="逾期" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px" v-loading="loading">
      <el-table :data="tableData" stripe border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="borrowNo" label="借阅编号" width="120" align="center" />
        <el-table-column prop="bookTitle" label="书名" min-width="180" align="center" />
        <el-table-column prop="borrowDate" label="借阅日期" width="120" align="center" />
        <el-table-column prop="dueDate" label="应还日期" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '借出中' ? 'warning' : row.status === '已归还' ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="info" size="small" @click="handleDetail(row)">
                <el-icon><View /></el-icon>详情
              </el-button>
              <el-button v-if="row.status === '借出中' || row.status === '逾期'" type="success" size="small" @click="handleReturn(row)">
                归还
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="借阅详情" width="500px">
      <el-descriptions :column="1" border v-if="currentBorrow">
        <el-descriptions-item label="借阅编号">{{ currentBorrow.borrowNo }}</el-descriptions-item>
        <el-descriptions-item label="书名">{{ currentBorrow.bookTitle }}</el-descriptions-item>
        <el-descriptions-item label="借阅日期">{{ currentBorrow.borrowDate }}</el-descriptions-item>
        <el-descriptions-item label="应还日期">{{ currentBorrow.dueDate }}</el-descriptions-item>
        <el-descriptions-item label="归还日期">{{ currentBorrow.returnDate || '未归还' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentBorrow.status === '借出中' ? 'warning' : currentBorrow.status === '已归还' ? 'success' : 'danger'" size="small">
            {{ currentBorrow.status }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View } from '@element-plus/icons-vue'
import { getMyBorrows, returnBook } from '@/api/borrowRecord'

const loading = ref(false)
const detailVisible = ref(false)
const currentBorrow = ref(null)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({ pageNum: 1, pageSize: 10, status: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyBorrows(queryForm)
    tableData.value = res.data.list || res.data || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.status = ''; handleSearch() }

const handleDetail = (row) => {
  currentBorrow.value = row
  detailVisible.value = true
}

const handleReturn = (row) => {
  ElMessageBox.confirm(`确定要归还"${row.bookTitle}"吗？`, '提示', { type: 'info' }).then(async () => {
    await returnBook(row.id)
    ElMessage.success('归还成功')
    fetchData()
  }).catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.action-buttons { display: flex; gap: 4px; justify-content: center; align-items: center; }
</style>
