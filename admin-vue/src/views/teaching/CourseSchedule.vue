<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Timer /></el-icon> 排课管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增排课</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
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
        <el-form-item label="教师">
          <el-select v-model="queryForm.teacherId" placeholder="请选择教师" clearable filterable style="width: 180px">
            <el-option v-for="t in teacherOptions" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="星期">
          <el-select v-model="queryForm.weekDay" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="(d, i) in ['一','二','三','四','五','六','日']" :key="i + 1" :label="'星期' + d" :value="i + 1" />
          </el-select>
        </el-form-item>
        <el-form-item label="周次">
          <el-select v-model="queryForm.weekNum" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="w in 20" :key="w" :label="'第' + w + '周'" :value="w" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe border max-height="calc(100vh - 280px)">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" align="center" />
        <el-table-column prop="semesterName" label="学期名称" width="130" align="center" />
        <el-table-column prop="teacherName" label="授课教师" width="100" align="center" />
        <el-table-column prop="classroomName" label="教室" width="130" align="center" />
        <el-table-column prop="classNames" label="授课班级名称" width="150" />
        <el-table-column prop="weekDay" label="星期几" width="80" align="center">
          <template #default="{ row }">
            {{ ['一', '二', '三', '四', '五', '六', '日'][row.weekDay - 1] || row.weekDay }}
          </template>
        </el-table-column>
        <el-table-column prop="startSection" label="开始节次" width="90" align="center" />
        <el-table-column prop="endSection" label="结束节次" width="90" align="center" />
        <el-table-column prop="weekRange" label="周次范围" width="100" align="center" />
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
    <!-- 排课详情弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-descriptions v-if="isView" :column="2" border size="default">
        <el-descriptions-item label="排课ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="学期名称">{{ detailData.semesterName }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ detailData.courseName }}</el-descriptions-item>
        <el-descriptions-item label="授课教师姓名">{{ detailData.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="教室名称">{{ detailData.classroomName }}</el-descriptions-item>
        <el-descriptions-item label="星期">
          {{ ['一', '二', '三', '四', '五', '六', '日'][detailData.weekDay - 1] || detailData.weekDay }}
        </el-descriptions-item>
        <el-descriptions-item label="开始节次">{{ detailData.startSection }}</el-descriptions-item>
        <el-descriptions-item label="结束节次">{{ detailData.endSection }}</el-descriptions-item>
        <el-descriptions-item label="周次范围">{{ detailData.weekRange }}</el-descriptions-item>
        <el-descriptions-item label="授课班级名称列表">{{ detailData.classNames }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学期" prop="semesterId">
              <el-select v-model="form.semesterId" placeholder="请选择学期" style="width: 100%">
                <el-option v-for="s in semesterOptions" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程" prop="courseId">
              <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%" filterable>
                <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="授课教师" prop="teacherId">
              <el-select v-model="form.teacherId" placeholder="请选择教师" style="width: 100%" filterable>
                <el-option v-for="t in teacherOptions" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="教室" prop="classroomId">
              <el-select v-model="form.classroomId" placeholder="请选择教室" style="width: 100%" filterable>
                <el-option v-for="r in classroomOptions" :key="r.id" :label="r.building + r.roomNumber" :value="r.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="星期" prop="weekDay">
              <el-select v-model="form.weekDay" style="width: 100%">
                <el-option v-for="(d, i) in ['一', '二', '三', '四', '五']" :key="i+1" :label="'星期' + d" :value="i + 1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开始节次" prop="startSection">
              <el-input-number v-model="form.startSection" :min="1" :max="12" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束节次" prop="endSection">
              <el-input-number v-model="form.endSection" :min="1" :max="12" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="周次范围" prop="weekRange">
          <el-input v-model="form.weekRange" placeholder="如：1-16" />
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
import { Timer, Plus, Search, Refresh, View, Edit, Delete } from '@element-plus/icons-vue'
import { getCourseScheduleList, createCourseSchedule, updateCourseSchedule, deleteCourseSchedule, getCourseScheduleDetail } from '@/api/courseSchedule'
import { getSemesterList } from '@/api/semester'
import { getCourseList } from '@/api/course'
import { getTeacherList } from '@/api/teacher'
import { getClassroomList } from '@/api/classroom'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增排课')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const semesterOptions = ref([])
const courseOptions = ref([])
const teacherOptions = ref([])
const classroomOptions = ref([])

const queryForm = reactive({ pageNum: 1, pageSize: 10, semesterId: '', courseId: '', teacherId: '', weekDay: '', weekNum: '' })
const form = reactive({ id: null, semesterId: '', courseId: '', teacherId: '', classroomId: '', weekDay: 1, startSection: 1, endSection: 2, weekRange: '1-16' })
const detailData = reactive({ id: null, semesterName: '', courseName: '', teacherName: '', classroomName: '', weekDay: null, startSection: null, endSection: null, weekRange: '', classNames: '', createTime: '', updateTime: '' })
const rules = {
  semesterId: [{ required: true, message: '请选择学期', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  classroomId: [{ required: true, message: '请选择教室', trigger: 'change' }]
}

const loadOptions = async () => {
  const [semesterRes, courseRes, teacherRes, classroomRes] = await Promise.all([
    getSemesterList({ pageNum: 1, pageSize: 100 }),
    getCourseList({ pageNum: 1, pageSize: 200 }),
    getTeacherList({ pageNum: 1, pageSize: 200 }),
    getClassroomList({ pageNum: 1, pageSize: 200 })
  ])
  semesterOptions.value = semesterRes.data.list || []
  courseOptions.value = courseRes.data.list || []
  teacherOptions.value = teacherRes.data.list || []
  classroomOptions.value = classroomRes.data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCourseScheduleList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.semesterId = ''; queryForm.courseId = ''; queryForm.teacherId = ''; queryForm.weekDay = ''; queryForm.weekNum = ''; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增排课'
  Object.assign(form, { id: null, semesterId: '', courseId: '', teacherId: '', classroomId: '', weekDay: 1, startSection: 1, endSection: 2, weekRange: '1-16' })
  dialogVisible.value = true
}

const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '查看排课详情'
  try {
    const detail = await getCourseScheduleDetail(row.id)
    const data = detail.data
    Object.assign(detailData, {
      id: data.id,
      semesterName: data.semesterName || '',
      courseName: data.courseName || '',
      teacherName: data.teacherName || '',
      classroomName: data.classroomName || '',
      weekDay: data.weekDay,
      startSection: data.startSection,
      endSection: data.endSection,
      weekRange: data.weekRange || '',
      classNames: Array.isArray(data.classNames) ? data.classNames.join('、') : (data.classNames || ''),
      createTime: data.createTime || '',
      updateTime: data.updateTime || ''
    })
    dialogVisible.value = true
  } catch (err) {
    console.error('获取排课详情失败:', err)
    ElMessage.error('获取排课详情失败')
    dialogVisible.value = false
  }
}

const handleEdit = (row) => {
  isEdit.value = true; isView.value = false; dialogTitle.value = '编辑排课'
  Object.assign(form, { ...row }); dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该排课记录吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteCourseSchedule(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateCourseSchedule(form) : await createCourseSchedule(form)
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
