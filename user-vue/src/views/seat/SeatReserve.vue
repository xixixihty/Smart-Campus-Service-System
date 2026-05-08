<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Plus /></el-icon> 预约座位</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="区域">
          <el-select v-model="queryForm.areaId" placeholder="请选择区域" clearable @change="handleSearch">
            <el-option v-for="a in areaOptions" :key="a.id" :label="a.areaName" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="queryForm.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" @change="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px" v-loading="loading">
      <div class="seat-grid">
        <div v-for="seat in seatList" :key="seat.id" class="seat-item" :class="{
          'seat-available': seat.status === 'AVAILABLE',
          'seat-occupied': seat.status === 'OCCUPIED',
          'seat-reserved': seat.status === 'RESERVED',
          'seat-maintenance': seat.status === 'MAINTENANCE'
        }" @click="handleSeatClick(seat)">
          <el-icon :size="28"><Chair /></el-icon>
          <div class="seat-number">{{ seat.seatNumber }}</div>
          <div class="seat-status">
            <el-tag :type="seat.status === 'AVAILABLE' ? 'success' : seat.status === 'OCCUPIED' ? 'danger' : seat.status === 'RESERVED' ? 'warning' : 'info'" size="small">
              {{ seat.status === 'AVAILABLE' ? '空闲' : seat.status === 'OCCUPIED' ? '使用中' : seat.status === 'RESERVED' ? '已预约' : '维护中' }}
            </el-tag>
          </div>
        </div>
      </div>
      <el-empty v-if="seatList.length === 0 && !loading" description="暂无座位数据" />
    </el-card>

    <el-dialog v-model="reserveVisible" title="预约座位" width="500px">
      <el-descriptions :column="2" border v-if="currentSeat">
        <el-descriptions-item label="座位编号">{{ currentSeat.seatNumber }}</el-descriptions-item>
        <el-descriptions-item label="区域">{{ currentSeat.areaName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ queryForm.date }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="reserveForm" label-width="100px" style="margin-top: 20px">
        <el-form-item label="开始时间" required>
          <el-time-select v-model="reserveForm.startTime" :max-time="reserveForm.endTime" placeholder="选择开始时间"
            start="08:00" step="00:30" end="22:00" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-time-select v-model="reserveForm.endTime" :min-time="reserveForm.startTime" placeholder="选择结束时间"
            start="08:00" step="00:30" end="22:00" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reserveVisible = false">取消</el-button>
        <el-button type="primary" :loading="reserveLoading" @click="handleReserveSubmit">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { reserveSeat } from '@/api/seatReservation'
import request from '@/utils/request'

const loading = ref(false)
const reserveLoading = ref(false)
const reserveVisible = ref(false)
const currentSeat = ref(null)
const seatList = ref([])
const areaOptions = ref([])

const queryForm = reactive({
  areaId: '',
  date: new Date().toISOString().slice(0, 10)
})

const reserveForm = reactive({ startTime: '', endTime: '' })

const fetchSeats = async () => {
  loading.value = true
  try {
    const res = await request.get('/seats/admin', { params: { pageNum: 1, pageSize: 200, areaId: queryForm.areaId } })
    seatList.value = (res.data.list || []).map(s => ({
      ...s,
      status: s.status || 'AVAILABLE'
    }))
  } finally { loading.value = false }
}

const loadAreas = async () => {
  try {
    const res = await request.get('/seat-areas/admin', { params: { pageNum: 1, pageSize: 100 } })
    areaOptions.value = res.data.list || []
  } catch {}
}

const handleSearch = () => { fetchSeats() }
const handleReset = () => { queryForm.areaId = ''; handleSearch() }

const handleSeatClick = (seat) => {
  if (seat.status !== 'AVAILABLE') {
    ElMessage.warning('该座位当前不可预约')
    return
  }
  currentSeat.value = seat
  reserveForm.startTime = ''
  reserveForm.endTime = ''
  reserveVisible.value = true
}

const handleReserveSubmit = async () => {
  if (!reserveForm.startTime || !reserveForm.endTime) {
    ElMessage.warning('请选择时间')
    return
  }
  reserveLoading.value = true
  try {
    await reserveSeat({
      seatId: currentSeat.value.id,
      date: queryForm.date,
      startTime: reserveForm.startTime,
      endTime: reserveForm.endTime
    })
    ElMessage.success('预约成功')
    reserveVisible.value = false
    fetchSeats()
  } finally { reserveLoading.value = false }
}

onMounted(() => { loadAreas(); fetchSeats() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.seat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.seat-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }

.seat-available { background: #f0f9eb; border-color: #b7eb8f; }
.seat-available:hover { background: #e6f7d6; }
.seat-occupied { background: #fff1f0; border-color: #ffa39e; cursor: not-allowed; }
.seat-reserved { background: #fff7e6; border-color: #ffd591; cursor: not-allowed; }
.seat-maintenance { background: #f5f5f5; border-color: #d9d9d9; cursor: not-allowed; }

.seat-number { font-size: 16px; font-weight: 600; color: #303133; }
</style>
