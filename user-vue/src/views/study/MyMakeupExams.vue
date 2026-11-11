<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><EditPen /></el-icon> 补考信息</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <template v-if="tableData.length > 0">
        <el-table :data="tableData" stripe border>
          <el-table-column prop="courseName" label="课程名称" min-width="160" />
          <el-table-column prop="examDate" label="考试日期" width="120" align="center" />
          <el-table-column label="考试时间" width="140" align="center">
            <template #default="{ row }">{{ row.examStartTime }} - {{ row.examEndTime }}</template>
          </el-table-column>
          <el-table-column prop="location" label="考试地点" min-width="140" />
          <el-table-column label="补考成绩" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.makeupScore != null" :class="row.makeupScore >= PASS_LINE ? 'score-pass' : 'score-fail'">
                {{ row.makeupScore }}
              </span>
              <el-tag v-else type="info" size="small">待考</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="myStatus" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusType(row.myStatus)" size="small">{{ row.myStatus }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="详情" width="80" align="center">
            <template #default="{ row }">
              <el-button type="primary" link @click="showDetail(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <el-empty v-else description="暂无补考安排" />
    </el-card>

    <el-dialog v-model="detailVisible" title="补考详情" width="480px">
      <el-descriptions :column="2" border v-if="currentExam">
        <el-descriptions-item label="课程名称" :span="2">{{ currentExam.courseName }}</el-descriptions-item>
        <el-descriptions-item label="考试日期">{{ currentExam.examDate }}</el-descriptions-item>
        <el-descriptions-item label="考试时间">{{ currentExam.examStartTime }} - {{ currentExam.examEndTime }}</el-descriptions-item>
        <el-descriptions-item label="考试地点">{{ currentExam.location }}</el-descriptions-item>
        <el-descriptions-item label="场次状态">
          <el-tag :type="statusType(currentExam.sessionStatus)" size="small">{{ currentExam.sessionStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="我的状态">
          <el-tag :type="statusType(currentExam.myStatus)" size="small">{{ currentExam.myStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="补考成绩" v-if="currentExam.makeupScore != null">
          <span :class="currentExam.makeupScore >= PASS_LINE ? 'score-pass' : 'score-fail'">{{ currentExam.makeupScore }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="通知时间" v-if="currentExam.notificationTime">{{ currentExam.notificationTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyMakeupSessions } from '@/api/makeupExam'

const PASS_LINE = 60

const loading = ref(false)
const detailVisible = ref(false)
const currentExam = ref(null)
const tableData = ref([])

const queryForm = reactive({ status: '' })

const statusType = (status) => {
  const map = {
    '已通过': 'success', '已签到': 'success', '已通知': 'warning',
    '待通知': 'info', '未通过': 'danger', '缺考': 'danger',
    '已安排': 'warning', '已结束': 'info', '已取消': 'danger'
  }
  return map[status] || 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyMakeupSessions({ status: queryForm.status || undefined })
    // 后端返回 List<T>，直接作为 data 返回，不是 PageInfo 格式
    tableData.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error('获取补考列表失败:', err)
  } finally { loading.value = false }
}

const showDetail = (row) => {
  currentExam.value = row
  detailVisible.value = true
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.score-pass { color: #67c23a; font-weight: bold; }
.score-fail { color: #f56c6c; font-weight: bold; }
</style>