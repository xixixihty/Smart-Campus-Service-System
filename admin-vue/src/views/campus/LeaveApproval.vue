<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Checked /></el-icon> 请假审批</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="学生">
          <el-select v-model="queryForm.studentId" placeholder="请选择学生" clearable filterable>
            <el-option v-for="s in studentOptions" :key="s.id" :label="s.name + ' (' + s.studentNo + ')'" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 120px">
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
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="leaveType" label="请假类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.leaveType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="请假原因" min-width="180" show-overflow-tooltip />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '已批准' ? 'success' : row.status === '已驳回' ? 'danger' : row.status === '待审批' ? 'warning' : 'info'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === '待审批'">
              <el-button type="success" link @click="handleApprove(row, '已批准')">
                <el-icon><Select /></el-icon>通过
              </el-button>
              <el-button type="danger" link @click="handleApprove(row, '已驳回')">
                <el-icon><CloseBold /></el-icon>拒绝
              </el-button>
            </template>
            <el-text v-else type="info" size="small">已处理</el-text>
          </template>
        </el-table-column>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLeaveRequestList, approveLeaveRequest } from '@/api/leaveApproval'
import { getStudentList } from '@/api/student'

const loading = ref(false)
const approveLoading = ref(false)
const approveVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const studentOptions = ref([])
const currentRow = ref(null)

const queryForm = reactive({ pageNum: 1, pageSize: 10, studentId: '', status: '' })
const approveForm = reactive({ status: '', comment: '' })

const loadStudents = async () => {
  const res = await getStudentList({ pageNum: 1, pageSize: 500 })
  studentOptions.value = res.data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getLeaveRequestList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.studentId = ''; queryForm.status = ''; handleSearch() }

const handleApprove = (row, status) => {
  currentRow.value = row
  approveForm.status = status
  approveForm.comment = ''
  approveVisible.value = true
}

const handleApproveSubmit = async () => {
  approveLoading.value = true
  try {
    await approveLeaveRequest(currentRow.value.id, { status: approveForm.status, comment: approveForm.comment })
    ElMessage.success('审批完成')
    approveVisible.value = false; fetchData()
  } finally { approveLoading.value = false }
}

onMounted(async () => { await loadStudents(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
