<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Document /></el-icon> 借阅记录</h2>
      <div class="header-actions">
        <el-button type="success" @click="handleStatistics"><el-icon><DataAnalysis /></el-icon>借阅统计</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="学生">
          <el-select v-model="queryForm.studentId" placeholder="请选择学生" clearable filterable class="search-input">
            <el-option v-for="s in studentOptions" :key="s.id" :label="s.name + ' (' + s.studentNo + ')'" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="图书">
          <el-select v-model="queryForm.bookId" placeholder="请选择图书" clearable filterable class="search-input">
            <el-option v-for="b in bookOptions" :key="b.id" :label="b.bookName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable class="search-input">
            <el-option label="借出中" value="借出中" />
            <el-option label="已归还" value="已归还" />
            <el-option label="逾期" value="逾期" />
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
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="borrowNo" label="借阅编号" width="120" align="center" />
        <el-table-column prop="userName" label="借阅人姓名" width="100" align="center" />
        <el-table-column prop="bookTitle" label="图书标题" min-width="180px" align="center" />
        <el-table-column prop="borrowDate" label="借阅日期" width="120" align="center" />
        <el-table-column prop="dueDate" label="应还日期" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '借出中' ? 'warning' : row.status === '已归还' ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="逾期天数" width="100" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="statsVisible" title="借阅统计分析" width="900px" destroy-on-close>
      <div v-loading="statsLoading">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-cards">
          <el-col :span="8">
            <div class="stat-card stat-card-blue">
              <div class="stat-icon">
                <el-icon :size="32"><Reading /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.totalBorrows }}</div>
                <div class="stat-label">总借阅量</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card stat-card-green">
              <div class="stat-icon">
                <el-icon :size="32"><Checked /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.currentBorrows }}</div>
                <div class="stat-label">当前借阅中</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card stat-card-red">
              <div class="stat-icon">
                <el-icon :size="32"><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.overdueCount }}</div>
                <div class="stat-label">逾期未还</div>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 图表区域 -->
        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <span><el-icon><PieChart /></el-icon> 分类借阅分布</span>
              </template>
              <div ref="categoryChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <span><el-icon><TrendCharts /></el-icon> 借阅趋势（近7天）</span>
              </template>
              <div ref="trendChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 分类统计表格 -->
        <el-card shadow="hover" style="margin-top: 20px">
          <template #header>
            <span><el-icon><DataAnalysis /></el-icon> 各分类借阅详情</span>
          </template>
          <el-table :data="categoryStats" stripe border max-height="300">
            <el-table-column prop="categoryName" label="分类名称" align="center" />
            <el-table-column prop="borrowCount" label="借阅次数" align="center" />
            <el-table-column label="占比" align="center">
              <template #default="{ row }">
                <el-progress 
                  :percentage="getPercentage(row.borrowCount)" 
                  :color="getProgressColor(getPercentage(row.borrowCount))"
                  :stroke-width="12"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
      <template #footer>
        <el-button @click="statsVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="借阅记录详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="借阅记录ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="借阅流水号">{{ detailData.borrowNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="借阅人">{{ detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="图书标题">{{ detailData.bookTitle }}</el-descriptions-item>
        <el-descriptions-item label="图书ISBN">{{ detailData.bookIsbn || '-' }}</el-descriptions-item>
        <el-descriptions-item label="借阅日期">{{ detailData.borrowDate }}</el-descriptions-item>
        <el-descriptions-item label="应还日期">{{ detailData.dueDate }}</el-descriptions-item>
        <el-descriptions-item label="归还日期">{{ detailData.returnDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === '借出中' ? 'warning' : detailData.status === '已归还' ? 'success' : 'danger'" size="small">
            {{ detailData.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="逾期天数">
          <span :style="{ color: detailData.overdueDays > 0 ? '#f56c6c' : '#67c23a' }">
            {{ detailData.overdueDays || 0 }} 天
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ detailData.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Reading, Checked, Warning, PieChart, TrendCharts, DataAnalysis } from '@element-plus/icons-vue'
import { getBorrowRecordList, getBorrowRecordDetail, getBorrowStatistics } from '@/api/borrowRecord'
import { getStudentList } from '@/api/student'
import { getBookList } from '@/api/book'

const loading = ref(false)
const statsVisible = ref(false)
const statsLoading = ref(false)
const detailVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const studentOptions = ref([])
const bookOptions = ref([])
const categoryStats = ref([])

const stats = reactive({ totalBorrows: 0, currentBorrows: 0, overdueCount: 0 })

const detailData = reactive({
  id: null,
  borrowNo: '',
  userId: null,
  userName: '',
  bookId: null,
  bookTitle: '',
  bookIsbn: '',
  borrowDate: '',
  dueDate: '',
  returnDate: '',
  status: '',
  overdueDays: 0,
  createTime: '',
  updateTime: ''
})

const queryForm = reactive({ pageNum: 1, pageSize: 10, studentId: '', bookId: '', status: '' })

const categoryChartRef = ref(null)
const trendChartRef = ref(null)
let categoryChart = null
let trendChart = null

const loadOptions = async () => {
  const [studentRes, bookRes] = await Promise.all([
    getStudentList({ pageNum: 1, pageSize: 500 }),
    getBookList({ pageNum: 1, pageSize: 500 })
  ])
  studentOptions.value = studentRes.data.list || []
  bookOptions.value = bookRes.data.list || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getBorrowRecordList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.studentId = ''; queryForm.bookId = ''; queryForm.status = ''; handleSearch() }

const handleView = async (row) => {
  try {
    const res = await getBorrowRecordDetail(row.id)
    const data = res.data
    Object.assign(detailData, {
      id: data.id || null,
      borrowNo: data.borrowNo || '',
      userId: data.userId || null,
      userName: data.userName || '',
      bookId: data.bookId || null,
      bookTitle: data.bookTitle || '',
      bookIsbn: data.bookIsbn || '',
      borrowDate: data.borrowDate || '',
      dueDate: data.dueDate || '',
      returnDate: data.returnDate || '',
      status: data.status || '',
      overdueDays: data.overdueDays || 0,
      createTime: data.createTime || '',
      updateTime: data.updateTime || ''
    })
    detailVisible.value = true
  } catch (err) {
    console.error('获取借阅记录详情失败:', err)
    ElMessage.error('获取借阅记录详情失败')
  }
}

const getPercentage = (count) => {
  if (stats.totalBorrows === 0) return 0
  return Math.round((count / stats.totalBorrows) * 100)
}

const getProgressColor = (percentage) => {
  if (percentage >= 50) return '#f56c6c'
  if (percentage >= 30) return '#e6a23c'
  return '#67c23a'
}

const initCategoryChart = (data) => {
  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }
  const pieData = data.map(item => ({ name: item.categoryName || '未知', value: item.borrowCount || 0 }))
  const colorPalette = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']

  categoryChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'center' },
    series: [{
      name: '分类借阅',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: 18, fontWeight: 'bold' }
      },
      labelLine: { show: false },
      data: pieData,
      color: colorPalette,
      animationType: 'scale',
      animationEasing: 'elasticOut',
      animationDelay: (idx) => Math.random() * 200
    }]
  })
}

