<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><User /></el-icon> 学生管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增学生</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="所属学院">
          <el-select v-model="queryForm.collegeId" placeholder="请选择学院" clearable style="width: 150px" @change="onCollegeChange">
            <el-option v-for="c in collegeOptions" :key="c.id" :label="c.collegeName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属专业">
          <el-select v-model="queryForm.majorId" placeholder="请选择专业" clearable style="width: 150px">
            <el-option v-for="m in majorOptions" :key="m.id" :label="m.majorName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属班级">
          <el-select v-model="queryForm.classId" placeholder="请选择班级" clearable style="width: 150px">
            <el-option v-for="c in classOptions" :key="c.id" :label="c.className" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model="queryForm.name" placeholder="请输入学生姓名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="queryForm.studentNo" placeholder="请输入学号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="studentNo" label="学号" width="130" align="center" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === '男' ? '' : 'danger'" size="small">{{ row.gender }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="所属班级" min-width="160" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center"> 
          <template #default="{ row }">
            <el-tag :type="row.status === '正常' || row.status === '在读' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accountStatus" label="账号状态" width="100" align="center"> 
          <template #default="{ row }">
            <el-tag :type="row.accountStatus === '正常' ? 'success' : 'danger'" size="small">{{ row.accountStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close>
      <!-- 查看详情：描述列表 -->
      <el-descriptions v-if="isView" :column="2" border size="default">
        <el-descriptions-item label="学生ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          <el-tag :type="detailData.gender === '男' ? '' : 'danger'" size="small">{{ detailData.gender }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属班级">{{ detailData.className }}</el-descriptions-item>
        <el-descriptions-item label="入学日期">{{ detailData.enrollmentDate }}</el-descriptions-item>
        <el-descriptions-item label="学籍状态">
          <el-tag :type="detailData.status === '正常' || detailData.status === '在读' ? 'success' : 'danger'" size="small">{{ detailData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detailData.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="detailData.accountStatus === '正常' ? 'success' : 'warning'" size="small">{{ detailData.accountStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <!-- 新增/编辑：表单 -->
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" placeholder="请输入学号" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属学院" prop="collegeId">
              <el-select v-model="form.collegeId" placeholder="请选择学院" style="width: 100%" @change="onFormCollegeChange">
                <el-option v-for="c in collegeOptions" :key="c.id" :label="c.collegeName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属专业" prop="majorId">
              <el-select v-model="form.majorId" placeholder="请选择专业" style="width: 100%" @change="onFormMajorChange">
                <el-option v-for="m in formMajorOptions" :key="m.id" :label="m.majorName" :value="m.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属班级" prop="classId">
              <el-select v-model="form.classId" placeholder="请选择班级" style="width: 100%">
                <el-option v-for="c in formClassOptions" :key="c.id" :label="c.className" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="isEdit" :gutter="16">
          <el-col :span="12">
            <el-form-item label="学籍状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="正常" value="正常" />
                <el-option label="休学" value="休学" />
                <el-option label="退学" value="退学" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账号状态" prop="accountStatus">
              <el-select v-model="form.accountStatus" style="width: 100%">
                <el-option label="正常" value="正常" />
                <el-option label="停用" value="停用" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="!isEdit" label="入学日期" prop="enrollmentDate">
          <el-date-picker v-model="form.enrollmentDate" type="date" placeholder="请选择入学日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入初始密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentList, getStudentDetail, createStudent, updateStudent, deleteStudent } from '@/api/student'
import { getCollegeList } from '@/api/college'
import { getMajorList } from '@/api/major'
import { getClassList } from '@/api/class'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增学生')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const collegeOptions = ref([])
const majorOptions = ref([])
const classOptions = ref([])
const formMajorOptions = ref([])
const formClassOptions = ref([])
const isEchoing = ref(false)

const queryForm = reactive({ pageNum: 1, pageSize: 10, collegeId: '', majorId: '', classId: '', name: '', studentNo: '' })
const form = reactive({ id: null, studentNo: '', name: '', gender: '男', collegeId: '', majorId: '', classId: '', phone: '', idCard: '', enrollmentDate: '', password: '', status: '正常', accountStatus: '正常' })
const detailData = reactive({ id: null, studentNo: '', name: '', gender: '', className: '', enrollmentDate: '', status: '', idCard: '', phone: '', accountStatus: '', createTime: '', updateTime: '' })
const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  enrollmentDate: [{ required: true, message: '请选择入学日期', trigger: 'change' }]
}

const loadColleges = async () => {
  const res = await getCollegeList({ pageNum: 1, pageSize: 100 })
  collegeOptions.value = res.data.list || []
}

const loadMajors = async (collegeId) => {
  const res = await getMajorList({ pageNum: 1, pageSize: 100, collegeId: collegeId || '' })
  return res.data.list || []
}

const loadClasses = async (majorId) => {
  const res = await getClassList({ pageNum: 1, pageSize: 100, majorId: majorId || '' })
  return res.data.list || []
}

const onCollegeChange = async (val) => {
  queryForm.majorId = ''; queryForm.classId = ''
  majorOptions.value = await loadMajors(val)
  classOptions.value = []
}

const onFormCollegeChange = async (val) => {
  if (isEchoing.value) return
  form.majorId = ''; form.classId = ''
  formMajorOptions.value = await loadMajors(val)
  formClassOptions.value = []
}

const onFormMajorChange = async (val) => {
  if (isEchoing.value) return
  form.classId = ''
  formClassOptions.value = await loadClasses(val)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudentList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.collegeId = ''; queryForm.majorId = ''; queryForm.classId = ''; queryForm.name = ''; queryForm.studentNo = ''; majorOptions.value = []; classOptions.value = []; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增学生'
  Object.assign(form, { id: null, studentNo: '', name: '', gender: '男', collegeId: '', majorId: '', classId: '', phone: '', idCard: '', enrollmentDate: '', password: '', status: '正常', accountStatus: '正常' })
  formMajorOptions.value = []; formClassOptions.value = []
  dialogVisible.value = true
}
const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '查看学生详情'
  const detail = await getStudentDetail(row.id)
  const data = detail.data
  Object.assign(detailData, {
    id: data.id,
    studentNo: data.studentNo || '',
    name: data.name || '',
    gender: data.gender || '',
    className: data.className || '',
    enrollmentDate: data.enrollmentDate || '',
    status: data.status || '',
    idCard: data.idCard || '',
    phone: data.phone || '',
    accountStatus: data.accountStatus || '',
    createTime: data.createTime || '',
    updateTime: data.updateTime || ''
  })
  dialogVisible.value = true
}
const handleEdit = async (row) => {
  isEdit.value = true; isView.value = false; dialogTitle.value = '编辑学生'
  const detail = await getStudentDetail(row.id)
  const data = detail.data
  const classId = data.classId
  let collegeId = ''
  let majorId = ''
  if (classId) {
    const allClasses = await getClassList({ pageNum: 1, pageSize: 1000 })
    const foundClass = (allClasses.data.list || []).find(c => c.id === classId)
    if (foundClass && foundClass.majorId) {
      majorId = foundClass.majorId
      const allMajors = await getMajorList({ pageNum: 1, pageSize: 1000 })
      const foundMajor = (allMajors.data.list || []).find(m => m.id === majorId)
      if (foundMajor && foundMajor.collegeId) {
        collegeId = foundMajor.collegeId
      }
    }
  }
  isEchoing.value = true
  if (collegeId) {
    formMajorOptions.value = await loadMajors(collegeId)
  }
  if (majorId) {
    formClassOptions.value = await loadClasses(majorId)
  }
  isEchoing.value = false
  await nextTick()
  form.id = data.id
  form.studentNo = data.studentNo || ''
  form.name = data.name || ''
  form.gender = data.gender || '男'
  form.collegeId = collegeId
  form.majorId = majorId
  form.classId = data.classId || ''
  form.phone = data.phone || ''
  form.idCard = data.idCard || ''
  form.status = data.status || '正常'
  form.accountStatus = data.accountStatus || '正常'
  form.password = ''
  await nextTick()
  dialogVisible.value = true
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该学生吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteStudent(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateStudent(form) : await createStudent(form)
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

onMounted(async () => { await loadColleges(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.search-form { margin-bottom: 0; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
