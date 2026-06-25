<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Bell /></el-icon> 通知管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>发布通知</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="通知标题">
          <el-input v-model="queryForm.title" placeholder="请输入通知标题" clearable class="search-input" />
        </el-form-item>
        <el-form-item label="通知类型">
          <el-select v-model="queryForm.noticeType" placeholder="请选择类型" clearable class="search-input">
            <el-option label="系统通知" value="系统通知" />
            <el-option label="教学通知" value="教学通知" />
            <el-option label="活动通知" value="活动通知" />
            <el-option label="紧急通知" value="紧急通知" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable class="search-input">
            <el-option label="发布" value="发布" />
            <el-option label="撤回" value="撤回" />
          </el-select>
        </el-form-item>
        <el-form-item>
          
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">


      <el-table :data="tableData" v-loading="loading" stripe border max-height="calc(100vh - 280px)" scrollbar-always-on>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="发布人姓名" width="100" />
        <el-table-column prop="publishTime" label="发布时间" width="170" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <StatusBadge :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '草稿'" type="primary" link @click="handlePublish(row)"><el-icon><Promotion /></el-icon>发布</el-button>
            <el-button v-if="row.status === '发布'" type="warning" link @click="handleWithdraw(row)"><el-icon><TurnOff /></el-icon>撤回</el-button>
            <el-button type="info" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)"><el-icon><Delete /></el-icon>删除</el-button>
          </template>
        </el-table-column>
        <el-table-column width="12" class-name="scroll-hint-column" fixed="right" />
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>
    <!-- 通知详情/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" destroy-on-close>
      <!-- 详情模式 -->
      <div v-if="isView" class="detail-container">
        <el-descriptions :column="2" border class="detail-descriptions">
          <el-descriptions-item label="通知标题" :span="2">
            <span class="detail-title">{{ form.title }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <StatusBadge :status="form.status" />
          </el-descriptions-item>
          <el-descriptions-item label="发布人">{{ form.publisherName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ form.publishTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="通知类型">{{ form.type || form.noticeType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布范围">{{ form.scope || '-' }}</el-descriptions-item>
          <el-descriptions-item label="通知内容" :span="2">
            <div class="detail-content">{{ form.content }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ form.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ form.updateTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 编辑/新增模式 -->
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知类型" prop="noticeType">
          <el-select v-model="form.noticeType" style="width: 100%">
            <el-option label="系统通知" value="系统通知" />
            <el-option label="教学通知" value="教学通知" />
            <el-option label="活动通知" value="活动通知" />
            <el-option label="紧急通知" value="紧急通知" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入通知内容" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoticeList, createNotice, updateNotice, withdrawNotice, deleteNotice, getNoticeDetail } from '@/api/notice'
import StatusBadge from '@/components/StatusBadge.vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('发布通知')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const queryForm = reactive({ pageNum: 1, pageSize: 10, title: '', noticeType: '', status: '' })
const form = reactive({ id: null, title: '', noticeType: '系统通知', content: '', publisherName: '', publishTime: '', type: '', scope: '', status: '', createTime: '', updateTime: '' })
const rules = {
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getNoticeList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.title = ''; queryForm.noticeType = ''; queryForm.status = ''; handleSearch() }
const getTypeTagColor = (type) => {
  const colorMap = { '系统通知': 'info', '教学通知': 'success', '活动通知': 'warning', '紧急通知': 'danger' }
  return colorMap[type] || 'info'
}
const handleAdd = () => {
  isEdit.value = false; dialogTitle.value = '发布通知'
  Object.assign(form, { id: null, title: '', noticeType: '系统通知', content: '' })
  dialogVisible.value = true
}

const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '通知详情'
  try {
    const detail = await getNoticeDetail(row.id)
    const data = detail.data
    Object.assign(form, {
      id: data.id,
      title: data.title,
      content: data.content,
      publisherName: data.publisherName,
      publishTime: data.publishTime,
      type: data.type,
      scope: data.scope,
      status: data.status,
      createTime: data.createTime,
      updateTime: data.updateTime
    })
    dialogVisible.value = true
  } catch (err) {
    console.error('获取通知详情失败:', err)
    ElMessage.error('获取通知详情失败')
    dialogVisible.value = false
  }
}


const handleEdit = async (row) => {
  isEdit.value = true; isView.value = false; dialogTitle.value = '编辑通知'
  try {
    const detail = await getNoticeDetail(row.id)
    const data = detail.data
    Object.assign(form, {
      id: data.id,
      title: data.title,
      content: data.content,
      publisherName: data.publisherName,
      publishTime: data.publishTime,
      type: data.type,
      scope: data.scope,
      status: data.status,
      createTime: data.createTime,
      updateTime: data.updateTime
    })
    dialogVisible.value = true
  } catch (err) {
    console.error('获取通知详情失败:', err)
    ElMessage.error('获取通知详情失败')
  }
}
const handleWithdraw = (row) => {
  ElMessageBox.confirm('确定要撤回该通知吗？', '提示', { type: 'warning' }).then(async () => {
    await withdrawNotice(row.id); ElMessage.success('撤回成功'); fetchData()
  }).catch(() => {})
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该通知吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteNotice(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateNotice(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createNotice(form)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.search-form { display: flex; flex-wrap: wrap; gap: 0; }
.search-form :deep(.el-form-item) { margin-bottom: 16px; }
.search-input { width: 200px; }
.detail-container { padding: 10px 0; }
.detail-descriptions { margin-bottom: 10px; }
.detail-title { font-size: 16px; font-weight: 600; color: #303133; }
.detail-content {
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
  line-height: 1.6;
  color: #606266;
}
</style>
