<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Calendar /></el-icon> 学期管理</h2>
      <div class="header-actions">
        <el-button @click="handleViewCurrent"><el-icon><View /></el-icon>查看当前学期</el-button>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增学期</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="学期名称">
          <el-input v-model="queryForm.name" placeholder="请输入学期名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 200px">
            <el-option label="未开始" value="未开始" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已结束" value="已结束" />
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
        <el-table-column prop="name" label="学期名称" min-width="180"  align="center" />
        <el-table-column prop="startDate" label="开始日期" width="120" align="center" />
        <el-table-column prop="endDate" label="结束日期" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <StatusBadge :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="isCurrent" label="当前学期" width="100" align="center">
          <template #default="{ row }">
            <StatusBadge :status="row.isCurrent ? '已同步' : '未同步'" mode="syncStatus" />
          </template>
        </el-table-column>
        <el-table-column label="设为当前" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isCurrent" type="success" effect="dark" size="small">当前学期</el-tag>
            <el-button v-else type="warning" size="small" plain @click="handleSetCurrent(row)">
              <el-icon><Star /></el-icon>设为当前
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-descriptions v-if="isView" :column="2" border size="default">
        <el-descriptions-item label="学期ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="学期名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detailData.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detailData.endDate }}</el-descriptions-item>
        <el-descriptions-item label="当前学期">
          <StatusBadge :status="detailData.isCurrent ? '已同步' : '未同步'" mode="syncStatus" />
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusBadge :status="detailData.status" />
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学期名称" prop="name">
          <el-input v-model="form.name" placeholder="如：2025-2026学年第一学期" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" placeholder="请选择开始日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" placeholder="请选择结束日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="未开始" value="未开始" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已结束" value="已结束" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Plus, Search, Refresh, View, Edit, Delete, Check, Star } from '@element-plus/icons-vue'
import { getSemesterList, getSemesterDetail, getCurrentSemester, setCurrentSemester, createSemester, updateSemester, deleteSemester } from '@/api/semester'
import StatusBadge from '@/components/StatusBadge.vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增学期')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const queryForm = reactive({ pageNum: 1, pageSize: 10, name: '', status: '' })
const form = reactive({ id: null, name: '', startDate: '', endDate: '', status: '未开始' })
const detailData = reactive({ id: null, name: '', startDate: '', endDate: '', isCurrent: false, status: '', createTime: '', updateTime: '' })
const rules = {
  name: [{ required: true, message: '请输入学期名称', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSemesterList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.name = ''; queryForm.status = ''; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增学期'
  Object.assign(form, { id: null, name: '', startDate: '', endDate: '', status: '未开始' })
  dialogVisible.value = true
}

const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '查看学期详情'
  dialogVisible.value = true
  try {
    const detail = await getSemesterDetail(row.id)
    const data = detail.data
    Object.assign(detailData, {
      id: data.id,
      name: data.name || '',
      startDate: data.startDate || '',
      endDate: data.endDate || '',
      isCurrent: data.isCurrent || false,
      status: data.status || '',
      createTime: data.createTime || '',
      updateTime: data.updateTime || ''
    })
  } catch {
    ElMessage.error('获取学期详情失败')
    dialogVisible.value = false
  }
}

const handleEdit = (row) => {
  isEdit.value = true; isView.value = false; dialogTitle.value = '编辑学期'
  Object.assign(form, { ...row }); dialogVisible.value = true
}

const handleSetCurrent = (row) => {
  ElMessageBox.confirm(`确定要将「${row.name}」设为当前学期吗？设置后原当前学期将被取消。`, '提示', { type: 'warning' }).then(async () => {
    await setCurrentSemester(row.id)
    ElMessage.success('设置成功')
    fetchData()
  }).catch(() => {})
}

const handleViewCurrent = async () => {
  try {
    const res = await getCurrentSemester()
    const data = res.data
    if (!data) { ElMessage.warning('暂无当前学期'); return }
    isEdit.value = false; isView.value = true; dialogTitle.value = '当前学期详情'
    Object.assign(detailData, {
      id: data.id,
      name: data.name || '',
      startDate: data.startDate || '',
      endDate: data.endDate || '',
      isCurrent: data.isCurrent || false,
      status: data.status || '',
      createTime: data.createTime || '',
      updateTime: data.updateTime || ''
    })
    dialogVisible.value = true
  } catch {
    ElMessage.error('获取当前学期失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该学期吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteSemester(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateSemester(form) : await createSemester(form)
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.header-actions { display: flex; gap: 8px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
