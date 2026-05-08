<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><User /></el-icon> 学生管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增学生</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="所属学院">
          <el-select v-model="queryForm.collegeId" placeholder="请选择学院" clearable @change="onCollegeChange">
            <el-option v-for="c in collegeOptions" :key="c.id" :label="c.collegeName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属专业">
          <el-select v-model="queryForm.majorId" placeholder="请选择专业" clearable>
            <el-option v-for="m in majorOptions" :key="m.id" :label="m.majorName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属班级">
          <el-select v-model="queryForm.classId" placeholder="请选择班级" clearable>
            <el-option v-for="c in classOptions" :key="c.id" :label="c.className" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model="queryForm.name" placeholder="请输入学生姓名" clearable />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="queryForm.studentNo" placeholder="请输入学号" clearable />
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
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === '男' ? '' : 'danger'" size="small">{{ row.gender }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collegeName" label="所属学院" width="130" />
        <el-table-column prop="majorName" label="所属专业" width="130" />
        <el-table-column prop="className" label="所属班级" width="130" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
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
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入初始密码" show-password />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentList, createStudent, updateStudent, deleteStudent } from '@/api/student'
import { getCollegeList } from '@/api/college'
import { getMajorList } from '@/api/major'
import { getClassList } from '@/api/class'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增学生')
const isEdit = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const collegeOptions = ref([])
const majorOptions = ref([])
const classOptions = ref([])
const formMajorOptions = ref([])
const formClassOptions = ref([])

const queryForm = reactive({ pageNum: 1, pageSize: 10, collegeId: '', majorId: '', classId: '', name: '', studentNo: '' })
const form = reactive({ id: null, studentNo: '', name: '', gender: '男', collegeId: '', majorId: '', classId: '', phone: '', email: '', password: '' })
const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
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
  form.majorId = ''; form.classId = ''
  formMajorOptions.value = await loadMajors(val)
  formClassOptions.value = []
}

const onFormMajorChange = async (val) => {
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
  isEdit.value = false; dialogTitle.value = '新增学生'
  Object.assign(form, { id: null, studentNo: '', name: '', gender: '男', collegeId: '', majorId: '', classId: '', phone: '', email: '', password: '' })
  formMajorOptions.value = []; formClassOptions.value = []
  dialogVisible.value = true
}
const handleEdit = async (row) => {
  isEdit.value = true; dialogTitle.value = '编辑学生'
  formMajorOptions.value = await loadMajors(row.collegeId)
  formClassOptions.value = await loadClasses(row.majorId)
  Object.assign(form, { ...row })
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
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
