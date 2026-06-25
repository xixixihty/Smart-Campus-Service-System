<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><FolderOpened /></el-icon> 图书分类管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增分类</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="分类名称">
          <el-input v-model="queryForm.categoryName" placeholder="请输入分类名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe border max-height="calc(100vh - 280px)" scrollbar-always-on>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="categoryName" label="分类名称" min-width="180" align="center" />
        <el-table-column prop="categoryCode" label="分类代码" width="120" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)"><el-icon><Delete /></el-icon>删除</el-button>
          </template>
        </el-table-column>
        <el-table-column width="12" class-name="scroll-hint-column" fixed="right" />
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close @close="isView = false">
      <!-- 查看详情：描述列表 -->
      <el-descriptions v-if="isView" :column="1" border size="default">
        <el-descriptions-item label="分类ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="分类名称">{{ detailData.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="分类代码">{{ detailData.categoryCode }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <!-- 新增/编辑：表单 -->
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类代码" prop="categoryCode">
          <el-input v-model="form.categoryCode" placeholder="请输入分类代码" />
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
import { getCategoryList, createCategory, updateCategory, deleteCategory, getCategoryDetail } from '@/api/bookCategory'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const detailData = reactive({
  id: null, categoryName: '', categoryCode: '', description: '',
  createTime: '', updateTime: ''
})

const queryForm = reactive({ pageNum: 1, pageSize: 10, categoryName: '' })
const form = reactive({ id: null, categoryName: '', categoryCode: '', description: '' })
const rules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  categoryCode: [{ required: true, message: '请输入分类代码', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCategoryList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.categoryName = ''; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增分类'
  Object.assign(form, { id: null, categoryName: '', categoryCode: '', description: '' })
  dialogVisible.value = true
}

const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '图书分类详情'
  try {
    const detail = await getCategoryDetail(row.id)
    const data = detail.data
    Object.assign(detailData, {
      id: data.id || null,
      categoryName: data.categoryName || '',
      categoryCode: data.categoryCode || '',
      createTime: data.createTime || '',
      updateTime: data.updateTime || ''
    })
    dialogVisible.value = true
  } catch (err) {
    console.error('获取图书分类详情失败:', err)
    ElMessage.error('获取图书分类详情失败')
    dialogVisible.value = false
  }
}

const handleEdit = (row) => { isEdit.value = true; isView.value = false; dialogTitle.value = '编辑分类'; Object.assign(form, { ...row }); dialogVisible.value = true }
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该分类吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteCategory(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateCategory(form) : await createCategory(form)
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