const initTrendChart = (data) => {
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  const dates = data.map(item => item.name || '')
  const values = data.map(item => item.value || 0)

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value' },
    series: [{
      name: '借阅次数',
      type: 'line',
      data: values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { width: 3, color: '#5470c6' },
      itemStyle: { color: '#5470c6' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(84, 112, 198, 0.5)' },
          { offset: 1, color: 'rgba(84, 112, 198, 0.05)' }
        ])
      },
      animationDuration: 1500,
      animationEasing: 'cubicInOut'
    }]
  })
}

const handleStatistics = async () => {
  statsLoading.value = true
  statsVisible.value = true
  try {
    const res = await getBorrowStatistics()
    const data = res.data || {}
    Object.assign(stats, {
      totalBorrows: data.totalBorrows || 0,
      currentBorrows: data.currentBorrows || 0,
      overdueCount: data.overdueBorrows || 0
    })
    categoryStats.value = data.categoryBorrows || []

    await nextTick()

    if (data.categoryBorrows && data.categoryBorrows.length > 0) {
      initCategoryChart(data.categoryBorrows)
    }
    if (data.borrowTrend7Days && data.borrowTrend7Days.length > 0) {
      initTrendChart(data.borrowTrend7Days)
    }
  } catch (err) {
    console.error('获取借阅统计失败:', err)
    ElMessage.error('获取借阅统计失败')
  } finally {
    statsLoading.value = false
  }
}

const handleResize = () => {
  categoryChart?.resize()
  trendChart?.resize()
}

onMounted(async () => { 
  await loadOptions()
  fetchData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  categoryChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.header-actions { display: flex; gap: 8px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.search-form { display: flex; flex-wrap: wrap; gap: 0; }
.search-form :deep(.el-form-item) { margin-bottom: 16px; }
.search-input { width: 200px; }

.stats-cards { margin-bottom: 20px; }
.stat-card {
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
  transition: transform 0.3s, box-shadow 0.3s;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}
.stat-card-blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.stat-card-green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}
.stat-card-red {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
}
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-content { flex: 1; }
.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
}
.chart-container {
  width: 100%;
  height: 300px;
}
</style>
