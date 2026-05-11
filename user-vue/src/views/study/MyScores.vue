<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Tickets /></el-icon> 我的成绩</h2>
      <div class="header-actions">
        <el-tag type="success" size="large">
          平均绩点：{{ gpa }}
        </el-tag>
      </div>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="学期">
          <el-select v-model="queryForm.semesterId" placeholder="请选择学期" clearable style="width: 200px">
            <el-option v-for="s in semesterOptions" :key="s.id" :label="s.semesterName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px" v-loading="loading">
      <el-table :data="tableData" stripe border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column prop="studentNo" label="学号" width="100" />
        <el-table-column prop="semesterName" label="学期" width="180" />
        <el-table-column prop="usualScore" label="平时成绩" width="100" align="center" />
        <el-table-column prop="finalScore" label="期末成绩" width="100" align="center" />
        <el-table-column prop="totalScore" label="总成绩" width="100" align="center" />
        <el-table-column prop="scorePoint" label="绩点" width="80" align="center" />
        <el-table-column prop="credit" label="学分" width="70" align="center" />
        <el-table-column prop="examStatus" label="考试状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.examStatus === '正常' ? 'success' : row.examStatus === '缺考' ? 'danger' : row.examStatus === '作弊' ? 'danger' : 'warning'" size="small">
              {{ row.examStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button type="info" link @click="showDetail(row)"><el-icon><View /></el-icon>详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="成绩详情" width="450px">
      <el-descriptions :column="1" border v-if="currentScore">
        <el-descriptions-item label="课程名称">{{ currentScore.courseName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentScore.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ currentScore.semesterName }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ currentScore.credit }}</el-descriptions-item>
        <el-descriptions-item label="平时成绩">{{ currentScore.usualScore }}</el-descriptions-item>
        <el-descriptions-item label="期末成绩">{{ currentScore.finalScore }}</el-descriptions-item>
        <el-descriptions-item label="考试状态">{{ currentScore.examStatus }}</el-descriptions-item>
        <el-descriptions-item label="总成绩">{{ currentScore.totalScore }}</el-descriptions-item>
        <el-descriptions-item label="绩点">{{ currentScore.scorePoint }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ currentScore.credit }}</el-descriptions-item>
        <el-descriptions-item label="补考安排">{{ currentScore.ExamupExamId }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyScores, getMyGPA } from '@/api/scoreEntry'

const loading = ref(false)
const detailVisible = ref(false)
const currentScore = ref(null)
const tableData = ref([])
const total = ref(0)
const gpa = ref('--')
const semesterOptions = ref([])

const queryForm = reactive({ pageNum: 1, pageSize: 10, semesterId: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const [scoreRes, gpaRes] = await Promise.all([
      getMyScores(queryForm),
      getMyGPA().catch(() => ({ data: null }))
    ])
    tableData.value = scoreRes.data.list || scoreRes.data || []
    total.value = scoreRes.data.total || 0
    if (gpaRes.data) {
      gpa.value = gpaRes.data.gpa || gpaRes.data || '--'
    }
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.semesterId = ''; handleSearch() }

const showDetail = (row) => {
  currentScore.value = row
  detailVisible.value = true
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.header-actions { display: flex; gap: 8px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
