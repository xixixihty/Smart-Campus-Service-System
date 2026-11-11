<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><EditPen /></el-icon> 补考管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增补考场次</el-button>
    </div>

    <!-- 搜索区 -->
    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="学期">
          <el-select v-model="queryForm.semesterId" placeholder="请选择学期" clearable style="width: 200px">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.semesterName || s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="queryForm.courseId" placeholder="请选择课程" clearable filterable style="width: 200px">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="待安排" value="待安排" />
            <el-option label="已安排" value="已安排" />
            <el-option label="已通知" value="已通知" />
            <el-option label="已结束" value="已结束" />
            <el-option label="已取消" value="已取消" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 场次列表 -->
    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe border max-height="calc(100vh - 280px)" scrollbar-always-on>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" align="center" />
        <el-table-column prop="semesterName" label="学期" min-width="140" align="center" />
        <el-table-column prop="examDate" label="考试日期" width="120" align="center" />
        <el-table-column label="时间" width="140" align="center">
          <template #default="{ row }">{{ row.examStartTime }} - {{ row.examEndTime }}</template>
        </el-table-column>
        <el-table-column prop="location" label="考试地点" min-width="130" align="center" />
        <el-table-column label="人数" width="100" align="center">
          <template #default="{ row }">{{ row.enrolledCount }}/{{ row.maxCapacity }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <StatusBadge :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)" v-if="row.status === '待安排' || row.status === '已安排'"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-button v-if="row.status === '已安排'" type="success" link @click="handleNotify(row)"><el-icon><Bell /></el-icon>通知</el-button>
            <el-button v-if="row.status === '待安排'" type="danger" link @click="handleDelete(row)"><el-icon><Delete /></el-icon>删除</el-button>
            <el-button v-if="row.status === '已安排'" type="warning" link @click="handleCancel(row)"><el-icon><Close /></el-icon>取消</el-button>
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

    <!-- 新增/编辑 对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close @close="isView = false">
      <!-- 查看详情 -->
      <template v-if="isView">
        <el-tabs v-model="detailTab">
          <el-tab-pane label="场次信息" name="info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="场次ID">{{ detailData.id }}</el-descriptions-item>
              <el-descriptions-item label="课程名称">{{ detailData.courseName }}</el-descriptions-item>
              <el-descriptions-item label="学期">{{ detailData.semesterName }}</el-descriptions-item>
              <el-descriptions-item label="考试日期">{{ detailData.examDate }}</el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ detailData.examStartTime }}</el-descriptions-item>
              <el-descriptions-item label="结束时间">{{ detailData.examEndTime }}</el-descriptions-item>
              <el-descriptions-item label="考试地点">{{ detailData.location }}</el-descriptions-item>
              <el-descriptions-item label="人数">{{ detailData.enrolledCount }}/{{ detailData.maxCapacity }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <StatusBadge :status="detailData.status" />
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="学生列表" name="students">
            <div style="margin-bottom: 12px; display: flex; gap: 8px">
              <el-button type="primary" size="small" @click="openAddStudentDialog" v-if="detailData.status === '待安排' || detailData.status === '已安排'">
                <el-icon><Plus /></el-icon>添加学生
              </el-button>
              <el-button type="success" size="small" :disabled="selectedStudents.length === 0" @click="openBatchScore" v-if="detailData.status === '已通知' || detailData.status === '已结束'">
                <el-icon><EditPen /></el-icon>批量录入成绩
              </el-button>
            </div>
            <el-table :data="detailStudents" stripe border max-height="350" @selection-change="onStudentSelect">
              <el-table-column type="selection" width="45" v-if="detailData.status === '已通知' || detailData.status === '已结束'" />
              <el-table-column prop="studentNo" label="学号" width="120" />
              <el-table-column prop="studentName" label="姓名" min-width="100" />
              <el-table-column prop="originalScore" label="原始成绩" width="90" />
              <el-table-column prop="originalStatus" label="原始状态" width="90" />
              <el-table-column label="补考成绩" width="100">
                <template #default="{ row: stu }">
                  <span v-if="stu.makeupScore != null">{{ stu.makeupScore }}</span>
                  <el-tag v-else type="info" size="small">未录入</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="studentStatus" label="补考状态" width="90">
                <template #default="{ row: stu }">
                  <StatusBadge :status="stu.studentStatus" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" v-if="detailData.status === '已通知' || detailData.status === '已结束' || detailData.status === '待安排' || detailData.status === '已安排'">
                <template #default="{ row: stu }">
                  <el-button v-if="detailData.status === '已通知' || detailData.status === '已结束'" type="primary" link size="small" @click="openScoreInput(stu)">录入成绩</el-button>
                  <el-button v-if="detailData.status === '待安排' || detailData.status === '已安排'" type="danger" link size="small" @click="handleRemoveStudent(stu)">移除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </template>

      <!-- 新增/编辑 表单 -->
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学期" prop="semesterId">
          <el-select v-model="form.semesterId" placeholder="请选择学期" style="width: 100%" @change="onSemesterChange">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.semesterName || s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%" filterable @change="onCourseChange">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="candidateCount > 0" label="待补考">
          <el-tag type="warning">{{ candidateCount }} 人待补考</el-tag>
        </el-form-item>
        <el-form-item label="考试日期" prop="examDate">
          <el-date-picker v-model="form.examDate" type="date" placeholder="请选择考试日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="开始时间" prop="examStartTime">
          <el-time-picker v-model="form.examStartTime" placeholder="请选择开始时间" style="width: 100%" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="examEndTime">
          <el-time-picker v-model="form.examEndTime" placeholder="请选择结束时间" style="width: 100%" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="考试地点" prop="location">
          <el-select v-model="form.location" placeholder="请选择教室" style="width: 100%" filterable>
            <el-option v-for="r in classroomOptions" :key="r.id" :label="(r.building || '') + '-' + (r.roomNumber || '')" :value="(r.building || '') + '-' + (r.roomNumber || '')" />
          </el-select>
        </el-form-item>
        <el-form-item label="人数上限" prop="maxCapacity">
          <el-input-number v-model="form.maxCapacity" :min="1" :max="200" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 成绩录入弹窗 -->
    <el-dialog v-model="scoreDialogVisible" title="录入补考成绩" width="400px" destroy-on-close>
      <el-form ref="scoreFormRef" :model="scoreForm" :rules="scoreRules" label-width="100px">
        <el-form-item label="学生">
          <span>{{ scoreForm.studentName }} ({{ scoreForm.studentNo }})</span>
        </el-form-item>
        <el-form-item label="补考成绩" prop="score">
          <el-input-number v-model="scoreForm.score" :min="0" :max="100" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="scoreSubmitLoading" @click="handleScoreSubmit">确定</el-button>
      </template>
    </el-dialog>
  <!-- 批量成绩录入弹窗 -->
    <el-dialog v-model="batchScoreDialogVisible" title="批量录入补考成绩" width="480px" destroy-on-close>
      <p style="margin-bottom: 12px; color: #606266">已选择 <b>{{ selectedStudents.length }}</b> 名学生</p>
      <el-table :data="selectedStudents" stripe border max-height="250">
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" />
        <el-table-column label="本次成绩" width="130">
          <template #default="{ row }">
            <el-input-number v-model="batchScoreMap[row.studentId]" :min="0" :max="100" :precision="2" size="small" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="batchScoreDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchScoreLoading" @click="handleBatchScoreSubmit">批量提交</el-button>
      </template>
    </el-dialog>

    <!-- 添加学生弹窗 -->
    <el-dialog v-model="addStudentDialogVisible" title="添加补考学生" width="800px" destroy-on-close>
      <el-form :inline="true" :model="addStudentForm">
        <el-form-item label="课程">
          <el-select v-model="addStudentForm.courseId" placeholder="请选择课程" filterable clearable style="width: 200px">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchCandidates"><el-icon><Search /></el-icon>搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="candidateList" stripe border max-height="350" v-loading="candidateLoading" @selection-change="onCandidateSelect">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" min-width="100" />
        <el-table-column prop="originalScore" label="原始成绩" width="90" />
        <el-table-column prop="originalStatus" label="原始状态" width="90" />
        <el-table-column prop="courseName" label="课程" min-width="120" />
      </el-table>
      <template #footer>
        <el-button @click="addStudentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addStudentLoading" :disabled="selectedCandidates.length === 0" @click="handleAddStudents">添加选中学生 ({{ selectedCandidates.length }})</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSessionList, createSession, updateSession, deleteSession, cancelSession, getSessionDetail, scanCandidates, notifyStudents, inputSingleScore, batchInputScore, addStudentToSession, removeStudentFromSession } from '@/api/makeupExam'
