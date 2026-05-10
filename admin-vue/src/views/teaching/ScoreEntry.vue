<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Tickets /></el-icon> 成绩管理</h2>
      <div class="header-actions">
        <el-button type="success" @click="handleBatch"><el-icon><Upload /></el-icon>批量导入</el-button>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>录入成绩</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="学期">
          <el-select v-model="queryForm.semesterId" placeholder="请选择学期" clearable style="width: 180px">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.semesterName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="queryForm.courseId" placeholder="请选择课程" clearable filterable style="width: 180px">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生">
          <el-select v-model="queryForm.studentId" placeholder="请选择学生" clearable filterable style="width: 180px">
            <el-option v-for="s in studentOptions" :key="s.id" :label="s.name + ' (' + s.studentNo + ')'" :value="s.id" />
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
        <el-table-column prop="studentName" label="学生姓名" width="100" align="center" />
        <el-table-column prop="studentNo" label="学号" width="120" align="center" />
        <el-table-column prop="studentNo" label="学号" width="120" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="120" align="center" />
        <el-table-column prop="semesterName" label="学期名称" width="120" align="center" />
        <el-table-column prop="usualScore" label="平时成绩" width="80" align="center" />
        <el-table-column prop="finalScore" label="期末成绩" width="80" align="center" />
        <el-table-column prop="totalScore" label="总评成绩" width="80" align="center" />
        <el-table-column prop="scorePoint" label="绩点" width="80" align="center" />
        <el-table-column prop="credit" label="学分" width="80" align="center" />
        <el-table-column prop="examStatus" label="考试状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.examStatus === '正常' ? 'success' : row.examStatus === '缺考' ? 'danger' : 'warning'" size="small">
              {{ row.examStatus }}
            </el-tag>
          </template>
        </el-table-column>
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
        <el-descriptions-item label="成绩ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ detailData.courseName }}</el-descriptions-item>
        <el-descriptions-item label="学期名称">{{ detailData.semesterName }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ detailData.credit }}</el-descriptions-item>
        <el-descriptions-item label="平时成绩">{{ detailData.usualScore }}</el-descriptions-item>
        <el-descriptions-item label="期末成绩">{{ detailData.finalScore }}</el-descriptions-item>
        <el-descriptions-item label="总评成绩">{{ detailData.totalScore }}</el-descriptions-item>
        <el-descriptions-item label="绩点">{{ detailData.scorePoint }}</el-descriptions-item>
        <el-descriptions-item label="考试状态">
          <el-tag :type="detailData.examStatus === '正常' ? 'success' : detailData.examStatus === '缺考' ? 'danger' : 'warning'" size="small">
            {{ detailData.examStatus }}
          </el-tag>
        </el-descriptions-item>
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
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="form.studentId" placeholder="请选择学生" style="width: 100%" filterable>
            <el-option v-for="s in studentOptions" :key="s.id" :label="s.name + ' (' + s.studentNo + ')'" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input-number v-model="form.score" :min="0" :max="100" :precision="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchVisible" title="批量导入成绩" width="560px" destroy-on-close>
      <el-upload drag :auto-upload="false" accept=".xlsx,.xls" :on-change="handleFileChange">
        <el-icon :size="48"><UploadFilled /></el-icon>
        <div class="el-upload__text">将Excel文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持 .xlsx / .xls 格式，请按照模板格式填写</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="handleBatchSubmit">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getScoreList, createScore, updateScore, deleteScore, batchScore, getScoreDetail } from '@/api/scoreEntry'
import { getSemesterList } from '@/api/semester'
import { getCourseList } from '@/api/course'
import { getStudentList } from '@/api/student'

const loading = ref(false)
const submitLoading = ref(false)
const batchLoading = ref(false)
const dialogVisible = ref(false)
const batchVisible = ref(false)
const dialogTitle = ref('录入成绩')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const semesterOptions = ref([])
const courseOptions = ref([])
const studentOptions = ref([])
const uploadFile = ref(null)
const detailData = reactive({
  id: null, studentName: '', studentNo: '', courseName: '', semesterName: '',
  credit: 0, usualScore: 0, finalScore: 0, totalScore: 0, scorePoint: 0,
  examStatus: '', createTime: '', updateTime: ''
})

const queryForm = reactive({ pageNum: 1, pageSize: 10, semesterId: '', courseId: '', studentId: '' })
const form = reactive({ id: null, semesterId: '', courseId: '', studentId: '', score: 0 })
const rules = {
  semesterId: [{ required: true, message: '请选择学期', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  score: [{ required: true, message: '请输入成绩', trigger: 'blur' }]
}

const loadOptions = async () => {
  const [semesterRes, courseRes, studentRes] = await Promise.all([
    getSemesterList({ pageNum: 1, pageSize: 100 }),
    getCourseList({ pageNum: 1, pageSize: 200 }),
    getStudentList({ pageNum: 1, pageSize: 500 })
  ])
  semesterOptions.value = semesterRes.data.list || []
  courseOptions.value = courseRes.data.list || []
  studentOptions.value = studentRes.data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getScoreList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.semesterId = ''; queryForm.courseId = ''; queryForm.studentId = ''; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '录入成绩'
  Object.assign(form, { id: null, semesterId: '', courseId: '', studentId: '', score: 0 })
  dialogVisible.value = true
}
const handleEdit = (row) => { isEdit.value = true; isView.value = false; dialogTitle.value = '编辑成绩'; Object.assign(form, { ...row }); dialogVisible.value = true }

const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '成绩详情'
  try {
    const detail = await getScoreDetail(row.id)
    const data = detail.data
    Object.assign(detailData, {
      id: data.id || null,
      studentName: data.studentName || '',
      studentNo: data.studentNo || '',
      courseName: data.courseName || '',
      semesterName: data.semesterName || '',
      credit: data.credit || 0,
      usualScore: data.usualScore || 0,
      finalScore: data.finalScore || 0,
      totalScore: data.totalScore || 0,
      scorePoint: data.scorePoint || 0,
      examStatus: data.examStatus || '',
      createTime: data.createTime || '',
      updateTime: data.updateTime || ''
    })
    dialogVisible.value = true
  } catch (err) {
    console.error('获取成绩详情失败:', err)
    ElMessage.error('获取成绩详情失败')
    dialogVisible.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该成绩记录吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteScore(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateScore(form) : await createScore(form)
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

const handleBatch = () => { batchVisible.value = true }
const handleFileChange = (file) => { uploadFile.value = file.raw }
const handleBatchSubmit = async () => {
  if (!uploadFile.value) { ElMessage.warning('请选择文件'); return }
  batchLoading.value = true
  try {
    await batchScore({ file: uploadFile.value })
    ElMessage.success('批量导入成功')
    batchVisible.value = false; fetchData()
  } finally { batchLoading.value = false }
}

onMounted(async () => { await loadOptions(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.header-actions { display: flex; gap: 8px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
