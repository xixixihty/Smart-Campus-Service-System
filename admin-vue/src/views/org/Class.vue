<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Grid /></el-icon> 班级管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增班级</el-button>
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
        <el-form-item label="班级名称">
          <el-input v-model="queryForm.className" placeholder="请输入班级名称" clearable style="width: 200px" />
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
        <el-table-column prop="className" label="班级名称" min-width="150" align="center" />
        <el-table-column prop="majorName" label="所属专业" width="130" align="center"/>
        <el-table-column prop="headTeacherName" label="班主任姓名" width="130" align="center"/>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '在读' ? 'success' : 'info'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <!-- 查看详情：描述列表 -->
      <el-descriptions v-if="isView" :column="2" border size="default">
        <el-descriptions-item label="班级ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="班级名称">{{ detailData.className }}</el-descriptions-item>
        <el-descriptions-item label="所属专业">{{ detailData.majorName }}</el-descriptions-item>
        <el-descriptions-item label="班主任">{{ detailData.headTeacherName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === '在读' ? 'success' : 'info'" size="small">{{ detailData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <!-- 新增/编辑：表单 -->
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择学院" style="width: 100%" @change="onFormCollegeChange">
            <el-option v-for="c in collegeOptions" :key="c.id" :label="c.collegeName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属专业" prop="majorId">
          <el-select v-model="form.majorId" placeholder="请选择专业" style="width: 100%">
            <el-option v-for="m in formMajorOptions" :key="m.id" :label="m.majorName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="form.className" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="班主任ID" prop="headTeacherId">
          <el-input-number v-model="form.headTeacherId" :min="1" placeholder="请输入班主任ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="在读">在读</el-radio>
            <el-radio value="毕业">毕业</el-radio>
          </el-radio-group>
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
import { getClassList, getClassDetail, createClass, updateClass, deleteClass } from '@/api/class'
import { getCollegeList } from '@/api/college'
import { getMajorList } from '@/api/major'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增班级')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const collegeOptions = ref([])
const majorOptions = ref([])
const formMajorOptions = ref([])
const isEchoing = ref(false)

const queryForm = reactive({ pageNum: 1, pageSize: 10, collegeId: '', majorId: '', className: '' })
const form = reactive({ id: null, collegeId: '', majorId: '', className: '', headTeacherId: null, status: '在读' })
const detailData = reactive({ id: null, className: '', majorName: '', headTeacherName: '', status: '', createTime: '', updateTime: '' })
const rules = {
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }]
}

const loadColleges = async () => {
  const res = await getCollegeList({ pageNum: 1, pageSize: 100 })
  collegeOptions.value = res.data.list || []
}

const loadMajors = async (collegeId) => {
  const res = await getMajorList({ pageNum: 1, pageSize: 100, collegeId: collegeId || '' })
  return res.data.list || []
}

const onCollegeChange = async (val) => {
  queryForm.majorId = ''
  majorOptions.value = await loadMajors(val)
}

const onFormCollegeChange = async (val) => {
  if (isEchoing.value) return
  form.majorId = ''
  formMajorOptions.value = await loadMajors(val)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getClassList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.collegeId = ''; queryForm.majorId = ''; queryForm.className = ''; majorOptions.value = []; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增班级'
  Object.assign(form, { id: null, collegeId: '', majorId: '', className: '', headTeacherId: null, status: '在读' })
  formMajorOptions.value = []
  dialogVisible.value = true
}
const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '查看班级详情'
  const detail = await getClassDetail(row.id)
  const data = detail.data
  Object.assign(detailData, {
    id: data.id,
    className: data.className || '',
    majorName: data.majorName || '',
    headTeacherName: data.headTeacherName || '',
    status: data.status || '',
    createTime: data.createTime || '',
    updateTime: data.updateTime || ''
  })
  dialogVisible.value = true
}
const handleEdit = async (row) => {
  isEdit.value = true; isView.value = false; dialogTitle.value = '编辑班级'
  const detail = await getClassDetail(row.id)
  const data = detail.data
  isEchoing.value = true
  formMajorOptions.value = await loadMajors(data.collegeId)
  isEchoing.value = false
  await nextTick()
  form.id = data.id
  form.collegeId = data.collegeId
  form.majorId = data.majorId
  form.className = data.className || ''
  form.headTeacherId = data.headTeacherId
  form.status = data.status || '在读'
  await nextTick()
  dialogVisible.value = true
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该班级吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteClass(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateClass(form) : await createClass(form)
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
