<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><EditPen /></el-icon> 请假申请</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新建请假</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
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

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border max-height="calc(100vh - 280px)">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="type" label="请假类型" width="100" align="center" />
        <el-table-column prop="startTime" label="开始时间" width="170" align="center" />
        <el-table-column prop="endTime" label="结束时间" width="170" align="center" />
        <el-table-column prop="reason" label="请假原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" align="center" />
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建请假申请" width="500px" destroy-on-close @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="请假类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择请假类型" style="width: 100%">
            <el-option label="事假" value="事假" />
            <el-option label="病假" value="病假" />
            <el-option label="公假" value="公假" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="请选择开始时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="请选择结束时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入请假原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStudentLeaveList, createStudentLeave } from '@/api/student'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const queryForm = reactive({ status: '', pageNum: 1, pageSize: 10 })
const form = reactive({ type: '', startTime: '', endTime: '', reason: '' })

const rules = {
  type: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

const statusType = (s) => ({ '待审批': 'warning', '已批准': 'success', '已驳回': 'danger' }[s] || 'info')
const statusLabel = (s) => ({ '待审批': '待审批', '已批准': '已批准', '已驳回': '已驳回' }[s] || s)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudentLeaveList(queryForm)
    tableData.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载请假列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { Object.assign(queryForm, { status: '', pageNum: 1, pageSize: 10 }); fetchData() }

const handleAdd = () => { resetForm(); dialogVisible.value = true }
const resetForm = () => { Object.assign(form, { type: '', startTime: '', endTime: '', reason: '' }) }

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await createStudentLeave(form)
    ElMessage.success('请假申请已提交')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => fetchData())
</script>