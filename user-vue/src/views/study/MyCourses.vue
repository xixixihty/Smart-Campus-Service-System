<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Notebook /></el-icon> 我的课程</h2>
      <div class="header-tip">
        <el-tag type="info" size="small">课表</el-tag>
        <span>课表课程</span>
        <el-tag type="success" size="small" style="margin-left: 8px;">选课</el-tag>
        <span>选修课程</span>
        <el-tag type="warning" size="small" style="margin-left: 8px;">课表+选课</el-tag>
        <span>课表中的选修</span>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <div class="search-bar">
        <el-select v-model="filterSource" placeholder="课程来源" clearable style="width: 140px;" @change="handleFilter">
          <el-option label="全部" value="" />
          <el-option label="课表" value="课表" />
          <el-option label="选课" value="选课" />
          <el-option label="课表+选课" value="课表+选课" />
        </el-select>
        <el-select v-model="filterType" placeholder="课程类型" clearable style="width: 120px; margin-left: 8px;" @change="handleFilter">
          <el-option label="全部" value="" />
          <el-option label="必修" value="必修" />
          <el-option label="选修" value="选修" />
          <el-option label="公选" value="公选" />
        </el-select>
      </div>

      <el-table :data="filteredData" stripe border style="margin-top: 12px;">
        <el-table-column prop="courseId" label="ID" width="70" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column prop="credit" label="学分" width="70" align="center" />
        <el-table-column prop="type" label="类型" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === '必修' ? 'danger' : row.type === '选修' ? 'warning' : 'info'" size="small">
              {{ row.type || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="授课教师" width="100" align="center" />
        <el-table-column prop="classroomName" label="教室" width="120" align="center" />
        <el-table-column label="时间" width="140" align="center">
          <template #default="{ row }">
            <span v-if="row.weekDay">周{{ ['一','二','三','四','五','六','日'][row.weekDay - 1] }} {{ row.startSection }}-{{ row.endSection }}节</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="weekRange" label="教学周" width="100" align="center" />
        <el-table-column prop="classNames" label="授课班级" min-width="140" />
        <el-table-column prop="semesterName" label="学期" width="140" align="center" />
        <el-table-column label="来源" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.source === '课表' ? 'info' : row.source === '选课' ? 'success' : 'warning'" size="small">
              {{ row.source }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" width="80" align="center" />
        <el-table-column prop="scorePoint" label="绩点" width="80" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.source === '选课' || row.source === '课表+选课'"
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyAllCourses } from '@/api/courseSchedule'
import { dropCourse } from '@/api/courseSelection'

const loading = ref(false)
const tableData = ref([])
const filterSource = ref('')
const filterType = ref('')

const filteredData = computed(() => {
  return tableData.value.filter((item) => {
    if (filterSource.value && item.source !== filterSource.value) return false
    if (filterType.value && item.type !== filterType.value) return false
    return true
  })
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyAllCourses()
    tableData.value = res.data || []
  } finally { loading.value = false }
}

const handleFilter = () => {}

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
.header-tip { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #909399; }
.search-bar { display: flex; align-items: center; }
.no-action { color: #c0c4cc; font-size: 12px; }
</style>