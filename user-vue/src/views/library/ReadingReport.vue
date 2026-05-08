<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Edit /></el-icon> 阅读报告</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>写报告</el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableData" stripe border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="bookName" label="书名" min-width="180" />
        <el-table-column prop="title" label="报告标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="rating" label="评分" width="100" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="80" align="center">
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

    <el-dialog v-model="dialogVisible" title="写阅读报告" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联图书" prop="bookId">
          <el-select v-model="form.bookId" placeholder="请选择图书" style="width: 100%" filterable>
            <el-option v-for="b in bookOptions" :key="b.id" :label="b.bookName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入报告标题" />
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="form.rating" show-text />
        </el-form-item>
        <el-form-item label="报告内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入阅读心得与报告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="报告详情" width="550px">
      <template v-if="currentReport">
        <h3 style="margin-bottom: 12px">{{ currentReport.title }}</h3>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="关联图书">{{ currentReport.bookName }}</el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate v-model="currentReport.rating" disabled show-score text-color="#ff9900" />
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentReport.createTime }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 16px; line-height: 1.8; white-space: pre-wrap">{{ currentReport.content }}</div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyReadingReports, createReadingReport } from '@/api/readingReport'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const currentReport = ref(null)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const bookOptions = ref([])

const queryForm = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({ bookId: '', title: '', rating: 4, content: '' })
const rules = {
  bookId: [{ required: true, message: '请选择图书', trigger: 'change' }],
  title: [{ required: true, message: '请输入报告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入报告内容', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyReadingReports(queryForm)
    tableData.value = res.data.list || res.data || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const loadBooks = async () => {
  try {
    const res = await request.get('/books/admin', { params: { pageNum: 1, pageSize: 500 } })
    bookOptions.value = res.data.list || []
  } catch {}
}

const handleAdd = () => {
  Object.assign(form, { bookId: '', title: '', rating: 4, content: '' })
  dialogVisible.value = true
}

const showDetail = (row) => {
  currentReport.value = row
  detailVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await createReadingReport(form)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    fetchData()
  } finally { submitLoading.value = false }
}

onMounted(() => { loadBooks(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
