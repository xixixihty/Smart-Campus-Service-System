<template>
  <div class="leave-approval-page">
    <h2 class="page-title">
      <el-icon><Checked /></el-icon>
      请假审批
    </h2>

    <el-card shadow="hover" v-loading="loading">
      <el-table :data="leaveList" stripe style="width: 100%">
        <el-table-column prop="studentName" label="学生姓名" min-width="120" />
        <el-table-column prop="studentNo" label="学号" min-width="140" />
        <el-table-column prop="className" label="班级" min-width="140" />
        <el-table-column prop="leaveType" label="请假类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="leaveTypeTag(row.leaveType)">
              {{ leaveTypeLabel(row.leaveType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请假时间" min-width="200">
          <template #default="{ row }">
            {{ row.startTime }} ~ {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="请假原因" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" link size="small" @click="handleApprove(row, 'APPROVED')">
                <el-icon><Select /></el-icon> 通过
              </el-button>
              <el-button type="danger" link size="small" @click="handleApprove(row, 'REJECTED')">
                <el-icon><CloseBold /></el-icon> 驳回
              </el-button>
            </template>
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="leaveList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无待审批请假申请" />
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="请假申请详情" width="600px">
      <el-descriptions v-if="leaveDetail" :column="2" border size="small">
        <el-descriptions-item label="学生姓名">{{ leaveDetail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ leaveDetail.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ leaveDetail.className }}</el-descriptions-item>
        <el-descriptions-item label="请假类型">
          <el-tag size="small" :type="leaveTypeTag(leaveDetail.leaveType)">
            {{ leaveTypeLabel(leaveDetail.leaveType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ leaveDetail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ leaveDetail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="请假原因" :span="2">{{ leaveDetail.reason }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="statusType(leaveDetail.status)" size="small">{{ statusLabel(leaveDetail.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 审批日志 -->
      <el-divider>审批日志</el-divider>
      <el-timeline v-if="approvalLogs.length > 0">
        <el-timeline-item
          v-for="log in approvalLogs"
          :key="log.id"
          :timestamp="log.createTime"
          :type="log.result === 'APPROVED' ? 'success' : log.result === 'REJECTED' ? 'danger' : 'primary'"
        >
          <p>{{ log.approverName }} - {{ log.result === 'APPROVED' ? '通过' : log.result === 'REJECTED' ? '驳回' : log.result }}</p>
          <p v-if="log.comment" class="log-comment">{{ log.comment }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无审批日志" :image-size="60" />
    </el-dialog>

    <!-- 审批弹窗 -->
    <el-dialog v-model="approveVisible" :title="approveAction === 'APPROVED' ? '通过请假申请' : '驳回请假申请'" width="450px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input
            v-model="approveForm.comment"
            type="textarea"
            :rows="3"
            :placeholder="approveAction === 'APPROVED' ? '请输入审批意见（可选）' : '请输入驳回原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button :type="approveAction === 'APPROVED' ? 'success' : 'danger'" :loading="approving" @click="submitApprove">
          确认{{ approveAction === 'APPROVED' ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingLeaveRequests, getLeaveRequestDetail, approveLeaveRequest, getApprovalLogs } from '@/api/leaveApproval'
import { Checked, Select, CloseBold, View } from '@element-plus/icons-vue'

const loading = ref(false)
const approving = ref(false)
const leaveList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const leaveDetail = ref(null)
const approvalLogs = ref([])

const approveVisible = ref(false)
const approveAction = ref('')
const approveTargetId = ref(null)
const approveForm = reactive({ comment: '' })

const leaveTypeTag = (t) => {
  const map = { SICK: 'danger', PERSONAL: 'warning', OTHER: 'info' }
  return map[t] || 'info'
}

const leaveTypeLabel = (t) => {
  const map = { SICK: '病假', PERSONAL: '事假', OTHER: '其他' }
  return map[t] || t
}

const statusType = (s) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }
  return map[s] || 'info'
}

const statusLabel = (s) => {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', CANCELLED: '已取消' }
  return map[s] || s
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getPendingLeaveRequests({ pageNum: pageNum.value, pageSize: pageSize.value })
    leaveList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch {
    leaveList.value = []
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const [detailRes, logsRes] = await Promise.all([
      getLeaveRequestDetail(row.id),
      getApprovalLogs(row.id)
    ])
    leaveDetail.value = detailRes.data
    approvalLogs.value = logsRes.data || []
    detailVisible.value = true
  } catch {
    // ignore
  }
}

const handleApprove = (row, action) => {
  approveTargetId.value = row.id
  approveAction.value = action
  approveForm.comment = ''
  approveVisible.value = true
}

const submitApprove = async () => {
  approving.value = true
  try {
    await approveLeaveRequest(approveTargetId.value, {
      result: approveAction.value,
      comment: approveForm.comment
    })
    ElMessage.success(approveAction.value === 'APPROVED' ? '已通过请假申请' : '已驳回请假申请')
    approveVisible.value = false
    fetchList()
  } catch {
    // ignore
  } finally {
    approving.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.log-comment {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
</style>