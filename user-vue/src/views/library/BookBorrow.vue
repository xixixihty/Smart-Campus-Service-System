<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Search /></el-icon> 图书检索</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="书名">
          <el-input v-model="queryForm.title" placeholder="请输入书名" clearable />
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
        <el-table-column prop="title" label="书名" min-width="180" align="center" />
        <el-table-column label="封面" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              :preview-src-list="[row.coverImage]"
              fit="cover"
              style="width: 50px; height: 70px; border-radius: 4px"
            >
              <template #error>
                <div class="image-error">
                  <el-icon :size="20"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-else class="no-cover">
              <el-icon :size="20"><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120" align="center" />
        <el-table-column prop="categoryName" label="分类" width="100" align="center" />
        <el-table-column prop="publisher" label="出版社" width="130" align="center" />
        <el-table-column prop="isbn" label="ISBN" width="140" align="center" />
        <el-table-column prop="availableCopies" label="可借数量"  width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.availableCopies > 0 ? 'success' : 'danger'" size="small">
              {{ row.availableCopies }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="info" size="small" @click="handlePreview(row)">
                <el-icon><View /></el-icon>详情
              </el-button>
              <el-tag v-if="borrowedBookIds.has(row.id)" type="warning" size="small">已借</el-tag>
              <el-button v-else type="primary" size="small" :disabled="row.availableCopies <= 0" @click="handleBorrow(row)">
                <el-icon><Reading /></el-icon>借阅
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

    <el-dialog v-model="previewVisible" title="图书详情" width="600px">
      <el-descriptions :column="2" border v-if="previewBook">
        <el-descriptions-item label="封面" :span="2" align="center">
          <el-image
            v-if="previewBook.coverImage"
            :src="previewBook.coverImage"
            :preview-src-list="[previewBook.coverImage]"
            fit="cover"
            style="width: 120px; height: 160px; border-radius: 4px"
          >
            <template #error>
              <div class="image-error">
                <el-icon :size="30"><Picture /></el-icon>
                <div style="margin-top: 8px; font-size: 12px">暂无封面</div>
              </div>
            </template>
          </el-image>
          <div v-else class="no-cover">
            <el-icon :size="30"><Picture /></el-icon>
            <div style="margin-top: 8px; font-size: 12px">暂无封面</div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="ISBN">{{ previewBook.isbn || '无' }}</el-descriptions-item>
        <el-descriptions-item label="书名">{{ previewBook.title }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ previewBook.author }}</el-descriptions-item>
        <el-descriptions-item label="出版社">{{ previewBook.publisher || '无' }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ previewBook.categoryName || '无' }}</el-descriptions-item>
        <el-descriptions-item label="可借数量">
          <el-tag :type="previewBook.availableCopies > 0 ? 'success' : 'danger'" size="small">
            {{ previewBook.availableCopies }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="previewBook.status === '在库' ? 'success' : previewBook.status === '借出' ? 'warning' : previewBook.status === '维修' ? 'info' : 'danger'" size="small">
            {{ previewBook.status || '无' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ previewBook.description || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="previewBook?.availableCopies <= 0" @click="previewVisible = false; handleBorrow(previewBook)">
          <el-icon><Reading /></el-icon>立即借阅
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="borrowVisible" title="确认借阅" width="450px">
      <el-descriptions :column="1" border v-if="currentBook">
        <el-descriptions-item label="书名">{{ currentBook.title }}</el-descriptions-item>
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
import { View, Reading } from '@element-plus/icons-vue'
import { borrowBook, getMyBorrows } from '@/api/borrowRecord'
import request from '@/utils/request'

const loading = ref(false)
const borrowLoading = ref(false)
const borrowVisible = ref(false)
const previewVisible = ref(false)
const currentBook = ref(null)
const previewBook = ref(null)
const tableData = ref([])
const total = ref(0)
const categoryOptions = ref([])
const borrowedBookIds = ref(new Set())

const queryForm = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: '' })

const fetchBorrowedBooks = async () => {
  try {
    const res = await getMyBorrows({ status: '借阅中', pageNum: 1, pageSize: 100 })
    const list = res.data.list || []
    borrowedBookIds.value = new Set(list.map(item => item.bookId).filter(Boolean))
  } catch {}
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/books/user', { params: queryForm })
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

const handlePreview = (row) => {
  previewBook.value = row
  previewVisible.value = true
}

const handleBorrowSubmit = async () => {
  borrowLoading.value = true
  try {
    await borrowBook({ bookId: currentBook.value.id })
    ElMessage.success('借阅成功')
    borrowVisible.value = false
    // 借阅成功后刷新已借列表和图书列表
    fetchBorrowedBooks()
    fetchData()
  } finally { borrowLoading.value = false }
}

onMounted(() => { loadCategories(); fetchData(); fetchBorrowedBooks() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.action-buttons { display: flex; gap: 4px; justify-content: center; align-items: center; }
</style>
