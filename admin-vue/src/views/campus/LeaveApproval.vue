<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Checked /></el-icon> 请假审批</h2>
      <div class="header-actions">
        <el-tag v-if="wsConnected" type="success" size="small">实时通知已连接</el-tag>
        <el-tag v-else type="info" size="small">实时通知未连接</el-tag>
      </div>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="申请人">
          <el-input v-model="queryForm.studentName" placeholder="请输入申请人姓名" clearable class="search-input" />
        </el-form-item>
        <el-form-item label="请假类型">
          <el-select v-model="queryForm.leaveType" placeholder="请选择类型" clearable class="search-input">
            <el-option label="事假" value="事假" />
            <el-option label="病假" value="病假" />
            <el-option label="公假" value="公假" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable class="search-input">
            <el-option label="待审批" value="待审批" />
            <el-option label="已批准" value="已批准" />
            <el-option label="已驳回" value="已驳回" />
            <el-option label="已取消" value="已取消" />
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
        <el-table-column prop="applicantType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.applicantType === 'STUDENT' ? 'primary' : 'success'" size="small">
              {{ row.applicantType === 'STUDENT' ? '学生' : '教师' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="申请人" width="120" />
        <el-table-column prop="leaveType" label="请假类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLeaveTypeColor(row.leaveType)" size="small">{{ row.leaveType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="approverName" label="审批人" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.approverName ? '#303133' : '#909399' }">
              {{ row.approverName || '待匹配' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <StatusBadge :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '待审批'" type="success" link @click="handleApprove(row)">
              <el-icon><Select /></el-icon>批准
            </el-button>
            <el-button v-if="row.status === '待审批'" type="danger" link @click="handleReject(row)">
              <el-icon><CloseBold /></el-icon>驳回
            </el-button>
            <el-button type="info" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
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

    <el-dialog v-model="approveVisible" title="审批意见" width="500px">
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="审批结果">
          <el-tag :type="approveForm.status === '已批准' ? 'success' : 'danger'" size="large">
            {{ approveForm.status === '已批准' ? '通过' : '拒绝' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.comment" type="textarea" :rows="4" placeholder="请输入审批意见（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" :loading="approveLoading" @click="handleApproveSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="请假详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请人">{{ detailForm.studentName }}</el-descriptions-item>
        <el-descriptions-item label="申请人类型">
          {{ detailForm.applicantType === 'STUDENT' ? '学生' : detailForm.applicantType === 'TEACHER' ? '教师' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="审批人">
          <span :style="{ color: detailForm.approverName ? '#303133' : '#909399' }">
            {{ detailForm.approverName || '待匹配' }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="请假类型">
          <el-tag :type="getLeaveTypeColor(detailForm.leaveType)" size="small">{{ detailForm.leaveType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(detailForm.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(detailForm.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailForm.status === '已批准' ? 'success' : detailForm.status === '已驳回' ? 'danger' : detailForm.status === '待审批' ? 'warning' : 'info'" size="small">
            {{ detailForm.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDateTime(detailForm.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="请假原因" :span="2">
          <div class="detail-reason">{{ detailForm.reason }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="detailForm.approvalLogs && detailForm.approvalLogs.length > 0" class="approval-logs">
        <h4 class="logs-title">审批记录</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(log, index) in detailForm.approvalLogs"
            :key="index"
            :type="log.result === 'approved' ? 'success' : 'danger'"
            :timestamp="formatDateTime(log.approveTime)"
            placement="top"
          >
            <el-card shadow="hover">
              <p><strong>审批人：</strong>{{ log.approverName }}</p>
              <p><strong>审批结果：</strong>
                <span :class="log.result === 'approved' ? 'result-approved' : 'result-rejected'">{{ log.result === 'approved' ? '批准' : '驳回' }}</span>
              </p>
              <p v-if="log.comment"><strong>审批意见：</strong>{{ log.comment }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLeaveRequestList, approveLeaveRequest, getLeaveRequestDetail } from '@/api/leaveApproval'
import StatusBadge from '@/components/StatusBadge.vue'
import { useWebSocket } from '@/composables/useWebSocket'

const loading = ref(false)
const approveLoading = ref(false)
const approveVisible = ref(false)
const detailVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const currentRow = ref(null)
const wsConnected = ref(false)

const { connect, subscribe, disconnect } = useWebSocket()

const detailForm = reactive({
  id: null,
  studentName: '',
  applicantType: '',
  approverName: '',
  leaveType: '',
  startTime: '',
  endTime: '',
  reason: '',
  status: '',
  createTime: '',
  approvalLogs: []
})

const queryForm = reactive({ pageNum: 1, pageSize: 10, studentName: '', leaveType: '', status: '' })
const approveForm = reactive({ status: '', comment: '' })

const getLeaveTypeColor = (type) => {
  const colorMap = { '事假': 'warning', '病假': 'danger', '公假': 'success' }
  return colorMap[type] || 'info'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ')
}

const initWebSocket = async () => {
  try {
    await connect()
    wsConnected.value = true

    subscribe('/queue/leave/apply', (payload) => {
      ElMessage({
        message: `新的请假申请：${payload.title || ''}`,
        type: 'info',
        duration: 5000
      })
      fetchData()
    })
  } catch (e) {
    console.warn('WebSocket连接失败:', e.message)
    wsConnected.value = false
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getLeaveRequestList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('获取请假列表失败:', e)
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.studentName = ''; queryForm.leaveType = ''; queryForm.status = ''; handleSearch() }

const handleView = async (row) => {
  try {
    const res = await getLeaveRequestDetail(row.id)
    const data = res.data
    Object.assign(detailForm, {
      id: data.id,
      studentName: data.studentName,
      applicantType: data.applicantType || '',
      approverName: data.approverName || '',
      leaveType: data.leaveType,
      startTime: data.startTime,
      endTime: data.endTime,
      reason: data.reason,
      status: data.status,
      createTime: data.createTime,
      approvalLogs: data.approvalLogs || []
    })
    detailVisible.value = true
  } catch (err) {
    console.error('获取请假详情失败:', err)
    ElMessage.error('获取请假详情失败')
  }
}

const handleApprove = (row, status) => {
  currentRow.value = row
  approveForm.status = status
  approveForm.comment = ''
  approveVisible.value = true
}

const handleApproveSubmit = async () => {
  approveLoading.value = true
  try {
    const action = approveForm.status === '已批准' ? 'approved' : 'rejected'
    await approveLeaveRequest(currentRow.value.id, { action: action, comment: approveForm.comment })
    ElMessage.success('审批完成')
    approveVisible.value = false
    fetchData()
  } catch (e) {
    console.error('审批操作失败:', e)
  } finally { approveLoading.value = false }
}

onMounted(() => {
  fetchData()
  initWebSocket()
})

onUnmounted(() => {
  disconnect()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.header-actions { display: flex; align-items: center; gap: 12px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.search-form { display: flex; flex-wrap: wrap; gap: 0; }
.search-form :deep(.el-form-item) { margin-bottom: 16px; }
.search-input { width: 200px; }
.detail-reason {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
  color: #606266;
}
.approval-logs { margin-top: 24px; }
.logs-title { font-size: 16px; font-weight: 600; color: #303133; margin-bottom: 16px; }
.result-approved { color: #67c23a; font-weight: 600; }
.result-rejected { color: #f56c6c; font-weight: 600; }
</style>