<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Clock /></el-icon> 选课时段管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增时段</el-button>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="学期">
          <el-select v-model="queryForm.semesterId" placeholder="请选择学期" clearable style="width: 200px">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时段名称">
          <el-input v-model="queryForm.periodName" placeholder="请输入时段名称" clearable style="width: 200px" />
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
        <el-table-column prop="semesterName" label="学期名称" width="180"  align="center" />
        <el-table-column prop="startTime" label="选课开始时间" width="170" align="center" />
        <el-table-column prop="endTime" label="选课结束时间" width="170" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
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
        <el-descriptions-item label="时段ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="所属学期">{{ detailData.semesterName }}</el-descriptions-item>
        <el-descriptions-item label="选课开始时间">{{ detailData.startTime }}</el-descriptions-item>
        <el-descriptions-item label="选课结束时间">{{ detailData.endTime }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <!-- 新增/编辑：表单 -->
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属学期" prop="semesterId">
          <el-select v-model="form.semesterId" placeholder="请选择学期" style="width: 100%">  
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="请选择开始时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="请选择结束时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
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
import { Clock, Plus, Search, Refresh, View, Edit, Delete } from '@element-plus/icons-vue'
import { getPeriodList, createPeriod, updatePeriod, deletePeriod, getPeriodDetail } from '@/api/courseSelectionPeriod'
import { getSemesterList } from '@/api/semester'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增时段')
const isEdit = ref(false)
const isView = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const semesterOptions = ref([])
const detailData = reactive({
  id: null, semesterId: '', semesterName: '', periodName: '',
  startTime: '', endTime: '', maxSelections: 0, createTime: '', updateTime: ''
})

const queryForm = reactive({ pageNum: 1, pageSize: 10, semesterId: '', periodName: '' })
const form = reactive({ id: null, semesterId: '', semesterName: '', periodName: '', startTime: '', endTime: '', maxSelections: 3 })
const rules = {
  semesterId: [{ required: true, message: '请选择学期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const loadSemesters = async () => {
  const res = await getSemesterList({ pageNum: 1, pageSize: 100 })
  semesterOptions.value = res.data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPeriodList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.semesterId = ''; queryForm.periodName = ''; handleSearch() }
const handleAdd = () => {
  isEdit.value = false; isView.value = false; dialogTitle.value = '新增时段'
  Object.assign(form, { id: null, semesterId: '', periodName: '', startTime: '', endTime: ''})
  dialogVisible.value = true
}
const handleEdit = (row) => { isEdit.value = true; isView.value = false; dialogTitle.value = '编辑时段'; Object.assign(form, { ...row }); dialogVisible.value = true }


const handleView = async (row) => {
  isEdit.value = false; isView.value = true; dialogTitle.value = '查看选课时间详情'
  const detail = await getPeriodDetail(row.id)
  const data = detail.data
  Object.assign(detailData, {
    id: data.id || null,
    semesterId: data.semesterId || '',
    semesterName: data.semesterName || '',
    startTime: data.startTime || '',
    endTime: data.endTime || '',
    createTime: data.createTime || '',
    updateTime: data.updateTime || ''
  })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该时段吗？', '提示', { type: 'warning' }).then(async () => {
    await deletePeriod(row.id); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    isEdit.value ? await updatePeriod(form) : await createPeriod(form)
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

onMounted(async () => { await loadSemesters(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
