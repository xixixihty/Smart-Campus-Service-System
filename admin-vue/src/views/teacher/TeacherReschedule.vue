<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Refresh /></el-icon> 调课申请</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新建调课申请</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
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
          <el-table-column prop="courseName" label="课程名称" min-width="150" align="center" />
          <el-table-column prop="originalWeekDay" label="原星期" width="80" align="center">
            <template #default="{ row }">{{ weekDays[row.originalWeekDay - 1] || row.originalWeekDay }}</template>
          </el-table-column>
          <el-table-column label="原节次" width="100" align="center">
            <template #default="{ row }">{{ row.originalStartPeriod }}-{{ row.originalEndPeriod }}节</template>
          </el-table-column>
          <el-table-column prop="targetWeekDay" label="新星期" width="80" align="center">
            <template #default="{ row }">{{ weekDays[row.targetWeekDay - 1] || row.targetWeekDay }}</template>
          </el-table-column>
          <el-table-column label="新节次" width="100" align="center">
            <template #default="{ row }">{{ row.targetStartPeriod }}-{{ row.targetEndPeriod }}节</template>
          </el-table-column>
          <el-table-column prop="reason" label="调课原因" min-width="150" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="170" align="center" />
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 'PENDING'" type="danger" link @click="handleCancel(row)">
                <el-icon><Close /></el-icon>撤销
              </el-button>
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

    <el-dialog v-model="dialogVisible" title="新建调课申请" width="600px" destroy-on-close @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程" prop="scheduleId">
          <el-select v-model="form.scheduleId" placeholder="请选择课程" filterable style="width: 100%">
            <el-option v-for="s in scheduleOptions" :key="s.id"
              :label="`${s.courseName}(${weekDays[s.weekDay-1]} ${s.startPeriod}-${s.endPeriod}节)`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="新星期" prop="targetWeekDay">
          <el-select v-model="form.targetWeekDay" placeholder="请选择" style="width: 100%">
            <el-option v-for="(label, idx) in weekDays" :key="idx + 1" :label="label" :value="idx + 1" />
          </el-select>
        </el-form-item>
        <el-form-item label="新开始节次" prop="targetStartPeriod">
          <el-input-number v-model="form.targetStartPeriod" :min="1" :max="12" style="width: 100%" />
        </el-form-item>
        <el-form-item label="新结束节次" prop="targetEndPeriod">
          <el-input-number v-model="form.targetEndPeriod" :min="1" :max="12" style="width: 100%" />
        </el-form-item>
        <el-form-item label="调课原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入调课原因" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTeacherRescheduleList, createTeacherReschedule, cancelTeacherReschedule } from '@/api/teacher'
import { getTeacherTimetable } from '@/api/teacher'

const weekDays = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日']

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const scheduleOptions = ref([])

const queryForm = reactive({ status: '', pageNum: 1, pageSize: 10 })
const form = reactive({ scheduleId: '', targetWeekDay: null, targetStartPeriod: null, targetEndPeriod: null, reason: '' })

const rules = {
  scheduleId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  targetWeekDay: [{ required: true, message: '请选择新星期', trigger: 'change' }],
  targetStartPeriod: [{ required: true, message: '请输入开始节次', trigger: 'blur' }],
  targetEndPeriod: [{ required: true, message: '请输入结束节次', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入调课原因', trigger: 'blur' }]
}

const statusType = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info')
const statusLabel = (s) => ({ PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }[s] || s)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTeacherRescheduleList(queryForm)
    tableData.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载调课申请列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { Object.assign(queryForm, { status: '', pageNum: 1, pageSize: 10 }); fetchData() }

const handleAdd = async () => {
  try {
    const res = await getTeacherTimetable()
    scheduleOptions.value = res.data?.records || res.data || []
  } catch (e) { console.error('加载课表失败:', e) }
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(form, { scheduleId: '', targetWeekDay: null, targetStartPeriod: null, targetEndPeriod: null, reason: '' })
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await createTeacherReschedule(form)
    ElMessage.success('调课申请已提交，等待管理员审批')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定撤销该调课申请吗？', '提示', { type: 'warning' })
    await cancelTeacherReschedule(row.id)
    ElMessage.success('已撤销')
    fetchData()
  } catch {}
}

onMounted(() => fetchData())
</script>