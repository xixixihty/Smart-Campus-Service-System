<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Tickets /></el-icon> 成绩查询</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="学期">
          <el-select v-model="queryForm.semesterId" placeholder="请选择学期" clearable style="width: 180px">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程名称">
          <el-input v-model="queryForm.courseName" placeholder="请输入课程名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border max-height="calc(100vh - 280px)" scrollbar-always-on>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="180" align="center" />
        <el-table-column prop="semesterName" label="学期" width="130" align="center" />
        <el-table-column prop="score" label="成绩" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getScoreType(row.score)">{{ row.score ?? '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="examType" label="考试类型" width="100" align="center" />
        <el-table-column prop="updateTime" label="更新时间" width="170" align="center" />
        <el-table-column width="12" class-name="scroll-hint-column" fixed="right" />
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
import { getStudentScoreList } from '@/api/student'
import { getSemesterList } from '@/api/semester'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const semesterOptions = ref([])

const queryForm = reactive({ semesterId: '', courseName: '', pageNum: 1, pageSize: 10 })

const getScoreType = (score) => {
  if (score == null) return 'info'
  if (score >= 90) return 'success'
  if (score >= 60) return ''
  return 'danger'
}

const loadSemesters = async () => {
  try {
    const res = await getSemesterList({ pageNum: 1, pageSize: 100 })
    semesterOptions.value = res.data.list || []
  } catch (e) { console.error('加载学期列表失败:', e) }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudentScoreList(queryForm)
    tableData.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载成绩列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { Object.assign(queryForm, { semesterId: '', courseName: '', pageNum: 1, pageSize: 10 }); fetchData() }

onMounted(async () => { await loadSemesters(); fetchData() })
</script>