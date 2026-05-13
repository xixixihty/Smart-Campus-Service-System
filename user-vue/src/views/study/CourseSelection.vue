<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Plus /></el-icon> 选课中心</h2>
      <div class="header-actions">
        <el-tag type="warning" size="large">
          <el-icon><Clock /></el-icon> 选课时段：{{ periodInfo }}
        </el-tag>
      </div>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="课程名称">
          <el-input v-model="queryForm.courseName" placeholder="请输入课程名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="queryForm.courseType" placeholder="请选择类型" clearable style="width: 200px">
            <el-option label="必修" value="必修" />
            <el-option label="选修" value="选修" />
            <el-option label="公选" value="公选" />
          </el-select>
        </el-form-item>
        <el-form-item label="开课学院">
          <el-select v-model="queryForm.collegeId" placeholder="请选择学院" clearable style="width: 200px">
            <el-option v-for="c in collegeOptions" :key="c.id" :label="c.collegeName" :value="c.id" />
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
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column prop="courseCode" label="课程代码" width="110" />
        <el-table-column prop="collegeName" label="开课学院" width="120" />
        <el-table-column prop="courseType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.courseType === '必修' ? 'danger' : row.courseType === '选修' ? 'warning' : ''" size="small">
              {{ row.courseType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="credit" label="学分" width="70" align="center" />
        <el-table-column prop="schedule" label="上课时间" width="160" show-overflow-tooltip />
        <el-table-column label="容量" width="120" align="center">
          <template #default="{ row }">
            <el-progress :percentage="Math.round((row.selectedCount / row.maxCapacity) * 100)" :stroke-width="8"
              :color="row.selectedCount >= row.maxCapacity ? '#F56C6C' : '#67C23A'" />
            <span style="font-size: 12px; color: #909399">{{ row.selectedCount }}/{{ row.maxCapacity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.selected ? 'danger' : 'primary'"
              size="small"
              :disabled="row.selectedCount >= row.maxCapacity && !row.selected"
              @click="row.selected ? handleDrop(row) : handleSelect(row)"
            >
              {{ row.selected ? '退选' : '选课' }}
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
import { getAvailableCourses, selectCourse, dropCourse, getCurrentPeriod } from '@/api/courseSelection'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const collegeOptions = ref([])
const periodInfo = ref()

const queryForm = reactive({ pageNum: 1, pageSize: 10, courseName: '', courseType: '', collegeId: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAvailableCourses(queryForm)
    tableData.value = (res.data.list || res.data || []).map(item => ({
      ...item,
      selectedCount: item.selectedCount || 0,
      maxCapacity: item.maxCapacity || 60,
      selected: item.selected || false
    }))
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const fetchPeriodInfo = async () => {
  try {
    const res = await getCurrentPeriod()
    if (res.data) {
      const { startTime, endTime } = res.data
      periodInfo.value = formatDateTime(startTime) + ' ~ ' + formatDateTime(endTime)
    } else {
      periodInfo.value = '未设置'
    }
  } catch (e) {
    periodInfo.value = '获取失败'
    console.error('获取选课时间段失败:', e)
  }
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.courseName = ''; queryForm.courseType = ''; queryForm.collegeId = ''; handleSearch() }

const handleSelect = async (row) => {
  try {
    await selectCourse({ courseId: row.id || row.courseId })
    ElMessage.success('选课成功')
    fetchData()
  } catch {}
}

const handleDrop = (row) => {
  ElMessageBox.confirm(`确定要退选课程"${row.courseName}"吗？`, '提示', { type: 'warning' }).then(async () => {
    await dropCourse(row.id || row.courseId)
    ElMessage.success('退选成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
  fetchPeriodInfo()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.header-actions { display: flex; gap: 8px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
