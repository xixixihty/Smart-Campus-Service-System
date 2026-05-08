<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Notebook /></el-icon> 我的课程</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableData" stripe border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column prop="courseCode" label="课程代码" width="110" />
        <el-table-column prop="courseType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.courseType === '必修' ? 'danger' : row.courseType === '选修' ? 'warning' : ''" size="small">
              {{ row.courseType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="credit" label="学分" width="70" align="center" />
        <el-table-column prop="schedule" label="上课时间" width="180" show-overflow-tooltip />
        <el-table-column prop="classroomName" label="上课地点" width="130" />
        <el-table-column prop="semesterName" label="学期" width="180" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDrop(row)">
              <el-icon><Close /></el-icon>退选
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyCourses, dropCourse } from '@/api/courseSelection'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({ pageNum: 1, pageSize: 10 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyCourses(queryForm)
    tableData.value = res.data.list || res.data || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleDrop = (row) => {
  ElMessageBox.confirm(`确定要退选课程"${row.courseName}"吗？`, '提示', { type: 'warning' }).then(async () => {
    await dropCourse(row.id || row.courseId)
    ElMessage.success('退选成功')
    fetchData()
  }).catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
