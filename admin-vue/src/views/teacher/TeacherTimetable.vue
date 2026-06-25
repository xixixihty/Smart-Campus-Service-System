<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Timer /></el-icon> 我的课表</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="学期">
          <el-select v-model="queryForm.semesterId" placeholder="请选择学期" clearable style="width: 200px" @change="fetchData">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="星期">
          <el-select v-model="queryForm.weekDay" placeholder="请选择" clearable style="width: 120px" @change="fetchData">
            <el-option v-for="(label, idx) in weekDays" :key="idx + 1" :label="label" :value="idx + 1" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="table-card">
      <el-card shadow="never">
        <el-table :data="tableData" v-loading="loading" stripe border max-height="calc(100vh - 280px)" scrollbar-always-on>
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="courseName" label="课程名称" min-width="150" align="center" />
          <el-table-column prop="semesterName" label="学期名称" width="130" align="center" />
          <el-table-column prop="classroomName" label="教室" width="130" align="center" />
          <el-table-column prop="classNames" label="授课班级" width="150" />
          <el-table-column prop="weekDay" label="星期" width="80" align="center">
            <template #default="{ row }">{{ weekDays[row.weekDay - 1] || row.weekDay }}</template>
          </el-table-column>
          <el-table-column label="节次" width="120" align="center">
            <template #default="{ row }">{{ row.startSection }}-{{ row.endSection }}节</template>
          </el-table-column>
          <el-table-column label="周次" width="150" align="center">
            <template #default="{ row }">{{ row.weekRange }}</template>
          </el-table-column>
          <el-table-column width="12" class-name="scroll-hint-column" fixed="right" />
        </el-table>
        <div class="pagination">
          <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
            :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
            @size-change="fetchData" @current-change="fetchData" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTeacherTimetable } from '@/api/teacher'
import { getSemesterList } from '@/api/semester'

const weekDays = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日']

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const semesterOptions = ref([])

const queryForm = reactive({ semesterId: '', weekDay: '', pageNum: 1, pageSize: 10 })

const loadSemesters = async () => {
  try {
    const res = await getSemesterList({ pageNum: 1, pageSize: 100 })
    semesterOptions.value = res.data.list || []
    if (semesterOptions.value.length > 0) {
      const current = semesterOptions.value.find(s => s.isCurrent)
      if (current) queryForm.semesterId = current.id
    }
  } catch (e) { console.error('加载学期列表失败:', e) }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTeacherTimetable(queryForm)
    tableData.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载课表失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => { await loadSemesters(); fetchData() })
</script>