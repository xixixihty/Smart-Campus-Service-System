<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Document /></el-icon> 借阅记录</h2>
      <div class="header-actions">
        <el-button type="success" @click="handleStatistics"><el-icon><DataAnalysis /></el-icon>借阅统计</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="学生">
          <el-select v-model="queryForm.studentId" placeholder="请选择学生" clearable filterable>
            <el-option v-for="s in studentOptions" :key="s.id" :label="s.name + ' (' + s.studentNo + ')'" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="图书">
          <el-select v-model="queryForm.bookId" placeholder="请选择图书" clearable filterable>
            <el-option v-for="b in bookOptions" :key="b.id" :label="b.bookName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 120px">
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

    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="studentName" label="借阅人" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="bookName" label="图书名称" min-width="180" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="borrowDate" label="借阅日期" width="120" />
        <el-table-column prop="dueDate" label="应还日期" width="120" />
        <el-table-column prop="returnDate" label="归还日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '借出中' ? 'warning' : row.status === '已归还' ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="statsVisible" title="借阅统计" width="700px">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-statistic title="总借阅量" :value="stats.totalBorrows" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="当前借阅中" :value="stats.currentBorrows" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="逾期未还" :value="stats.overdueCount" />
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBorrowRecordList, getBorrowStatistics } from '@/api/borrowRecord'
import { getStudentList } from '@/api/student'
import { getBookList } from '@/api/book'

const loading = ref(false)
const statsVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const studentOptions = ref([])
const bookOptions = ref([])
const stats = reactive({ totalBorrows: 0, currentBorrows: 0, overdueCount: 0 })

const queryForm = reactive({ pageNum: 1, pageSize: 10, studentId: '', bookId: '', status: '' })

const loadOptions = async () => {
  const [studentRes, bookRes] = await Promise.all([
    getStudentList({ pageNum: 1, pageSize: 500 }),
    getBookList({ pageNum: 1, pageSize: 500 })
  ])
  studentOptions.value = studentRes.data.list || []
  bookOptions.value = bookRes.data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getBorrowRecordList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.studentId = ''; queryForm.bookId = ''; queryForm.status = ''; handleSearch() }

const handleStatistics = async () => {
  try {
    const res = await getBorrowStatistics()
    Object.assign(stats, res.data || {})
    statsVisible.value = true
  } catch { /* ignore */ }
}

onMounted(async () => { await loadOptions(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.header-actions { display: flex; gap: 8px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
