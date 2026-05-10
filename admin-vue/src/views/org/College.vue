<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><School /></el-icon> 学院管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增学院</el-button>
    </div>
    <!-- 搜索表单中的状态字段，需要显示内容-->
    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="学院名称">
          <el-input v-model="queryForm.collegeName" placeholder="请输入学院名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
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
        <el-table-column prop="collegeName" label="学院名称" min-width="150" align="center"/>
        <el-table-column prop="collegeCode" label="学院代码" width="120" align="center"/>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '启用' ? 'success' : 'danger'" size="small">
              {{ row.status === '启用' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)"><el-icon><Delete /></el-icon>删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" :disabled="isView">
        <el-form-item label="学院名称" prop="collegeName">
          <el-input v-model="form.collegeName" placeholder="请输入学院名称" />
        </el-form-item>
        <el-form-item label="学院代码" prop="collegeCode">
          <el-input v-model="form.collegeCode" placeholder="请输入学院代码" />
        </el-form-item>
        <el-form-item label="院长姓名" prop="dean">
          <el-input v-model="form.dean" placeholder="请输入院长姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
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
import { getCollegeList, getCollegeDetail, createCollege, updateCollege, deleteCollege } from '@/api/college'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增学院')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  collegeName: '',
  status: ''
})

const form = reactive({
  id: null,
  collegeName: '',
  collegeCode: '',
  dean: '',
  contactPhone: '',
  status: '启用'
})

const rules = {
  collegeName: [{ required: true, message: '请输入学院名称', trigger: 'blur' }],
  collegeCode: [{ required: true, message: '请输入学院代码', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCollegeList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  fetchData()
}

const handleReset = () => {
  queryForm.collegeName = ''
  queryForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  isView.value = false
  dialogTitle.value = '新增学院'
  Object.assign(form, { id: null, collegeName: '', collegeCode: '', dean: '', contactPhone: '', status: '启用' })
  dialogVisible.value = true
}

const handleView = async (row) => {
  isEdit.value = false
  isView.value = true
  dialogTitle.value = '查看学院详情'
  const detail = await getCollegeDetail(row.id)
  const data = detail.data
  await nextTick()
  Object.assign(form, {
    id: data.id,
    collegeName: data.collegeName,
    collegeCode: data.collegeCode,
    dean: data.dean || '',
    contactPhone: data.contactPhone || '',
    status: data.status
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  isView.value = false
  dialogTitle.value = '编辑学院'
  const detail = await getCollegeDetail(row.id)
  const data = detail.data
  await nextTick()
  Object.assign(form, {
    id: data.id,
    collegeName: data.collegeName,
    collegeCode: data.collegeCode,
    dean: data.dean || '',
    contactPhone: data.contactPhone || '',
    status: data.status
  })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该学院吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteCollege(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCollege(form)
      ElMessage.success('更新成功')
    } else {
      await createCollege(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchData)
</script>
<!-- 字体需要居中显示-->
<style scoped>
.page-container { 
  padding: 0; 
}
.page-header { 
  display: flex; 
  align-items: center; 
  margin-bottom: 16px; 
}
.page-header h2 { 
  flex: 1;
  margin: 0;
  font-size: 20px; 
  display: flex; 
  align-items: center; 
  gap: 8px; 
  color: #303133; 
}
.search-form { 
  margin-bottom: 0;  
}
.pagination { 
  display: flex; 
  justify-content: flex-end; 
  margin-top: 16px; 
}
</style>
