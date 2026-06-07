<template>
  <div class="score-entry-page">
    <h2 class="page-title">
      <el-icon><Tickets /></el-icon>
      成绩录入
    </h2>

    <div class="toolbar">
      <span class="toolbar-label">学期：</span>
      <el-select v-model="semesterId" placeholder="请选择学期" @change="onSemesterChange" style="width: 200px">
        <el-option v-for="s in semesterList" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <span class="toolbar-label">课程：</span>
      <el-select v-model="courseId" placeholder="请选择课程" @change="onCourseChange" style="width: 200px">
        <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
      </el-select>
      <el-button type="warning" @click="fetchUnrecorded" :disabled="!courseId || !semesterId">
        <el-icon><WarningFilled /></el-icon> 查看未录入学生
      </el-button>
      <el-button type="success" @click="openBatchDialog" :disabled="unrecordedList.length === 0">
        <el-icon><Plus /></el-icon> 批量录入
      </el-button>
    </div>

    <!-- 成绩统计 -->
    <el-row :gutter="20" class="stats-row" v-if="scoreStats">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-small">
          <div class="stat-small-value">{{ scoreStats.totalStudents || 0 }}</div>
          <div class="stat-small-label">学生总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-small">
          <div class="stat-small-value" style="color: #67C23A">{{ scoreStats.recordedCount || 0 }}</div>
          <div class="stat-small-label">已录入</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-small">
          <div class="stat-small-value" style="color: #F56C6C">{{ scoreStats.unrecordedCount || 0 }}</div>
          <div class="stat-small-label">未录入</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-small">
          <div class="stat-small-value" style="color: #409EFF">{{ scoreStats.avgScore || '--' }}</div>
          <div class="stat-small-label">平均分</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" v-loading="loading" style="margin-top: 16px">
      <el-table :data="scoreList" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="studentNo" label="学号" min-width="140" />
        <el-table-column prop="studentName" label="姓名" min-width="120" />
        <el-table-column prop="usualScore" label="平时成绩" width="100" />
        <el-table-column prop="finalScore" label="期末成绩" width="100" />
      <el-table-column label="总评成绩" width="110">
          <template #default="{ row }">
            <span :style="{ color: row.totalScore >= 60 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
              {{ row.totalScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="绩点" width="80">
          <template #default="{ row }">
            <span v-if="row.scorePoint != null">{{ row.scorePoint }}</span>
            <span v-else class="text-secondary">自动计算</span>
          </template>
        </el-table-column>
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="examStatus" label="考试状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.examStatus === 'NORMAL' ? 'success' : 'warning'" size="small">
              {{ row.examStatus === 'NORMAL' ? '正考' : row.examStatus === 'MAKEUP' ? '补考' : row.examStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">修改</el-button>
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="scoreList.length === 0 && !loading" class="empty-state">
        <el-empty description="请选择学期和课程查看成绩" />
      </div>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchScoreList"
        />
      </div>
    </el-card>

    <!-- 修改成绩弹窗 -->
    <el-dialog v-model="editVisible" title="修改成绩" width="450px">
      <el-form :model="editForm" label-width="100px" v-if="editForm.id">
        <el-form-item label="平时成绩">
          <el-input-number v-model="editForm.usualScore" :min="0" :max="100" :precision="1" />
        </el-form-item>
        <el-form-item label="期末成绩">
          <el-input-number v-model="editForm.finalScore" :min="0" :max="100" :precision="1" />
        </el-form-item>
        <el-form-item label="总评成绩">
          <span v-if="editForm.totalScore != null" style="font-size:16px;font-weight:bold;line-height:32px">{{ editForm.totalScore }}</span>
          <span v-else class="text-secondary" style="line-height:32px">保存后将自动计算</span>
        </el-form-item>
        <el-form-item label="绩点">
          <span v-if="editForm.scorePoint != null" style="font-size:16px;font-weight:bold;line-height:32px">{{ editForm.scorePoint }}</span>
          <span v-else class="text-secondary" style="line-height:32px">保存后将自动计算</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量录入弹窗 -->
    <el-dialog v-model="batchVisible" title="批量录入成绩" width="800px">
      <el-table :data="batchForm.scores" stripe max-height="400">
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column label="平时成绩" width="120">
          <template #default="{ row, $index }">
            <el-input-number v-model="row.usualScore" :min="0" :max="100" :precision="1" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="期末成绩" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.finalScore" :min="0" :max="100" :precision="1" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="总评成绩" width="120">
          <template #default="{ row }">
            <span style="line-height:32px;color:#909399">自动计算</span>
          </template>
        </el-table-column>
        <el-table-column label="绩点" width="100">
          <template #default="{ row }">
            <span style="line-height:32px;color:#909399">自动计算</span>
          </template>
        </el-table-column>
        <el-table-column label="考试状态" width="100">
          <template #default="{ row }">
            <el-select v-model="row.examStatus" size="small">
              <el-option label="正考" value="NORMAL" />
              <el-option label="补考" value="MAKEUP" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="batchForm.scores.length === 0" class="empty-state">
        <el-empty description="所有学生成绩已录入" :image-size="60" />
      </div>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSubmitting" @click="submitBatch" :disabled="batchForm.scores.length === 0">
          批量提交
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="成绩详情" width="500px">
      <el-descriptions v-if="scoreDetail" :column="2" border size="small">
        <el-descriptions-item label="课程">{{ scoreDetail.courseName }}</el-descriptions-item>
        <el-descriptions-item label="学生">{{ scoreDetail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ scoreDetail.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ scoreDetail.semesterName }}</el-descriptions-item>
        <el-descriptions-item label="平时成绩">{{ scoreDetail.usualScore }}</el-descriptions-item>
        <el-descriptions-item label="期末成绩">{{ scoreDetail.finalScore }}</el-descriptions-item>
        <el-descriptions-item label="总评成绩">{{ scoreDetail.totalScore }}</el-descriptions-item>
        <el-descriptions-item label="绩点">{{ scoreDetail.scorePoint }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ scoreDetail.credit }}</el-descriptions-item>
        <el-descriptions-item label="考试状态">{{ scoreDetail.examStatus }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getScoreList, getScoreDetail, updateScore,
  getUnrecordedStudents, batchInsertScore, getScoreStats
} from '@/api/scoreEntry'
import { getTeachingCourses } from '@/api/course'
import { getSemesterList } from '@/api/semester'
import { Tickets, WarningFilled, Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const batchSubmitting = ref(false)

const semesterId = ref(null)
const courseId = ref(null)
const semesterList = ref([])
const courseList = ref([])
const scoreList = ref([])
const unrecordedList = ref([])
const scoreStats = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const editVisible = ref(false)
const editForm = reactive({ id: null, usualScore: null, finalScore: null, totalScore: null, scorePoint: null })

const batchVisible = ref(false)
const batchForm = reactive({ scores: [] })

const detailVisible = ref(false)
const scoreDetail = ref(null)

const fetchSemesters = async () => {
  try {
    const res = await getSemesterList({ pageNum: 1, pageSize: 50 })
    semesterList.value = res.data?.list || []
    const current = semesterList.value.find(s => s.isCurrent)
    if (current) {
      semesterId.value = current.id
    } else if (semesterList.value.length > 0) {
      semesterId.value = semesterList.value[0].id
    }
  } catch { /* ignore */ }
}

const fetchCourses = async () => {
  if (!semesterId.value) return
  try {
    const res = await getTeachingCourses(semesterId.value)
    courseList.value = res.data || []
  } catch { /* ignore */ }
}

const fetchScoreList = async () => {
  if (!courseId.value || !semesterId.value) return
  loading.value = true
  try {
    const res = await getScoreList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      courseId: courseId.value,
      semesterId: semesterId.value
    })
    scoreList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch {
    scoreList.value = []
  } finally {
    loading.value = false
  }
}

const fetchScoreStats = async () => {
  if (!semesterId.value) return
  try {
    const res = await getScoreStats(semesterId.value)
    scoreStats.value = res.data
  } catch { /* ignore */ }
}

const fetchUnrecorded = async () => {
  try {
    const res = await getUnrecordedStudents(courseId.value, semesterId.value)
    unrecordedList.value = res.data || []
    if (unrecordedList.value.length === 0) {
      ElMessage.success('所有学生成绩已录入完毕')
    } else {
      ElMessage.info(`还有 ${unrecordedList.value.length} 名学生未录入成绩`)
    }
  } catch { /* ignore */ }
}

const onSemesterChange = () => {
  courseId.value = null
  scoreList.value = []
  fetchCourses()
  fetchScoreStats()
}

const onCourseChange = () => {
  pageNum.value = 1
  fetchScoreList()
}

const openEditDialog = (row) => {
  editForm.id = row.id
  editForm.usualScore = row.usualScore
  editForm.finalScore = row.finalScore
  editForm.totalScore = row.totalScore
  editForm.scorePoint = row.scorePoint
  editVisible.value = true
}

const submitEdit = async () => {
  submitting.value = true
  try {
    await updateScore({
      id: editForm.id,
      usualScore: editForm.usualScore,
      finalScore: editForm.finalScore
    })
    ElMessage.success('成绩修改成功')
    editVisible.value = false
    fetchScoreList()
  } catch { /* ignore */ } finally {
    submitting.value = false
  }
}

const openBatchDialog = () => {
  batchForm.scores = unrecordedList.value.map(s => ({
    courseId: courseId.value,
    semesterId: semesterId.value,
    studentId: s.studentId,
    studentNo: s.studentNo,
    studentName: s.studentName,
    usualScore: null,
    finalScore: null,
    examStatus: 'NORMAL'
  }))
  batchVisible.value = true
}

const submitBatch = async () => {
  // 过滤掉平时成绩和期末成绩都为空的学生
  const validScores = batchForm.scores.filter(s => s.usualScore != null && s.finalScore != null)
  if (validScores.length === 0) {
    ElMessage.warning('请至少录入一名学生的平时成绩和期末成绩')
    batchSubmitting.value = false
    return
  }
  batchSubmitting.value = true
  try {
    await batchInsertScore({
      courseId: courseId.value,
      semesterId: semesterId.value,
      scoreEntries: validScores
    })
    ElMessage.success(`批量录入成功，共录入 ${validScores.length} 名学生成绩`)
    batchVisible.value = false
    fetchScoreList()
    fetchScoreStats()
    fetchUnrecorded()
  } catch { /* ignore */ } finally {
    batchSubmitting.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await getScoreDetail(row.id)
    scoreDetail.value = res.data
    detailVisible.value = true
  } catch { /* ignore */ }
}

onMounted(async () => {
  await fetchSemesters()
  if (semesterId.value) {
    fetchCourses()
    fetchScoreStats()
  }
})
</script>

<style scoped>
.stats-row {
  margin-top: 0;
}

.stat-card-small {
  text-align: center;
  padding: 8px;
}

.stat-small-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.stat-small-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.toolbar-label {
  font-size: 14px;
  color: #606266;
  margin-left: 8px;
}

.toolbar-label:first-child {
  margin-left: 0;
}
</style>