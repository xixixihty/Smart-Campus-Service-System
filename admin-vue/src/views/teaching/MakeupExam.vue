<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><EditPen /></el-icon> 补考管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增补考</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="学期">
          <el-select v-model="queryForm.semesterId" placeholder="请选择学期" clearable style="width: 200px">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.semesterName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="queryForm.courseId" placeholder="请选择课程" clearable filterable style="width: 200px">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
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
        <el-table-column prop="studentName" label="学生姓名" min-width="120" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" align="center" />
        <el-table-column prop="examDate" label="补考日期" width="120" align="center" />
        <el-table-column prop="startTime" label="开始时间" width="120" align="center" />
        <el-table-column prop="endTime" label="结束时间" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '已考' ? 'success' : row.status === '待考' ? 'warning' : 'info'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)"><el-icon><Delete /></el-icon>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close @close="isView = false">
      <!-- 查看详情：描述列表 -->
      <el-descriptions v-if="isView" :column="2" border size="default">
        <el-descriptions-item label="补考ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ detailData.courseName }}</el-descriptions-item>
        <el-descriptions-item label="补考日期">{{ detailData.examDate }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detailData.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detailData.endTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === '已考' ? 'success' : detailData.status === '待考' ? 'warning' : 'info'" size="small">
            {{ detailData.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <!-- 新增/编辑：表单 -->
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学期" prop="semesterId">
          <el-select v-model="form.semesterId" placeholder="请选择学期" style="width: 100%">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.semesterName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%" filterable>
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试日期" prop="examDate">
          <el-date-picker v-model="form.examDate" type="date" placeholder="请选择考试日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="考试时间" prop="examTime">
          <el-time-picker v-model="form.examTime" placeholder="请选择考试时间" style="width: 100%" value-format="HH:mm" />
        </el-form-item>
        <el-form-item label="考试地点" prop="classroomId">
          <el-select v-model="form.classroomId" placeholder="请选择教室" style="width: 100%" filterable>
            <el-option v-for="r in classroomOptions" :key="r.id" :label="r.roomName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="人数上限" prop="maxStudents">
          <el-input-number v-model="form.maxStudents" :min="1" :max="200" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMakeupExamList, createMakeupExam, updateMakeupExam, deleteMakeupExam, getMakeupExamDetail } from '@/api/makeupExam'
import { getSemesterList } from '@/api/semester'
import { getCourseList } from '@/api/course'
import { getClassroomList } from '@/api/classroom'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增补考')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const semesterOptions = ref([])
const courseOptions = ref([])
const classroomOptions = ref([])
const detailData = reactive({
  id: null, studentName: '', courseName: '', semesterName: '',
  examDate: '', startTime: '', endTime: '', status: '',
  createTime: '', updateTime: ''
})

const queryForm = reactive({ pageNum: 1, pageSize: 10, semesterId: '', courseId: '' })
const form = reactive({ id: null, semesterId: '', courseId: '', examDate: '', examTime: '', classroomId: '', maxStudents: 60 })
const rules = {
  semesterId: [{ required: true, message: '请选择学期', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  examDate: [{ required: true, message: '请选择考试日期', trigger: 'change' }],
  examTime: [{ required: true, message: '请选择考试时间', trigger: 'change' }],
  classroomId: [{ required: true, message: '请选择考试地点', trigger: 'change' }]
}

const loadOptions = async () => {
  const [semesterRes, courseRes, classroomRes] = await Promise.all([
    getSemesterList({ pageNum: 1, pageSize: 100 }),
    getCourseList({ pageNum: 1, pageSize: 200 }),
    getClassroomList({ pageNum: 1, pageSize: 200 })
  ])
  semesterOptions.value = semesterRes.data.list || []
  courseOptions.value = courseRes.data.list || []
  classroomOptions.value = classroomRes.data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMakeupExamList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.semesterId = ''; queryForm.courseId = ''; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增补考'
  Object.assign(form, { id: null, semesterId: '', courseId: '', examDate: '', examTime: '', classroomId: '', maxStudents: 60 })
  dialogVisible.value = true
}

const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '补考详情'
  try {
    const detail = await getMakeupExamDetail(row.id)
    const data = detail.data
    Object.assign(detailData, {
      id: data.id || null,
      studentName: data.studentName || '',
      courseName: data.courseName || '',
      examDate: data.examDate || '',
      startTime: data.startTime || '',
      endTime: data.endTime || '',
      status: data.status || '',
      createTime: data.createTime || '',
      updateTime: data.updateTime || ''
    })
    dialogVisible.value = true
  } catch (err) {
    console.error('获取补考详情失败:', err)
    ElMessage.error('获取补考详情失败')
    dialogVisible.value = false
  }
}

const handleEdit = (row) => { isEdit.value = true; isView.value = false; dialogTitle.value = '编辑补考'; Object.assign(form, { ...row }); dialogVisible.value = true }
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该补考记录吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteMakeupExam(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateMakeupExam(form) : await createMakeupExam(form)
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

onMounted(async () => { await loadOptions(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
