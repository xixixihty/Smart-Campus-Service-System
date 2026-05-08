<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><EditPen /></el-icon> 请假申请</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新建申请</el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableData" stripe border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="leaveType" label="请假类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.leaveType === 'SICK' ? 'danger' : row.leaveType === 'PERSONAL' ? 'warning' : ''" size="small">
              {{ row.leaveType === 'SICK' ? '病假' : row.leaveType === 'PERSONAL' ? '事假' : '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="reason" label="请假原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'" size="small">
              {{ row.status === 'PENDING' ? '待审批' : row.status === 'APPROVED' ? '已通过' : '已拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="handleCancel(row)">
              取消
            </el-button>
            <el-button type="primary" link size="small" @click="showDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建请假申请" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择类型" style="width: 100%">
            <el-option label="病假" value="SICK" />
            <el-option label="事假" value="PERSONAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请输入请假原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="请假详情" width="500px">
      <el-descriptions :column="1" border v-if="currentLeave">
        <el-descriptions-item label="请假类型">
          <el-tag :type="currentLeave.leaveType === 'SICK' ? 'danger' : ''" size="small">
            {{ currentLeave.leaveType === 'SICK' ? '病假' : currentLeave.leaveType === 'PERSONAL' ? '事假' : '其他' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ currentLeave.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ currentLeave.endDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLeave.status === 'APPROVED' ? 'success' : currentLeave.status === 'REJECTED' ? 'danger' : 'warning'" size="small">
            {{ currentLeave.status === 'PENDING' ? '待审批' : currentLeave.status === 'APPROVED' ? '已通过' : '已拒绝' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentLeave.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请假原因">{{ currentLeave.reason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyLeaveRequests, createLeaveRequest, cancelLeaveRequest } from '@/api/leaveRequest'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const currentLeave = ref(null)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const queryForm = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({ leaveType: '', startDate: '', endDate: '', reason: '' })
const rules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyLeaveRequests(queryForm)
    tableData.value = res.data.list || res.data || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, { leaveType: '', startDate: '', endDate: '', reason: '' })
  dialogVisible.value = true
}

const showDetail = (row) => {
  currentLeave.value = row
  detailVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await createLeaveRequest(form)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    fetchData()
  } finally { submitLoading.value = false }
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消该请假申请吗？', '提示', { type: 'warning' }).then(async () => {
    await cancelLeaveRequest(row.id)
    ElMessage.success('已取消')
    fetchData()
  }).catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
