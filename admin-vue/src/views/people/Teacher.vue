<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Avatar /></el-icon> 教师管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增教师</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="所属学院">
          <el-select v-model="queryForm.collegeId" placeholder="请选择学院" clearable style="width: 150px">
            <el-option v-for="c in collegeOptions" :key="c.id" :label="c.collegeName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师姓名">
          <el-input v-model="queryForm.name" placeholder="请输入教师姓名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="queryForm.teacherNo" placeholder="请输入工号" clearable style="width: 200px" />
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
        <el-table-column prop="teacherNo" label="工号" width="120" align="center" />
        <el-table-column prop="name" label="姓名" width="100" align="center" />
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === '男' ? '' : 'danger'" size="small">{{ row.gender }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collegeName" label="所属学院" width="130" align="center" />
        <el-table-column prop="title" label="职称" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center"> 
          <template #default="{ row }">
            <el-tag :type="row.status === '正常' || row.status === '在职' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="300" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-button type="warning" link @click="handleResetPwd(row)"><el-icon><Lock /></el-icon>重置密码</el-button>
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
        <el-descriptions-item label="教师ID">{{ detailData.teacherId }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ detailData.teacherNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          <el-tag :type="detailData.gender === '男' ? '' : 'danger'" size="small">{{ detailData.gender }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属学院">{{ detailData.collegeName }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailData.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === '正常' || detailData.status === '在职' ? 'success' : 'danger'" size="small">{{ detailData.status }}</el-tag>
        </el-descriptions-item>
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
            <el-form-item label="工号" prop="teacherNo">
              <el-input v-model="form.teacherNo" placeholder="请输入工号" :disabled="isEdit" />
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
              <el-select v-model="form.collegeId" placeholder="请选择学院" style="width: 100%">
                <el-option v-for="c in collegeOptions" :key="c.id" :label="c.collegeName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="form.title" placeholder="请选择职称" style="width: 100%">
                <el-option label="教授" value="教授" />
                <el-option label="副教授" value="副教授" />
                <el-option label="讲师" value="讲师" />
                <el-option label="助教" value="助教" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col v-if="isEdit" :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="正常" value="正常" />
                <el-option label="停用" value="停用" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="isEdit" :gutter="16">
          <el-col :span="12">
            <el-form-item label="账号状态" prop="accountStatus">
              <el-select v-model="form.accountStatus" style="width: 100%">
                <el-option label="正常" value="正常" />
                <el-option label="停用" value="停用" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
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
import { getTeacherList, getTeacherDetail, createTeacher, updateTeacher, deleteTeacher, resetTeacherPassword } from '@/api/teacher'
import { getCollegeList } from '@/api/college'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增教师')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const collegeOptions = ref([])

const queryForm = reactive({ pageNum: 1, pageSize: 10, collegeId: '', name: '', teacherNo: '' })
const form = reactive({ id: null, teacherNo: '', name: '', gender: '男', collegeId: '', title: '讲师', phone: '', email: '', password: '', status: '正常', accountStatus: '正常' })
const detailData = reactive({ teacherId: null, teacherNo: '', name: '', gender: '', collegeName: '', title: '', phone: '', email: '', status: '', accountStatus: '', createTime: '', updateTime: '' })
const rules = {
  teacherNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
}

const loadColleges = async () => {
  const res = await getCollegeList({ pageNum: 1, pageSize: 100 })
  collegeOptions.value = res.data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTeacherList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.collegeId = ''; queryForm.name = ''; queryForm.teacherNo = ''; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增教师'
  Object.assign(form, { id: null, teacherNo: '', name: '', gender: '男', collegeId: '', title: '讲师', phone: '', email: '', password: '', status: '正常', accountStatus: '正常' })
  dialogVisible.value = true
}
const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '查看教师详情'
  const detail = await getTeacherDetail(row.id)
  const data = detail.data
  Object.assign(detailData, {
    teacherId: data.teacherId || data.id,
    teacherNo: data.teacherNo || '',
    name: data.name || '',
    gender: data.gender || '',
    collegeName: data.collegeName || '',
    title: data.title || '',
    phone: data.phone || '',
    email: data.email || '',
    status: data.status || '',
    accountStatus: data.accountStatus || '',
    createTime: data.createTime || '',
    updateTime: data.updateTime || ''
  })
  dialogVisible.value = true
}
const handleEdit = async (row) => {
  isEdit.value = true; isView.value = false; dialogTitle.value = '编辑教师'
  const detail = await getTeacherDetail(row.id)
  const data = detail.data
  await nextTick()
  form.id = data.id || data.teacherId
  form.teacherNo = data.teacherNo || ''
  form.name = data.name || ''
  form.gender = data.gender || '男'
  form.collegeId = data.collegeId || ''
  form.title = data.title || '讲师'
  form.phone = data.phone || ''
  form.email = data.email || ''
  form.status = data.status || '正常'
  form.accountStatus = data.accountStatus || '正常'
  form.password = ''
  await nextTick()
  dialogVisible.value = true
}
const handleResetPwd = (row) => {
  ElMessageBox.confirm(`确定要重置教师 ${row.name} 的密码吗？`, '提示', { type: 'warning' }).then(async () => {
    await resetTeacherPassword(row.id); ElMessage.success('密码重置成功')
  }).catch(() => {})
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该教师吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteTeacher(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateTeacher(form) : await createTeacher(form)
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