import { getSemesterList } from '@/api/semester'
import { getCourseList } from '@/api/course'
import { getClassroomList } from '@/api/classroom'
import StatusBadge from '@/components/StatusBadge.vue'
import { Bell, Close } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增补考场次')
const isEdit = ref(false)
const isView = ref(false)
const detailTab = ref('info')
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const semesterOptions = ref([])
const courseOptions = ref([])
const classroomOptions = ref([])
const candidateCount = ref(0)
const detailData = reactive({
  id: null, courseName: '', semesterName: '', examDate: '', examStartTime: '', examEndTime: '',
  location: '', maxCapacity: 0, enrolledCount: 0, status: '', createTime: '', updateTime: '',
  semesterId: '', courseId: ''
})
const detailStudents = ref([])

const queryForm = reactive({ pageNum: 1, pageSize: 10, semesterId: '', courseId: '', status: '' })
const form = reactive({
  id: null, semesterId: '', courseId: '', examDate: '', examStartTime: '', examEndTime: '', location: '', maxCapacity: 60
})
const rules = {
  semesterId: [{ required: true, message: '请选择学期', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  examDate: [{ required: true, message: '请选择考试日期', trigger: 'change' }],
  examStartTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  examEndTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  location: [{ required: true, message: '请选择考试地点', trigger: 'change' }]
}

// 成绩录入
const scoreDialogVisible = ref(false)
const scoreSubmitLoading = ref(false)
const scoreFormRef = ref(null)
const scoreForm = reactive({ sessionId: null, studentId: null, studentName: '', studentNo: '', score: null })
const scoreRules = { score: [{ required: true, message: '请输入补考成绩', trigger: 'blur' }] }

// 批量成绩录入
const batchScoreDialogVisible = ref(false)
const batchScoreLoading = ref(false)
const selectedStudents = ref([])
const batchScoreMap = reactive({})

// 添加学生
const addStudentDialogVisible = ref(false)
const addStudentLoading = ref(false)
const candidateLoading = ref(false)
const addStudentForm = reactive({ courseId: '' })
const candidateList = ref([])
const selectedCandidates = ref([])

const loadOptions = async () => {
  const [semesterRes, courseRes, classroomRes] = await Promise.all([
    getSemesterList({ pageNum: 1, pageSize: 100 }),
    getCourseList({ pageNum: 1, pageSize: 200 }),
    getClassroomList({ pageNum: 1, pageSize: 200 })
  ])
  semesterOptions.value = (semesterRes.data && semesterRes.data.list) ? semesterRes.data.list : []
  courseOptions.value = (courseRes.data && courseRes.data.list) ? courseRes.data.list : []
  classroomOptions.value = (classroomRes.data && classroomRes.data.list) ? classroomRes.data.list : []
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { pageNum: queryForm.pageNum, pageSize: queryForm.pageSize }
    if (queryForm.semesterId) params.semesterId = queryForm.semesterId
    if (queryForm.courseId) params.courseId = queryForm.courseId
    if (queryForm.status) params.status = queryForm.status
    const res = await getSessionList(params)
    tableData.value = (res.data && res.data.list) ? res.data.list : []
    total.value = (res.data && res.data.total) ? res.data.total : 0
  } catch (err) {
    console.error('获取场次列表失败:', err)
    ElMessage.error('获取场次列表失败')
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.semesterId = ''; queryForm.courseId = ''; queryForm.status = ''; handleSearch() }

const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增补考场次'
  candidateCount.value = 0
  Object.assign(form, { id: null, semesterId: '', courseId: '', examDate: '', examStartTime: '', examEndTime: '', location: '', maxCapacity: 60 })
  dialogVisible.value = true
}

const onSemesterChange = () => { candidateCount.value = 0 }
const onCourseChange = async () => {
  if (!form.semesterId || !form.courseId) { candidateCount.value = 0; return }
  try {
    const res = await scanCandidates({ courseId: form.courseId, semesterId: form.semesterId })
    const groups = res.data || []
    if (groups.length > 0) {
      candidateCount.value = groups[0].candidateCount || 0
    } else {
      candidateCount.value = 0
    }
  } catch { candidateCount.value = 0 }
}

const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '补考场次详情'
  detailTab.value = 'info'
  try {
    const detail = await getSessionDetail(row.id)
    const data = detail.data
    Object.assign(detailData, {
      id: data.id, courseName: data.courseName || '', semesterName: data.semesterName || '',
      examDate: data.examDate || '', examStartTime: data.examStartTime || '', examEndTime: data.examEndTime || '',
      location: data.location || '', maxCapacity: data.maxCapacity || 0, enrolledCount: data.enrolledCount || 0,
      status: data.status || '', createTime: data.createTime || '', updateTime: data.updateTime || '',
      semesterId: data.semesterId || '', courseId: data.courseId || ''
    })
    detailStudents.value = data.students || []
    dialogVisible.value = true
  } catch (err) {
    console.error('获取详情失败:', err)
    ElMessage.error('获取详情失败')
    dialogVisible.value = false
  }
}

