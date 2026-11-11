<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Edit /></el-icon> 阅读报告</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>生成报告</el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableData" stripe border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="semester" label="学期" min-width="140" />
        <el-table-column prop="totalBorrow" label="总借阅量" width="100" align="center" />
        <el-table-column prop="favCategory" label="偏好分类" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="生成时间" width="170" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="生成阅读报告" width="450px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学期" prop="semester">
          <el-input v-model="form.semester" placeholder="请输入学期，如 2025-2026-1" />
        </el-form-item>
        <div class="form-tip">系统将自动分析您的借阅行为并生成阅读报告。</div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">生成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="报告详情" width="550px">
      <div v-loading="detailLoading">
        <template v-if="currentReport">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="学期">{{ currentReport.semester }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ currentReport.userName }}</el-descriptions-item>
            <el-descriptions-item label="总借阅量">{{ currentReport.totalBorrow }} 本</el-descriptions-item>
            <el-descriptions-item label="偏好分类">{{ currentReport.favCategory || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生成时间" :span="2">{{ currentReport.createTime }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="currentReport.analysisText" class="analysis-section">
            <h4>AI 阅读分析</h4>
            <div class="analysis-content" v-html="renderMarkdown(currentReport.analysisText)"></div>
          </div>
          <el-empty v-else description="暂无分析内容" />
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { getMyReadingReports, createReadingReport, getReadingReportDetail } from '@/api/readingReport'

marked.setOptions({ breaks: true, gfm: true })

const renderMarkdown = (text) => (text ? marked.parse(text) : '')

const loading = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const currentReport = ref(null)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const queryForm = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({ semester: '' })
const rules = {
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyReadingReports(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleAdd = () => {
  form.semester = ''
  dialogVisible.value = true
}

const showDetail = async (id) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getReadingReportDetail(id)
    currentReport.value = res.data
  } catch {
    currentReport.value = null
  } finally { detailLoading.value = false }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await createReadingReport({ semester: form.semester })
    ElMessage.success('阅读报告生成成功')
    dialogVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('生成失败')
  } finally { submitLoading.value = false }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.form-tip { color: #909399; font-size: 13px; margin-top: -8px; }
.analysis-section { margin-top: 20px; }
.analysis-section h4 { font-size: 15px; color: #303133; margin-bottom: 8px; }
.analysis-content { line-height: 1.8; color: #606266; }
.analysis-content :deep(p) { margin: 6px 0; }
.analysis-content :deep(h2) { font-size: 16px; margin: 12px 0 8px; color: #303133; }
.analysis-content :deep(h3) { font-size: 15px; margin: 10px 0 6px; color: #303133; }
.analysis-content :deep(ul), .analysis-content :deep(ol) { padding-left: 20px; margin: 6px 0; }
.analysis-content :deep(li) { margin: 4px 0; }
.analysis-content :deep(table) { border-collapse: collapse; width: 100%; margin: 8px 0; }
.analysis-content :deep(th), .analysis-content :deep(td) { border: 1px solid #dcdfe6; padding: 6px 10px; text-align: left; }
.analysis-content :deep(th) { background: #f5f7fa; }
.analysis-content :deep(strong) { font-weight: 600; }
.analysis-content :deep(blockquote) { border-left: 3px solid #409EFF; padding-left: 12px; margin: 8px 0; color: #909399; }
</style>
