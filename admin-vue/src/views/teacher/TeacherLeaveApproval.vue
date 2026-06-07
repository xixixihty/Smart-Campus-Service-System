<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Checked /></el-icon> 请假审批</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="学生姓名">
          <el-input v-model="queryForm.studentName" placeholder="请输入学生姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="待审批" value="待审批" />
            <el-option label="已批准" value="已批准" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="table-card">
      <el-card shadow="never">
        <el-table :data="tableData" v-loading="loading" stripe border max-height="calc(100vh - 280px)">
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="studentName" label="学生姓名" width="100" align="center" />
          <el-table-column prop="studentNo" label="学号" width="120" align="center" />
          <el-table-column prop="className" label="班级" width="130" align="center" />
          <el-table-column prop="type" label="请假类型" width="100" align="center" />
          <el-table-column prop="startTime" label="开始时间" width="170" align="center" />
          <el-table-column prop="endTime" label="结束时间" width="170" align="center" />
          <el-table-column prop="reason" label="请假原因" min-width="150" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === '待审批'">
                <el-button type="success" link @click="handleApprove(row)"><el-icon><Select /></el-icon>通过</el-button>
                <el-button type="danger" link @click="handleReject(row)"><el-icon><CloseBold /></el-icon>拒绝</el-button>
              </template>
              <span v-else style="color: #909399">--</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
            :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
            @size-change="fetchData" @current-change="fetchData" />
        </div>
      </el-card>
    </div>

    <el-dialog v-model="approveDialogVisible" title="审批意见" width="450px" destroy-on-close>
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审批结果">
          <el-tag :type="approveForm.action === 'approved' ? 'success' : 'danger'">
            {{ approveForm.action === 'approved' ? '通过' : '拒绝' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="approveLoading" @click="submitApprove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTeacherLeaveList, approveTeacherLeave } from '@/api/teacher'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const approveDialogVisible = ref(false)
const approveLoading = ref(false)
const currentRow = ref(null)

const queryForm = reactive({ studentName: '', status: '', pageNum: 1, pageSize: 10 })
const approveForm = reactive({ action: '', comment: '' })

const statusType = (s) => ({ '待审批': 'warning', '已批准': 'success', '已驳回': 'danger' }[s] || 'info')
const statusLabel = (s) => ({ '待审批': '待审批', '已批准': '已批准', '已驳回': '已驳回' }[s] || s)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTeacherLeaveList(queryForm)
    tableData.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载请假列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { Object.assign(queryForm, { studentName: '', status: '', pageNum: 1, pageSize: 10 }); fetchData() }

const handleApprove = (row) => {
  currentRow.value = row
  approveForm.action = 'approved'
  approveForm.comment = ''
  approveDialogVisible.value = true
}

const handleReject = (row) => {
  currentRow.value = row
  approveForm.action = 'rejected'
  approveForm.comment = ''
  approveDialogVisible.value = true
}

const submitApprove = async () => {
  approveLoading.value = true
  try {
    await approveTeacherLeave(currentRow.value.id, { action: approveForm.action, comment: approveForm.comment })
    ElMessage.success(approveForm.action === 'approved' ? '已通过' : '已拒绝')
    approveDialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    approveLoading.value = false
  }
}

onMounted(() => fetchData())
</script>