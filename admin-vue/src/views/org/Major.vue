<template>
  <div class="page-container">
    <div class="page-header">
      <!-- 专业管理字体需要居中显示-->
      <h2><el-icon><Collection /></el-icon> 专业管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增专业</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="所属学院">
          <el-select v-model="queryForm.collegeId" placeholder="请选择学院" clearable style="width: 150px">
            <el-option v-for="c in collegeOptions" :key="c.id" :label="c.collegeName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业名称">
          <el-input v-model="queryForm.majorName" placeholder="请输入专业名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="启用" value="启用" />
            <el-option label="禁用" value="禁用" />
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
        <el-table-column prop="majorName" label="专业名称" min-width="150" align="center" />
        <el-table-column prop="majorCode" label="专业代码" width="120" align="center" />
        <el-table-column prop="collegeName" label="所属学院" width="150" />
        <el-table-column prop="studyYears" label="学制(年)" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '启用' ? 'success' : 'danger'" size="small">
              {{ row.status === '启用' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link @click="handleView(row)"><el-icon><View /></el-icon>查看</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" :disabled="isView">
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择学院" style="width: 100%">
            <el-option v-for="c in collegeOptions" :key="c.id" :label="c.collegeName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业名称" prop="majorName">
          <el-input v-model="form.majorName" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="专业代码" prop="majorCode">
          <el-input v-model="form.majorCode" placeholder="请输入专业代码" />
        </el-form-item>
        <el-form-item label="学制(年)" prop="studyYears">
          <el-input-number v-model="form.studyYears" :min="2" :max="8" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="启用">启用</el-radio>
            <el-radio value="禁用">禁用</el-radio>
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
import { getMajorList, getMajorDetail, createMajor, updateMajor, deleteMajor } from '@/api/major'
import { getCollegeList } from '@/api/college'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增专业')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const collegeOptions = ref([])

const queryForm = reactive({ pageNum: 1, pageSize: 10, collegeId: '', majorName: '', status: '' })
const form = reactive({
  id: null,
  collegeId: '',
  majorName: '',
  majorCode: '',
  studyYears: 4,
  status: '启用'
})
const rules = {
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }],
  majorName: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  majorCode: [{ required: true, message: '请输入专业代码', trigger: 'blur' }]
}

const loadColleges = async () => {
  const res = await getCollegeList({ pageNum: 1, pageSize: 100 })
  collegeOptions.value = res.data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMajorList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.collegeId = ''; queryForm.majorName = ''; queryForm.status = ''; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增专业'
  Object.assign(form, { id: null, collegeId: '', majorName: '', majorCode: '', studyYears: 4, status: '启用' })
  dialogVisible.value = true
}
const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '查看专业详情'
  const detail = await getMajorDetail(row.id)
  const data = detail.data
  await nextTick()
  Object.assign(form, {
    id: data.id,
    collegeId: data.collegeId,
    majorName: data.majorName,
    majorCode: data.majorCode,
    studyYears: data.studyYears,
    status: data.status
  })
  dialogVisible.value = true
}
const handleEdit = async (row) => {
  isEdit.value = true; isView.value = false; dialogTitle.value = '编辑专业'
  const detail = await getMajorDetail(row.id)
  const data = detail.data
  await nextTick()
  Object.assign(form, {
    id: data.id,
    collegeId: data.collegeId,
    majorName: data.majorName,
    majorCode: data.majorCode,
    studyYears: data.studyYears,
    status: data.status
  })
  dialogVisible.value = true
}
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该专业吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteMajor(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateMajor(form) : await createMajor(form)
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

onMounted(() => { loadColleges(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.search-form { margin-bottom: 0; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
