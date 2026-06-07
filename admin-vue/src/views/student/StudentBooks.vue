<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Reading /></el-icon> 图书借阅</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="图书列表" name="books">
        <el-card shadow="never">
          <el-form :inline="true" :model="queryForm" class="search-form">
            <el-form-item label="关键词">
              <el-input v-model="queryForm.keyword" placeholder="书名/作者" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
              <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="table-card">
          <el-table :data="bookData" v-loading="bookLoading" stripe border max-height="calc(100vh - 340px)">
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="title" label="书名" min-width="180" align="center" />
            <el-table-column prop="author" label="作者" width="120" align="center" />
            <el-table-column prop="categoryName" label="分类" width="120" align="center" />
            <el-table-column prop="isbn" label="ISBN" width="140" align="center" />
            <el-table-column prop="stock" label="在库" width="80" align="center" />
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleBorrow(row)" :disabled="row.stock <= 0">
                  <el-icon><Plus /></el-icon>借阅
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
              :page-sizes="[10, 20, 50]" :total="bookTotal" layout="total, sizes, prev, pager, next"
              @size-change="fetchBooks" @current-change="fetchBooks" />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我的借阅" name="my">
        <el-card shadow="never" class="table-card">
          <el-table :data="myBorrowData" v-loading="myBorrowLoading" stripe border max-height="calc(100vh - 280px)" empty-text="暂无借阅记录">
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="bookTitle" label="书名" min-width="180" align="center" />
            <el-table-column prop="borrowDate" label="借阅日期" width="120" align="center" />
            <el-table-column prop="dueDate" label="应还日期" width="120" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === '借出中' || row.status === '借阅中' ? 'warning' : 'success'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === '借阅中'" type="success" link @click="handleReturn(row)">
                  <el-icon><Check /></el-icon>归还
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:current-page="myBorrowQuery.pageNum" v-model:page-size="myBorrowQuery.pageSize"
              :page-sizes="[10, 20, 50]" :total="myBorrowTotal" layout="total, sizes, prev, pager, next"
              @size-change="fetchMyBorrows" @current-change="fetchMyBorrows" />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentBookList, borrowBook, returnBook, getMyBorrowRecords } from '@/api/student'

const activeTab = ref('books')

const bookLoading = ref(false)
const bookData = ref([])
const bookTotal = ref(0)
const queryForm = reactive({ keyword: '', pageNum: 1, pageSize: 10 })

const myBorrowLoading = ref(false)
const myBorrowData = ref([])
const myBorrowTotal = ref(0)
const myBorrowQuery = reactive({ pageNum: 1, pageSize: 10 })

const fetchBooks = async () => {
  bookLoading.value = true
  try {
    const res = await getStudentBookList(queryForm)
    bookData.value = res.data?.records || res.data?.list || []
    bookTotal.value = res.data?.total || 0
  } catch (e) {
    console.error('加载图书列表失败:', e)
  } finally {
    bookLoading.value = false
  }
}

const fetchMyBorrows = async () => {
  myBorrowLoading.value = true
  try {
    const res = await getMyBorrowRecords(myBorrowQuery)
    myBorrowData.value = res.data?.records || res.data?.list || []
    myBorrowTotal.value = res.data?.total || 0
  } catch (e) {
    console.error('加载借阅记录失败:', e)
  } finally {
    myBorrowLoading.value = false
  }
}

const onTabChange = (tab) => {
  if (tab === 'my') fetchMyBorrows()
}

const handleSearch = () => { queryForm.pageNum = 1; fetchBooks() }
const handleReset = () => { Object.assign(queryForm, { keyword: '', pageNum: 1, pageSize: 10 }); fetchBooks() }

const handleBorrow = async (row) => {
  try {
    await ElMessageBox.confirm(`确定借阅《${row.title}》吗？`, '借阅确认', { type: 'info' })
    await borrowBook({ bookId: row.id })
    ElMessage.success('借阅成功')
    fetchBooks()
  } catch {}
}

const handleReturn = async (row) => {
  try {
    await ElMessageBox.confirm(`确定归还《${row.bookTitle}》吗？`, '归还确认', { type: 'info' })
    await returnBook(row.id)
    ElMessage.success('归还成功')
    fetchMyBorrows()
  } catch {}
}

onMounted(() => fetchBooks())
</script>