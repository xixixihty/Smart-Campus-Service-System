<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Chair /></el-icon> 座位管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增座位</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="区域">
          <el-input v-model="queryForm.area" placeholder="请输入区域" clearable />
        </el-form-item>
        <el-form-item label="座位编号">
          <el-input v-model="queryForm.seatNumber" placeholder="请输入座位编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 120px">
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
        <el-table-column prop="seatNumber" label="座位编号" width="120" />
        <el-table-column prop="area" label="区域" width="120" />
        <el-table-column prop="floor" label="楼层" width="80" align="center" />
        <el-table-column prop="seatType" label="座位类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.seatType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hasPower" label="电源" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.hasPower" color="#67C23A"><Check /></el-icon>
            <el-icon v-else color="#C0C4CC"><Close /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '空闲' ? 'success' : row.status === '使用中' ? 'warning' : row.status === '暂离' ? '' : 'info'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
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
            <el-form-item label="座位编号" prop="seatNumber">
              <el-input v-model="form.seatNumber" placeholder="请输入座位编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区域" prop="area">
              <el-input v-model="form.area" placeholder="如：A区、B区" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="楼层" prop="floor">
              <el-input-number v-model="form.floor" :min="1" :max="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="座位类型" prop="seatType">
              <el-select v-model="form.seatType" style="width: 100%">
                <el-option label="普通座" value="普通座" />
                <el-option label="电脑座" value="电脑座" />
                <el-option label="独立间" value="独立间" />
                <el-option label="沙发座" value="沙发座" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="是否有电源" prop="hasPower">
              <el-switch v-model="form.hasPower" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="空闲" value="空闲" />
                <el-option label="使用中" value="使用中" />
                <el-option label="暂离" value="暂离" />
                <el-option label="维修中" value="维修中" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSeatList, createSeat, updateSeat, deleteSeat } from '@/api/seat'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增座位')
const isEdit = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const queryForm = reactive({ pageNum: 1, pageSize: 10, area: '', seatNumber: '', status: '' })
const form = reactive({ id: null, seatNumber: '', area: '', floor: 1, seatType: '普通座', hasPower: true, status: '空闲' })
const rules = {
  seatNumber: [{ required: true, message: '请输入座位编号', trigger: 'blur' }],
  area: [{ required: true, message: '请输入区域', trigger: 'blur' }]
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
const handleReset = () => { queryForm.area = ''; queryForm.seatNumber = ''; queryForm.status = ''; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; dialogTitle.value = '新增座位'
  Object.assign(form, { id: null, seatNumber: '', area: '', floor: 1, seatType: '普通座', hasPower: true, status: '空闲' })
  dialogVisible.value = true
}
const handleEdit = (row) => { isEdit.value = true; dialogTitle.value = '编辑座位'; Object.assign(form, { ...row }); dialogVisible.value = true }
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
</style>
