<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Search /></el-icon> 图书检索</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="书名">
          <el-input v-model="queryForm.bookName" placeholder="请输入书名" clearable />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="queryForm.author" placeholder="请输入作者" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="请选择分类" clearable>
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.categoryName" :value="c.id" />
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
        <el-table-column prop="bookName" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="publisher" label="出版社" width="130" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column label="可借数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.availableQuantity > 0 ? 'success' : 'danger'" size="small">
              {{ row.availableQuantity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :disabled="row.availableQuantity <= 0" @click="handleBorrow(row)">
              借阅
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="borrowVisible" title="确认借阅" width="450px">
      <el-descriptions :column="1" border v-if="currentBook">
        <el-descriptions-item label="书名">{{ currentBook.bookName }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ currentBook.author }}</el-descriptions-item>
        <el-descriptions-item label="ISBN">{{ currentBook.isbn }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top: 16px; color: #909399; font-size: 13px">
        <el-icon><InfoFilled /></el-icon> 借阅期限为30天，请按时归还。
      </div>
      <template #footer>
        <el-button @click="borrowVisible = false">取消</el-button>
        <el-button type="primary" :loading="borrowLoading" @click="handleBorrowSubmit">确认借阅</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { borrowBook } from '@/api/borrowRecord'
import request from '@/utils/request'

const loading = ref(false)
const borrowLoading = ref(false)
const borrowVisible = ref(false)
const currentBook = ref(null)
const tableData = ref([])
const total = ref(0)
const categoryOptions = ref([])

const queryForm = reactive({ pageNum: 1, pageSize: 10, bookName: '', author: '', categoryId: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/books/admin', { params: queryForm })
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const loadCategories = async () => {
  try {
    const res = await request.get('/book-categories/admin', { params: { pageNum: 1, pageSize: 100 } })
    categoryOptions.value = res.data.list || []
  } catch {}
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.bookName = ''; queryForm.author = ''; queryForm.categoryId = ''; handleSearch() }

const handleBorrow = (row) => {
  currentBook.value = row
  borrowVisible.value = true
}

const handleBorrowSubmit = async () => {
  borrowLoading.value = true
  try {
    await borrowBook({ bookId: currentBook.value.id })
    ElMessage.success('借阅成功')
    borrowVisible.value = false
    fetchData()
  } finally { borrowLoading.value = false }
}

onMounted(() => { loadCategories(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
