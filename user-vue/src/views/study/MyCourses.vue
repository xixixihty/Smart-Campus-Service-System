<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Notebook /></el-icon> 我的课程</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableData" stripe border style="margin-top: 12px;">
        <el-table-column prop="courseId" label="ID" width="70" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column prop="credit" label="学分" width="70" align="center" />
        <el-table-column prop="teacherName" label="授课教师" width="100" align="center" />
        <el-table-column prop="classroomName" label="教室" width="120" align="center" />
        <el-table-column label="时间" width="140" align="center">
          <template #default="{ row }">
            <span v-if="row.weekDay">周{{ ['一','二','三','四','五','六','日'][row.weekDay - 1] }} {{ row.startSection }}-{{ row.endSection }}节</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="weekRange" label="教学周" width="100" align="center" />
        <el-table-column prop="semesterName" label="学期" width="140" align="center" />
        <el-table-column prop="score" label="成绩" width="80" align="center" />
        <el-table-column prop="scorePoint" label="绩点" width="80" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.teacherName"
              type="danger"
              size="small"
              plain
              @click="handleDrop(row)"
            >
              <el-icon><Close /></el-icon>退选
            </el-button>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyAllCourses } from '@/api/courseSchedule'
import { dropCourse } from '@/api/courseSelection'

const loading = ref(false)
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyAllCourses()
    tableData.value = res.data || []
  } finally { loading.value = false }
}

const handleDrop = (row) => {
  ElMessageBox.confirm(`确定要退选课程"${row.courseName}"吗？`, '提示', { type: 'warning' }).then(async () => {
    await dropCourse(row.courseId)
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
.no-action { color: #c0c4cc; font-size: 12px; }
</style>