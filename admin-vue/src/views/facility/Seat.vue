<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Place /></el-icon> 座位管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增座位</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="房间编号">
          <el-input v-model="queryForm.roomId" placeholder="请输入房间编号" clearable class="search-input" />
        </el-form-item>
        <el-form-item label="座位编号">
          <el-input v-model="queryForm.seatNumber" placeholder="请输入座位编号" clearable class="search-input" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable class="search-input">
            <el-option label="空闲" value="空闲" />
            <el-option label="使用中" value="使用中" />
            <el-option label="暂离" value="暂离" />
            <el-option label="维修中" value="维修中" />
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
        <el-table-column prop="roomId" label="房间编号" width="120"  align="center" />
        <el-table-column prop="seatNumber" label="座位编号" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '空闲' ? 'success' : row.status === '使用中' ? 'warning' : row.status === '暂离' ? '' : 'info'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)"><el-icon><Delete /></el-icon>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="房间编号" prop="roomId">
              <el-input v-model="form.roomId" placeholder="请输入房间编号" :disabled="!isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="座位编号" prop="seatNumber">
              <el-input v-model="form.seatNumber" placeholder="请输入座位编号" :disabled="!isEdit" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-tag v-if="!isEdit" :type="getStatusType(form.status)" size="large" style="width: 100%; justify-content: center; height: 40px; font-size: 14px">
                {{ form.status }}
              </el-tag>
              <el-select v-else v-model="form.status" style="width: 100%">
                <el-option label="空闲" value="空闲" />
                <el-option label="使用中" value="使用中" />
                <el-option label="暂离" value="暂离" />
                <el-option label="维修中" value="维修中" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer v-if="isEdit">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSeatList, createSeat, updateSeat, deleteSeat, getSeatDetail } from '@/api/seat'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增座位')
const isEdit = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const queryForm = reactive({ pageNum: 1, pageSize: 10, roomId: '', seatNumber: '', status: '' })
const form = reactive({ id: null, roomId: '', seatNumber: '', status: '', createTime: '', updateTime: '' })
const rules = {
  seatNumber: [{ required: true, message: '请输入座位编号', trigger: 'blur' }],
  roomId: [{ required: true, message: '请输入房间编号', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSeatList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.roomId = ''; queryForm.seatNumber = ''; queryForm.status = ''; handleSearch() }
const getStatusType = (status) => {
  const typeMap = { '空闲': 'success', '使用中': 'warning', '暂离': 'info', '维修中': 'danger' }
  return typeMap[status] || 'info'
}
const handleAdd = () => {
  isEdit.value = false; dialogTitle.value = '新增座位'
  Object.assign(form, { id: null, roomId: '', seatNumber: '', status: '空闲', createTime: '', updateTime: '' })
  dialogVisible.value = true
}

const handleView = async (row) => {
  isEdit.value = false; dialogTitle.value = '查看座位详情'
  try {
    const res = await getSeatDetail(row.id)
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const handleEdit = async (row) => {
  isEdit.value = true; dialogTitle.value = '编辑座位'
  try {
    const res = await getSeatDetail(row.id)
    Object.assign(form, res.data)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该座位吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteSeat(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateSeat(form) : await createSeat(form)
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
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.search-form { display: flex; flex-wrap: wrap; gap: 0; }
.search-form :deep(.el-form-item) { margin-bottom: 16px; }
.search-input { width: 200px; }
</style>
