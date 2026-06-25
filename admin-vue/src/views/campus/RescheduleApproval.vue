<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><RefreshRight /></el-icon> 调课审批</h2>
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe border max-height="calc(100vh - 280px)" scrollbar-always-on>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="courseName" label="课程" min-width="140" align="center" />
        <el-table-column prop="teacherName" label="申请教师" width="100" align="center" />
        <el-table-column prop="className" label="班级" width="130" />
        <el-table-column label="原安排" width="130" align="center">
          <template #default="{ row }">
            {{ weekDayLabel(row.originalWeekDay) }} 第{{ row.originalStartSection }}-{{ row.originalEndSection }}节
          </template>
        </el-table-column>
        <el-table-column label="调整至" width="130" align="center">
          <template #default="{ row }">
            {{ weekDayLabel(row.newWeekDay) }} 第{{ row.newStartSection }}-{{ row.newEndSection }}节
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="调课原因" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="170" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="handleApprove(row)">
              <el-icon><Select /></el-icon>通过
            </el-button>
            <el-button type="danger" link @click="handleReject(row)">
              <el-icon><CloseBold /></el-icon>驳回
            </el-button>
            <el-button type="info" link @click="handleView(row)">
              <el-icon><View /></el-icon>详情
            </el-button>
          </template>
        </el-table-column>
        <el-table-column width="12" class-name="scroll-hint-column" fixed="right" />
      </el-table>
      <el-empty v-if="tableData.length === 0 && !loading" description="暂无待审批调课申请" />
    </el-card>

    <el-dialog v-model="detailVisible" title="调课申请详情" width="600px" destroy-on-close>
      <el-descriptions v-if="detailData" :column="2" border>
        <el-descriptions-item label="申请教师">{{ detailData.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ detailData.courseName }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detailData.className }}</el-descriptions-item>
        <el-descriptions-item label="原教室">{{ detailData.originalClassroomName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="原时间">{{ weekDayLabel(detailData.originalWeekDay) }} 第{{ detailData.originalStartSection }}-{{ detailData.originalEndSection }}节</el-descriptions-item>
        <el-descriptions-item label="新教室">{{ detailData.newClassroomName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="新时间">{{ weekDayLabel(detailData.newWeekDay) }} 第{{ detailData.newStartSection }}-{{ detailData.newEndSection }}节</el-descriptions-item>
        <el-descriptions-item label="调课原因" :span="2">{{ detailData.reason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回调课申请" width="450px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingReschedules, getRescheduleDetail, approveReschedule, rejectReschedule } from '@/api/rescheduleApproval'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const detailData = ref(null)
const rejectVisible = ref(false)
const rejectLoading = ref(false)
const currentRow = ref(null)
const rejectForm = reactive({ reason: '' })

const weekDays = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
const weekDayLabel = (d) => weekDays[d] || ''

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPendingReschedules()
    tableData.value = res.data || []
  } catch (e) {
    console.error('获取待审批调课列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleView = async (row) => {
  try {
    const res = await getRescheduleDetail(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch (e) {
    console.error('获取调课详情失败:', e)
    ElMessage.error('获取调课详情失败')
  }
}

const handleApprove = async (row) => {
  try {
    await approveReschedule(row.id)
    ElMessage.success('已通过调课申请')
    fetchData()
  } catch (e) {
    ElMessage.error('审批操作失败')
  }
}

const handleReject = (row) => {
  currentRow.value = row
  rejectForm.reason = ''
  rejectVisible.value = true
}

const confirmReject = async () => {
  rejectLoading.value = true
  try {
    await rejectReschedule(currentRow.value.id, { reason: rejectForm.reason })
    ElMessage.success('已驳回调课申请')
    rejectVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('驳回操作失败')
  } finally {
    rejectLoading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
