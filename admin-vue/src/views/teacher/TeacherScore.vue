<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Tickets /></el-icon> 成绩录入</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>录入成绩</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="学期">
          <el-select v-model="queryForm.semesterId" placeholder="请选择学期" clearable style="width: 180px">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="queryForm.courseId" placeholder="请选择课程" clearable filterable style="width: 180px">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生">
          <el-input v-model="queryForm.studentName" placeholder="学生姓名" clearable style="width: 150px" />
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
          <el-table-column prop="courseName" label="课程名称" min-width="120" align="center" />
          <el-table-column prop="semesterName" label="学期" width="130" align="center" />
          <el-table-column prop="score" label="成绩" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getScoreType(row.score)">{{ row.score ?? '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="examType" label="考试类型" width="100" align="center" />
          <el-table-column prop="createTime" label="录入时间" width="170" align="center" />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleEdit(row)"><el-icon><Edit /></el-icon>编辑</el-button>
              <el-button type="success" link @click="handleMakeup(row)"><el-icon><EditPen /></el-icon>补考</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="form.studentId" placeholder="请选择学生" filterable style="width: 100%" :disabled="isEdit">
            <el-option v-for="s in studentOptions" :key="s.id" :label="s.name + ' (' + s.studentNo + ')'" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" filterable style="width: 100%" :disabled="isEdit">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semesterId">
          <el-select v-model="form.semesterId" placeholder="请选择学期" style="width: 100%" :disabled="isEdit">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input-number v-model="form.score" :min="0" :max="100" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="考试类型" prop="examType">
          <el-select v-model="form.examType" placeholder="请选择考试类型" style="width: 100%">
            <el-option label="正考" value="正考" />
            <el-option label="补考" value="补考" />
            <el-option label="缓考" value="缓考" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTeacherScoreList, createTeacherScore, updateTeacherScore } from '@/api/teacher'
import { getTeacherCourses } from '@/api/teacher'
import { getSemesterList } from '@/api/semester'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const semesterOptions = ref([])
const courseOptions = ref([])
const studentOptions = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const queryForm = reactive({ semesterId: '', courseId: '', studentName: '', pageNum: 1, pageSize: 10 })

const form = reactive({ id: '', studentId: '', courseId: '', semesterId: '', score: null, examType: '正考' })

const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  semesterId: [{ required: true, message: '请选择学期', trigger: 'change' }],
  score: [{ required: true, message: '请输入成绩', trigger: 'blur' }],
  examType: [{ required: true, message: '请选择考试类型', trigger: 'change' }]
}

const getScoreType = (score) => {
  if (score == null) return 'info'
  if (score >= 90) return 'success'
  if (score >= 60) return ''
  return 'danger'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTeacherScoreList(queryForm)
    tableData.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载成绩列表失败:', e)
  } finally {
    loading.value = false
  }
}

const loadCourses = async () => {
  try {
    const res = await getTeacherCourses()
    courseOptions.value = res.data || []
  } catch (e) { console.error('加载课程失败:', e) }
}

const loadSemesters = async () => {
  try {
    const res = await getSemesterList({ pageNum: 1, pageSize: 100 })
    semesterOptions.value = res.data.list || []
  } catch (e) { console.error('加载学期列表失败:', e) }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { Object.assign(queryForm, { semesterId: '', courseId: '', studentName: '', pageNum: 1, pageSize: 10 }); fetchData() }

const handleAdd = () => { isEdit.value = false; dialogTitle.value = '录入成绩'; resetForm(); dialogVisible.value = true }
const handleEdit = (row) => {
  isEdit.value = true; dialogTitle.value = '编辑成绩'
  Object.assign(form, { id: row.id, studentId: row.studentId, courseId: row.courseId,
    semesterId: row.semesterId, score: row.score, examType: row.examType })
  dialogVisible.value = true
}
const handleMakeup = (row) => {
  isEdit.value = false; dialogTitle.value = '录入补考成绩'
  Object.assign(form, { id: '', studentId: row.studentId, courseId: row.courseId,
    semesterId: row.semesterId, score: null, examType: '补考' })
  dialogVisible.value = true
}

const resetForm = () => { Object.assign(form, { id: '', studentId: '', courseId: '', semesterId: '', score: null, examType: '正考' }) }

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateTeacherScore(form)
      ElMessage.success('成绩修改成功')
    } else {
      await createTeacherScore(form)
      ElMessage.success('成绩录入成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => { loadSemesters(); fetchData(); loadCourses() })
</script>