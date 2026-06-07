<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><CollectionTag /></el-icon> 图书管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增图书</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="图书分类">
          <el-select v-model="queryForm.categoryId" placeholder="请选择分类" clearable class="search-input">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="书名">
          <el-input v-model="queryForm.title" placeholder="请输入书名" clearable class="search-input" />
        </el-form-item>
        <el-form-item label="ISBN">
          <el-input v-model="queryForm.isbn" placeholder="请输入ISBN" clearable class="search-input" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="queryForm.author" placeholder="请输入作者" clearable class="search-input" />
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
        <el-table-column label="封面" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              :preview-src-list="[row.coverImage]"
              fit="cover"
              style="width: 50px; height: 70px; border-radius: 4px"
            >
              <template #error>
                <div class="image-error">
                  <el-icon :size="20"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-else class="no-cover">
              <el-icon :size="20"><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="isbn" label="ISBN" width="140" align="center" />
        <el-table-column prop="title" label="书名" min-width="180"  align="center" />
        <el-table-column prop="author" label="作者" width="120" align="center" />
        <el-table-column prop="publisher" label="出版社" width="120" align="center" />
        <el-table-column prop="categoryName" label="分类名称" width="100" align="center" />
        <el-table-column prop="availableCopies" label="可借数量" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.availableCopies > 0 ? 'success' : 'danger'" size="small">
              {{ row.availableCopies }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '在库' ? 'success' : row.status === '借出' ? 'warning' : row.status === '维修' ? 'info' : 'danger'" size="small">
              {{ row.status }}
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-descriptions v-if="isView" :column="2" border>
        <el-descriptions-item label="封面图片" :span="2">
          <el-image
            :src="detailData.coverImage || defaultCover"
            :preview-src-list="[detailData.coverImage]"
            fit="contain"
            style="width: 200px; height: 280px; border-radius: 4px"
            :hide-on-click-modal="true"
          >
            <template #error>
              <div class="image-error">
                <el-icon :size="40"><Picture /></el-icon>
                <div style="margin-top: 8px; font-size: 12px">暂无封面</div>
              </div>
            </template>
          </el-image>
        </el-descriptions-item>
        <el-descriptions-item label="图书ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="ISBN">{{ detailData.isbn }}</el-descriptions-item>
        <el-descriptions-item label="书名">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ detailData.author }}</el-descriptions-item>
        <el-descriptions-item label="出版社">{{ detailData.publisher }}</el-descriptions-item>
        <el-descriptions-item label="出版日期">{{ detailData.publishDate }}</el-descriptions-item>
        <el-descriptions-item label="分类名称">{{ detailData.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="分类代码">{{ detailData.categoryCode }}</el-descriptions-item>
        <el-descriptions-item label="总册数">{{ detailData.totalCopies }}</el-descriptions-item>
        <el-descriptions-item label="可借册数">{{ detailData.availableCopies }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === '在库' ? 'success' : detailData.status === '借出' ? 'warning' : detailData.status === '维修' ? 'info' : 'danger'" size="small">
            {{ detailData.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="封面图片">{{ detailData.coverImage || '无' }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ detailData.description || '无' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="书名" prop="title">
              <el-input v-model="form.title" placeholder="请输入书名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="form.isbn" placeholder="请输入ISBN" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="form.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="图书分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="c in categoryOptions" :key="c.id" :label="c.categoryName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版年份" prop="publishYear">
              <el-input-number v-model="form.publishYear" :min="1900" :max="2099" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="总数量" prop="totalCopies">
              <el-input-number v-model="form.totalCopies" :min="1" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可借数量" prop="availableCopies">
              <el-input-number v-model="form.availableCopies" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图书描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入图书描述" />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-upload
            class="cover-uploader"
            action="#"
            :http-request="handleUploadCover"
            :show-file-list="false"
            :before-upload="beforeUploadCover"
            accept="image/jpeg,image/png,image/webp"
          >
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-image" />
            <div v-else class="cover-uploader-trigger">
              <el-icon :size="30"><Plus /></el-icon>
              <div class="upload-text">点击上传</div>
            </div>
          </el-upload>
          <div class="upload-tip">支持 JPG、PNG、WEBP 格式，大小不超过 5MB</div>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookList, createBook, updateBook, deleteBook, getBookDetail, uploadImage } from '@/api/book'
import { getCategoryList } from '@/api/bookCategory'

const defaultCover = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTAiIGhlaWdodD0iNzAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI2Y1ZjdmYSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTAiIGZpbGw9IiM5MDkzOTkiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj5ObyBJbWFnZTwvdGV4dD48L3N2Zz4='

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增图书')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const categoryOptions = ref([])

const detailData = reactive({
  id: null,
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  publishDate: '',
  categoryId: '',
  categoryName: '',
  categoryCode: '',
  totalCopies: 0,
  availableCopies: 0,
  status: '',
  coverImage: '',
  description: '',
  createTime: '',
  updateTime: ''
})

const queryForm = reactive({ pageNum: 1, pageSize: 10, categoryId: '', title: '', isbn: '', author: '' })
const form = reactive({ id: null, title: '', isbn: '', author: '', publisher: '', categoryId: '', publishYear: new Date().getFullYear(), totalCopies: 10, availableCopies: 10, description: '', coverImage: '' })
const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const loadCategories = async () => {
  const res = await getCategoryList({ pageNum: 1, pageSize: 100 })
  categoryOptions.value = res.data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getBookList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
    console.log('图书列表数据:', tableData.value)
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.categoryId = ''; queryForm.title = ''; queryForm.isbn = ''; queryForm.author = ''; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增图书'
  Object.assign(form, { id: null, title: '', isbn: '', author: '', publisher: '', categoryId: '', publishYear: new Date().getFullYear(), totalCopies: 10, availableCopies: 10, description: '', coverImage: '' })
  dialogVisible.value = true
}

const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '图书详情'
  try {
    const detail = await getBookDetail(row.id)
    const data = detail.data
    Object.assign(detailData, {
      id: data.id || null,
      isbn: data.isbn || '',
      title: data.title || '',
      author: data.author || '',
      publisher: data.publisher || '',
      publishDate: data.publishDate || '',
      categoryId: data.categoryId || '',
      categoryName: data.categoryName || '',
      categoryCode: data.categoryCode || '',
      totalCopies: data.totalCopies || 0,
      availableCopies: data.availableCopies || 0,
      status: data.status || '',
      coverImage: data.coverImage || '',
      description: data.description || '',
      createTime: data.createTime || '',
      updateTime: data.updateTime || ''
    })
    dialogVisible.value = true
  } catch (err) {
    console.error('获取图书详情失败:', err)
    ElMessage.error('获取图书详情失败')
    dialogVisible.value = false
  }
}


const handleEdit = (row) => { isEdit.value = true; isView.value = false; dialogTitle.value = '编辑图书'; Object.assign(form, { ...row }); dialogVisible.value = true }
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该图书吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteBook(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updateBook(form) : await createBook(form)
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

const beforeUploadCover = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUploadCover = async (options) => {
  const file = options.file
  try {
    const res = await uploadImage(file)
    form.coverImage = res.data
    ElMessage.success('封面上传成功')
  } catch (err) {
    console.error('封面上传失败:', err)
    ElMessage.error('封面上传失败')
  }
}

onMounted(async () => { await loadCategories(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.image-error { display: flex; justify-content: center; align-items: center; width: 100%; height: 100%; background: #f5f7fa; color: #909399; }
.no-cover { display: flex; justify-content: center; align-items: center; width: 50px; height: 70px; background: #f5f7fa; color: #909399; border-radius: 4px; }
.cover-uploader { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; transition: all 0.3s; }
.cover-uploader:hover { border-color: #409eff; }
.cover-uploader-trigger { width: 150px; height: 200px; display: flex; flex-direction: column; justify-content: center; align-items: center; color: #8c939d; }
.upload-text { margin-top: 8px; font-size: 12px; color: #606266; }
.cover-image { width: 150px; height: 200px; display: block; object-fit: cover; }
.upload-tip { font-size: 12px; color: #909399; margin-top: 8px; }
.search-form { display: flex; flex-wrap: wrap; gap: 0; }
.search-form :deep(.el-form-item) { margin-bottom: 16px; }
.search-input { width: 200px; }
</style>
