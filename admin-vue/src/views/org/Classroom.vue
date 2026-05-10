<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><HomeFilled /></el-icon> 教室管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增教室</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="教学楼">
          <el-input v-model="queryForm.building" placeholder="请输入教学楼" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="教室名称">
          <el-input v-model="queryForm.roomNumber" placeholder="请输入教室名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="教室类型">
          <el-select v-model="queryForm.type" placeholder="请选择类型" clearable style="width: 150px">
            <el-option label="普通教室" value="普通教室" />
            <el-option label="多媒体教室" value="多媒体教室" />
            <el-option label="实验室" value="实验室" />
            <el-option label="阶梯教室" value="阶梯教室" />
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
        <el-table-column prop="roomNumber" label="教室门牌号" min-width="150" align="center" />
        <el-table-column prop="building" label="教学楼" width="120" align="center" />
        <el-table-column prop="type" label="教室类型" width="120" align="center" />
        <el-table-column prop="capacity" label="容量" width="80" align="center" />

        <el-table-column prop="status" label="状态" width="100" align="center">
        
          <template #default="{ row }">
            <el-tag :type="row.status === '正常' || row.status === '可用' ? 'success' : row.status === '停用' ? 'danger' : 'warning'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <!-- 查看详情：描述列表 -->
      <el-descriptions v-if="isView" :column="2" border size="default">
        <el-descriptions-item label="教室ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="教室门牌号">{{ detailData.roomNumber }}</el-descriptions-item>
        <el-descriptions-item label="教学楼">{{ detailData.building }}</el-descriptions-item>
        <el-descriptions-item label="教室类型">{{ detailData.type }}</el-descriptions-item>
        <el-descriptions-item label="座位容量">{{ detailData.capacity }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === '正常' || detailData.status === '可用' ? 'success' : detailData.status === '停用' ? 'danger' : 'warning'" size="small">{{ detailData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <!-- 新增/编辑：表单 -->
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="教学楼" prop="building">
          <el-input v-model="form.building" placeholder="请输入教学楼" />
        </el-form-item>
        <el-form-item label="教室门牌号" prop="roomNumber">
          <el-input v-model="form.roomNumber" placeholder="请输入教室门牌号" />
        </el-form-item>
        <el-form-item label="教室类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="普通教室" value="普通教室" />
            <el-option label="多媒体教室" value="多媒体教室" />
            <el-option label="实验室" value="实验室" />
            <el-option label="阶梯教室" value="阶梯教室" />
          </el-select>
        </el-form-item>
        <el-form-item label="座位容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="500" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="正常" value="正常" />
            <el-option label="停用" value="停用" />
            <el-option label="维修" value="维修" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClassroomList, getClassroomDetail, createClassroom, updateClassroom, deleteClassroom } from '@/api/classroom'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增教室')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const queryForm = reactive({ pageNum: 1, pageSize: 10, building: '', roomNumber: '', type: '' })
const form = reactive({ id: null, building: '', roomNumber: '', type: '普通教室', capacity: 60, status: '正常' })
const detailData = reactive({ id: null, building: '', roomNumber: '', type: '', capacity: null, status: '', createTime: '', updateTime: '' })
const rules = {
  building: [{ required: true, message: '请输入教学楼', trigger: 'blur' }],
  roomNumber: [{ required: true, message: '请输入教室门牌号', trigger: 'blur' }],
  type: [{ required: true, message: '请选择教室类型', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getClassroomList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.building = ''; queryForm.roomNumber = ''; queryForm.type = ''; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增教室'
  Object.assign(form, { id: null, building: '', roomNumber: '', type: '普通教室', capacity: 60, status: '正常' })
  dialogVisible.value = true
}
const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '查看教室详情'
  const detail = await getClassroomDetail(row.id)
  const data = detail.data
  Object.assign(detailData, {
    id: data.id,
    building: data.building || '',
    roomNumber: data.roomNumber || '',
    type: data.type || '',
    capacity: data.capacity,
    status: data.status || '',
    createTime: data.createTime || '',
    updateTime: data.updateTime || ''
  })
  dialogVisible.value = true
}
const handleEdit = async (row) => {
  isEdit.value = true; isView.value = false; dialogTitle.value = '编辑教室'
  const detail = await getClassroomDetail(row.id)
  const data = detail.data
  await nextTick()
  Object.assign(form, {
    id: data.id,
    building: data.building || '',
    roomNumber: data.roomNumber || '',
    type: data.type || '普通教室',
    capacity: data.capacity || 60,
    status: data.status || '正常'
  })
  dialogVisible.value = true
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该教室吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteClassroom(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateClassroom(form) : await createClassroom(form)
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
.search-form { margin-bottom: 0; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
