<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><EditPen /></el-icon> 请假管理</h2>
    </div>

    <el-tabs v-model="activeTab" class="leave-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="请假申请" name="apply">
        <el-card shadow="never">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="apply-form">
            <el-form-item label="请假类型" prop="leaveType">
              <el-select v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
                <el-option label="病假" value="SICK" />
                <el-option label="事假" value="PERSONAL" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>

            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="请假天数">
              <el-input :value="calculatedDays" disabled placeholder="自动计算" />
            </el-form-item>

            <el-form-item label="请假原因" prop="reason">
              <el-input
                v-model="form.reason"
                type="textarea"
                :rows="6"
                placeholder="请输入请假原因，详细说明请假事由"
              />
            </el-form-item>

            <el-form-item>
              <div class="form-actions">
                <el-button @click="resetForm">重置</el-button>
                <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
                  <el-icon><Check /></el-icon>提交申请
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我的请假" name="list">
        <el-card shadow="never">
          <div class="search-bar">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 150px" @change="fetchData">
              <el-option label="待审批" value="待审批" />
              <el-option label="已批准" value="已批准" />
              <el-option label="已拒绝" value="已拒绝" />
            </el-select>
          </div>

          <el-table :data="tableData" stripe border v-loading="loading">
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="leaveType" label="请假类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.leaveType === '病假' ? 'danger' : row.leaveType === '事假' ? 'warning' : 'info'" size="small">
                  {{ row.leaveType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="170" />
            <el-table-column prop="endTime" label="结束时间" width="170" />
            <el-table-column prop="reason" label="请假事由" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === '已批准' ? 'success' : row.status === '已拒绝' ? 'danger' : 'warning'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="170" />
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button type="info" size="small" @click="handleDetail(row)">
                    <el-icon><View /></el-icon>详情
                  </el-button>
                  <el-button v-if="row.status === '待审批'" type="danger" size="small" @click="handleCancel(row)">
                    <el-icon><Close /></el-icon>取消
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="queryForm.pageNum"
              v-model:page-size="queryForm.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @size-change="fetchData"
              @current-change="fetchData"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="detailVisible" title="请假详情" width="550px">
      <el-descriptions :column="1" border v-if="currentLeave">
        <el-descriptions-item label="请假类型">
          <el-tag :type="currentLeave.leaveType === '病假' ? 'danger' : currentLeave.leaveType === '事假' ? 'warning' : 'info'" size="small">
            {{ currentLeave.leaveType }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ currentLeave.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentLeave.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentLeave.endTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLeave.status === '已批准' ? 'success' : currentLeave.status === '已拒绝' ? 'danger' : 'warning'" size="small">
            {{ currentLeave.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentLeave.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请假原因">{{ currentLeave.reason }}</el-descriptions-item>
        <el-descriptions-item v-if="currentLeave.approverName" label="审批人">{{ currentLeave.approverName }}</el-descriptions-item>
        <el-descriptions-item v-if="currentLeave.approveTime" label="审批时间">{{ currentLeave.approveTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentLeave.logResult" label="审批结果">{{ currentLeave.logResult }}</el-descriptions-item>
        <el-descriptions-item v-if="currentLeave.comment" label="备注">
          <span style="color: #F56C6C">{{ currentLeave.comment }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyLeaveRequests, createLeaveRequest, cancelLeaveRequest, getLeaveDetail } from '@/api/leaveRequest'

const activeTab = ref('apply')
const loading = ref(false)
const submitLoading = ref(false)
const detailVisible = ref(false)
const currentLeave = ref(null)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const queryForm = reactive({ pageNum: 1, pageSize: 10, status: '' })
const form = reactive({ leaveType: '', startTime: '', endTime: '', reason: '' })
const rules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

const calculatedDays = computed(() => {
  if (!form.startTime || !form.endTime) return '-'
  const start = new Date(form.startTime)
  const end = new Date(form.endTime)
  const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
  return diff > 0 ? `${diff} 天` : '-'
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (!params.status) delete params.status
    const res = await getMyLeaveRequests(params)
    tableData.value = res.data.list || res.data || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleTabChange = (tab) => {
  if (tab === 'list') {
    fetchData()
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (form.startTime && form.endTime) {
    const start = new Date(form.startTime)
    const end = new Date(form.endTime)
    if (end < start) {
      ElMessage.warning('结束时间不能早于开始时间')
      return
    }
  }

  submitLoading.value = true
  try {
    await createLeaveRequest(form)
    ElMessage.success('提交成功')
    resetForm()
    activeTab.value = 'list'
    fetchData()
  } finally {
    submitLoading.value = false
  }
}


const handleDetail = async (row) => {
  try {
    const res = await getLeaveDetail(row.id)
    currentLeave.value = res.data
    detailVisible.value = true
  } catch {
    ElMessage.error('获取详情失败')
  }
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消该请假申请吗？', '提示', { type: 'warning' }).then(async () => {
    await cancelLeaveRequest(row.id)
    ElMessage.success('已取消')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.leave-tabs {
  margin-top: 16px;
}

.apply-form {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px 0;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  width: 100%;
}

.upload-area {
  width: 100%;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.search-bar {
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