const handleEdit = async (row) => {
  isEdit.value = true; isView.value = false; dialogTitle.value = '编辑补考场次'
  try {
    const detail = await getSessionDetail(row.id)
    const data = detail.data
    Object.assign(form, {
      id: data.id, semesterId: data.semesterId || '', courseId: data.courseId || '',
      examDate: data.examDate || '', examStartTime: data.examStartTime || '', examEndTime: data.examEndTime || '',
      location: data.location || '', maxCapacity: data.maxCapacity || 60
    })
    dialogVisible.value = true
  } catch (err) {
    console.error('获取详情失败:', err)
    ElMessage.error('获取详情失败')
  }
}

const handleNotify = async (row) => {
  ElMessageBox.confirm('确定要通知该场次的所有学生吗？', '确认通知', { type: 'info' }).then(async () => {
    await notifyStudents(row.id)
    ElMessage.success('通知已发送')
    fetchData()
  }).catch(() => {})
}

const handleDelete = async (row) => {
  ElMessageBox.confirm('确定要删除该补考场次吗？删除后不可恢复。', '提示', { type: 'warning' }).then(async () => {
    await deleteSession(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handleCancel = async (row) => {
  ElMessageBox.confirm('确定要取消该补考场次吗？已通知的学生将收到取消通知。', '提示', { type: 'warning' }).then(async () => {
    await cancelSession(row.id)
    ElMessage.success('取消成功')
    fetchData()
  }).catch(() => {})
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload = {
      courseId: form.courseId,
      semesterId: form.semesterId,
      examDate: form.examDate,
      examStartTime: form.examStartTime,
      examEndTime: form.examEndTime,
      location: form.location,
      maxCapacity: form.maxCapacity
    }
    if (isEdit.value) {
      await updateSession(form.id, payload)
    } else {
      await createSession(payload)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchData()
  } catch (err) {
    const msg = (err.response && err.response.data && err.response.data.message) || (err.message) || '操作失败'
    ElMessage.error(msg)
  } finally { submitLoading.value = false }
}

const openScoreInput = (student) => {
  scoreForm.sessionId = detailData.id
  scoreForm.studentId = student.studentId
  scoreForm.studentName = student.studentName
  scoreForm.studentNo = student.studentNo
  scoreForm.score = student.makeupScore || null
  scoreDialogVisible.value = true
}

const handleScoreSubmit = async () => {
  const valid = await scoreFormRef.value.validate().catch(() => false)
  if (!valid) return
  scoreSubmitLoading.value = true
  try {
    await inputSingleScore(scoreForm.sessionId, scoreForm.studentId, { score: scoreForm.score })
    ElMessage.success('成绩录入成功')
    scoreDialogVisible.value = false
    // 刷新详情
    handleView({ id: detailData.id })
  } catch (err) {
    const msg = (err.response && err.response.data && err.response.data.message) || (err.message) || '成绩录入失败'
    ElMessage.error(msg)
  } finally { scoreSubmitLoading.value = false }
}

// ========== 批量成绩录入 ==========
const onStudentSelect = (selection) => {
  selectedStudents.value = selection
  // 清空并初始化 batchScoreMap
  Object.keys(batchScoreMap).forEach(k => delete batchScoreMap[k])
  selection.forEach(s => { batchScoreMap[s.studentId] = s.makeupScore || null })
}

const openBatchScore = () => {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请先选择学生')
    return
  }
  batchScoreDialogVisible.value = true
}

const handleBatchScoreSubmit = async () => {
  const scores = selectedStudents.value.map(s => ({
    studentId: s.studentId,
    score: batchScoreMap[s.studentId]
  }))
  const invalid = scores.find(s => s.score == null || s.score < 0 || s.score > 100)
  if (invalid) { ElMessage.warning('请为所有学生填写有效成绩(0-100)'); return }
  batchScoreLoading.value = true
  try {
    await batchInputScore(detailData.id, { scores })
    ElMessage.success('批量录入成功')
    batchScoreDialogVisible.value = false
    selectedStudents.value = []
    handleView({ id: detailData.id })
  } catch (err) {
    const msg = (err.response && err.response.data && err.response.data.message) || (err.message) || '批量录入失败'
    ElMessage.error(msg)
  } finally { batchScoreLoading.value = false }
}

// ========== 添加学生 ==========
const openAddStudentDialog = () => {
  addStudentForm.courseId = ''
  candidateList.value = []
  selectedCandidates.value = []
  addStudentDialogVisible.value = true
}

const onCandidateSelect = (selection) => { selectedCandidates.value = selection }

const searchCandidates = async () => {
  if (!detailData.semesterId) {
    const detail = await getSessionDetail(detailData.id)
    detailData.semesterId = detail.data.semesterId
  }
  candidateLoading.value = true
  try {
    const res = await scanCandidates({
      courseId: addStudentForm.courseId || detailData.courseId,
      semesterId: detailData.semesterId
    })
    const groups = res.data || []
    candidateList.value = groups.length > 0 ? (groups[0].candidates || []) : []
  } catch (err) {
    ElMessage.error('获取候选人失败')
  } finally { candidateLoading.value = false }
}

const handleAddStudents = async () => {
  if (selectedCandidates.value.length === 0) { ElMessage.warning('请选择学生'); return }
  addStudentLoading.value = true
  try {
    for (const c of selectedCandidates.value) {
      await addStudentToSession(detailData.id, c.studentId)
    }
    ElMessage.success(`成功添加 ${selectedCandidates.value.length} 名学生`)
    addStudentDialogVisible.value = false
    selectedCandidates.value = []
    handleView({ id: detailData.id })
  } catch (err) {
    const msg = (err.response && err.response.data && err.response.data.message) || (err.message) || '添加失败'
    ElMessage.error(msg)
  } finally { addStudentLoading.value = false }
}

// ========== 移除学生 ==========
const handleRemoveStudent = async (student) => {
  ElMessageBox.confirm(`确定要移除学生"${student.studentName}"吗？`, '确认移除', { type: 'warning' }).then(async () => {
    await removeStudentFromSession(detailData.id, student.studentId)
    ElMessage.success('移除成功')
    handleView({ id: detailData.id })
  }).catch(() => {})
}

onMounted(async () => { await loadOptions(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>