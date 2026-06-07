<template>
  <div class="reschedule-page">
    <h2 class="page-title">
      <el-icon><RefreshRight /></el-icon>
      调课管理
    </h2>

    <div class="toolbar">
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon> 新建调课申请
      </el-button>
    </div>

    <el-card shadow="hover" v-loading="loading">
      <el-table :data="rescheduleList" stripe style="width: 100%">
        <el-table-column prop="courseName" label="课程名称" min-width="140" />
        <el-table-column prop="className" label="班级" min-width="110" />
        <el-table-column label="原时间" width="130">
          <template #default="{ row }">
            {{ weekDayLabel(row.originalWeekDay) }} 第{{ row.originalStartSection }}-{{ row.originalEndSection }}节
          </template>
        </el-table-column>
        <el-table-column prop="originalClassroomName" label="原教室" width="130" />
        <el-table-column label="新时间" width="130">
          <template #default="{ row }">
            <span v-if="row.newWeekDay">
              {{ weekDayLabel(row.newWeekDay) }} 第{{ row.newStartSection }}-{{ row.newEndSection }}节
            </span>
            <span v-else class="text-secondary">--</span>
          </template>
        </el-table-column>
        <el-table-column prop="newClassroomName" label="新教室" width="130">
          <template #default="{ row }">
            <span v-if="row.newClassroomName">{{ row.newClassroomName }}</span>
            <span v-else class="text-secondary">--</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="调课原因" min-width="130" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              link
              size="small"
              @click="handleCancel(row)"
            >
              <el-icon><Close /></el-icon> 取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="rescheduleList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无调课记录" />
      </div>
    </el-card>

    <!-- 新建调课弹窗 -->
    <el-dialog v-model="createVisible" title="新建调课申请" width="750px" destroy-on-close>
      <el-form ref="formRef" :model="createForm" :rules="rules" label-width="100px">
        <el-form-item label="调课原因" prop="reason">
          <el-input v-model="createForm.reason" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="请输入调课原因" />
        </el-form-item>
        <el-divider>调课明细</el-divider>
        <div v-for="(item, index) in createForm.items" :key="index" class="reschedule-item">
          <el-form-item :label="`排课记录`" :prop="`items.${index}.courseScheduleId`" :rules="[{ required: true, message: '请选择排课' }]">
            <el-select v-model="item.courseScheduleId" placeholder="请选择原排课记录" style="width: 100%" @focus="fetchTeachingSchedules">
              <el-option
                v-for="s in teachingSchedules"
                :key="s.courseScheduleId"
                :label="`${s.courseName} (${weekDayLabel(s.originalWeekDay)} ${s.originalStartSection}-${s.originalEndSection}节)`"
                :value="s.courseScheduleId"
              />
            </el-select>
          </el-form-item>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="新星期" :prop="`items.${index}.newWeekDay`">
                <el-select v-model="item.newWeekDay" placeholder="星期" style="width: 100%">
                  <el-option v-for="d in weekDays" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="新教室" :prop="`items.${index}.newClassroomId`">
                <el-select v-model="item.newClassroomId" placeholder="选择教室" clearable style="width: 100%">
                  <el-option
                    v-for="c in classroomList"
                    :key="c.id"
                    :label="`${c.building} ${c.roomNumber}`"
                    :value="c.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="开始节" :prop="`items.${index}.newStartSection`">
                <el-input-number v-model="item.newStartSection" :min="1" :max="12" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束节" :prop="`items.${index}.newEndSection`">
                <el-input-number v-model="item.newEndSection" :min="1" :max="12" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-button type="danger" link size="small" @click="removeItem(index)" v-if="createForm.items.length > 1">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </div>
        <el-button type="primary" link @click="addItem">
          <el-icon><Plus /></el-icon> 添加调课项
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="调课详情" width="550px">
      <el-descriptions v-if="detailData" :column="2" border size="small">
        <el-descriptions-item label="课程">{{ detailData.courseName }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detailData.className }}</el-descriptions-item>
        <el-descriptions-item label="原时间">{{ weekDayLabel(detailData.originalWeekDay) }} 第{{ detailData.originalStartSection }}-{{ detailData.originalEndSection }}节</el-descriptions-item>
        <el-descriptions-item label="原教室">{{ detailData.originalClassroomName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="新时间">
          <span v-if="detailData.newWeekDay">{{ weekDayLabel(detailData.newWeekDay) }} 第{{ detailData.newStartSection }}-{{ detailData.newEndSection }}节</span>
          <span v-else>--</span>
        </el-descriptions-item>
        <el-descriptions-item label="新教室">{{ detailData.newClassroomName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="调课原因">{{ detailData.reason || '--' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detailData.status)" size="small">{{ statusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMyReschedules, getRescheduleDetail, createReschedule,
  cancelReschedule, getTeachingCourseSchedules, getClassroomList
} from '@/api/courseReschedule'
import { RefreshRight, Plus, View, Close, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const rescheduleList = ref([])
const teachingSchedules = ref([])
const classroomList = ref([])

const createVisible = ref(false)
const detailVisible = ref(false)
const detailData = ref(null)
const formRef = ref(null)

const weekDays = [
  { label: '周一', value: 1 }, { label: '周二', value: 2 }, { label: '周三', value: 3 },
  { label: '周四', value: 4 }, { label: '周五', value: 5 }, { label: '周六', value: 6 }, { label: '周日', value: 7 }
]

const weekDayLabel = (d) => {
  const found = weekDays.find(w => w.value === d)
  return found ? found.label : `周${d}`
}

const statusType = (s) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }
  return map[s] || 'info'
}

const statusLabel = (s) => {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', CANCELLED: '已取消' }
  return map[s] || s
}

const createForm = reactive({
  reason: '',
  items: [{ courseScheduleId: null, newWeekDay: null, newStartSection: null, newEndSection: null, newClassroomId: null }]
})

const rules = {
  reason: [{ required: true, message: '请输入调课原因', trigger: 'blur' }]
}

const resetForm = () => {
  createForm.reason = ''
  createForm.items = [{ courseScheduleId: null, newWeekDay: null, newStartSection: null, newEndSection: null, newClassroomId: null }]
  formRef.value?.resetFields()
}

const addItem = () => {
  createForm.items.push({ courseScheduleId: null, newWeekDay: null, newStartSection: null, newEndSection: null, newClassroomId: null })
}

const removeItem = (index) => {
  createForm.items.splice(index, 1)
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMyReschedules()
    rescheduleList.value = res.data || []
  } catch {
    rescheduleList.value = []
  } finally {
    loading.value = false
  }
}

const fetchTeachingSchedules = async () => {
  if (teachingSchedules.value.length > 0) return
  try {
    const res = await getTeachingCourseSchedules()
    teachingSchedules.value = res.data || []
  } catch {
    // ignore
  }
}

const fetchClassrooms = async () => {
  if (classroomList.value.length > 0) return
  try {
    const res = await getClassroomList()
    classroomList.value = res.data?.list || []
  } catch {
    classroomList.value = []
  }
}

const openCreateDialog = () => {
  createVisible.value = true
  fetchTeachingSchedules()
  fetchClassrooms()
}

const submitCreate = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const items = createForm.items.map(item => {
      const classroom = classroomList.value.find(c => c.id === item.newClassroomId)
      return {
        courseScheduleId: item.courseScheduleId,
        newWeekDay: item.newWeekDay,
        newStartSection: item.newStartSection,
        newEndSection: item.newEndSection,
        newClassroomId: item.newClassroomId || null,
        newClassroomName: classroom ? `${classroom.building} ${classroom.roomNumber}` : ''
      }
    })
    await createReschedule({
      reason: createForm.reason,
      items
    })
    ElMessage.success('调课申请提交成功')
    createVisible.value = false
    fetchList()
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await getRescheduleDetail(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch {
    // ignore
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该调课申请吗？', '提示', { type: 'warning' })
    await cancelReschedule(row.id)
    ElMessage.success('已取消调课申请')
    fetchList()
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.reschedule-item {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.text-secondary {
  color: #909399;
  font-size: 12px;
}

html.dark .reschedule-item {
  background: #262727;
}
</style>