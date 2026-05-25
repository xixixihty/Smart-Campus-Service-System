<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Plus /></el-icon> 预约座位</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="区域">
          <el-select v-model="queryForm.areaId" placeholder="请选择区域" clearable style="width: 200px" @change="handleSearch">
            <el-option v-for="a in areaOptions" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="queryForm.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 200px" @change="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px" v-loading="loading">
      <div class="seat-legend">
        <span class="legend-item"><span class="legend-dot dot-available"></span> 可预约</span>
        <span class="legend-item"><span class="legend-dot dot-occupied"></span> 使用中</span>
        <span class="legend-item"><span class="legend-dot dot-reserved"></span> 暂离</span>
        <span class="legend-item"><span class="legend-dot dot-maintenance"></span> 维修中</span>
      </div>
      <div class="seat-grid">
        <div v-for="seat in seatList" :key="seat.id" class="seat-item" :class="{
          'seat-available': seat.status === '空闲',
          'seat-occupied': seat.status === '使用中',
          'seat-reserved': seat.status === '暂离',
          'seat-maintenance': seat.status === '维修'
        }" @click="handleSeatClick(seat)">
          <el-icon :size="28"><Chair /></el-icon>
          <div class="seat-number">{{ seat.seatNumber }}</div>
          <div class="seat-status">
            <el-tag :type="seat.status === '空闲' ? 'success' : seat.status === '使用中' ? 'warning' : seat.status === '暂离' ? 'warning' : 'info'" size="small">
              {{ seat.status === '空闲' ? '空闲' : seat.status === '使用中' ? '使用中' : seat.status === '暂离' ? '暂离' : '维修' }}
            </el-tag>
          </div>
          <div v-if="seat.status === '使用中'" class="seat-hint">可预约后续时段</div>
        </div>
      </div>
      <el-empty v-if="seatList.length === 0 && !loading" description="暂无座位数据" />
    </el-card>

    <el-dialog v-model="reserveVisible" title="预约座位" width="600px">
      <el-descriptions :column="2" border v-if="currentSeat">
        <el-descriptions-item label="座位编号">{{ currentSeat.seatNumber }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">{{ currentSeat.status }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ queryForm.date }}</el-descriptions-item>
        <el-descriptions-item label="区域">{{ currentSeat.areaName || '阅览室' + currentSeat.roomId }}</el-descriptions-item>
      </el-descriptions>
      
      <div v-if="seatSchedule.length > 0" style="margin-top: 20px">
        <h4>当日时段占用情况</h4>
        <div class="time-schedule">
          <div v-for="slot in seatSchedule" :key="slot.time" 
            class="time-slot" :class="{ 'slot-occupied': !slot.available }">
            <span class="slot-time">{{ slot.time }}</span>
            <span class="slot-status">{{ slot.available ? '可选' : '已预约' }}</span>
          </div>
        </div>
      </div>
      
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
const seatSchedule = ref([])

const queryForm = reactive({
  areaId: '',
  date: new Date().toISOString().slice(0, 10)
})

const reserveForm = reactive({ startTime: '', endTime: '' })

const fetchSeats = async () => {
  loading.value = true
  try {
    const res = await request.get('/seat-reservations/user/available', { params: { areaId: queryForm.areaId, date: queryForm.date } })
    seatList.value = res.data || []
  } finally { loading.value = false }
}

const fetchSeatSchedule = async (seatId) => {
  try {
    const res = await request.get(`/seat-reservations/user/${seatId}/schedule/${queryForm.date}`)
    seatSchedule.value = res.data.timeSlots || []
  } catch (e) {
    seatSchedule.value = []
  }
}

const loadAreas = async () => {
  try {
    const res = await request.get('/seats/admin', { params: { pageNum: 1, pageSize: 100 } })
    const roomMap = new Map()
    ;(res.data.list || []).forEach(s => {
      if (s.roomId && !roomMap.has(s.roomId)) {
        roomMap.set(s.roomId, { id: s.roomId, name: `阅览室${s.roomId}` })
      }
    })
    areaOptions.value = Array.from(roomMap.values())
  } catch {}
}

const handleSearch = () => { fetchSeats() }
const handleReset = () => { queryForm.areaId = ''; handleSearch() }

const handleSeatClick = (seat) => {
  if (seat.status === '维修') {
    ElMessage.warning('该座位正在维修，不可预约')
    return
  }
  currentSeat.value = seat
  reserveForm.startTime = ''
  reserveForm.endTime = ''
  seatSchedule.value = []
  fetchSeatSchedule(seat.id)
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
  } catch (e) {
    if (e.response?.data?.message === '该座位在该时间段已被预约') {
      ElMessage.warning('该时间段已被预约，请选择其他时间')
    } else {
      ElMessage.error('预约失败，请稍后重试')
    }
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

.seat-legend {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  padding: 10px 16px;
  background: #fafafa;
  border-radius: 8px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  flex-shrink: 0;
}

.dot-available { background: #b7eb8f; border: 1px solid #52c41a; }
.dot-occupied { background: #ffe58f; border: 1px solid #fa8c16; }
.dot-reserved { background: #ffd591; border: 1px solid #fa8c16; }
.dot-maintenance { background: #d9d9d9; border: 1px solid #8c8c8c; }

.seat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.seat-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }

.seat-available { background: #f0f9eb; border-color: #b7eb8f; }
.seat-available:hover { background: #e6f7d6; }
.seat-occupied { background: #fffbe6; border-color: #ffe58f; }
.seat-occupied:hover { background: #fff7d6; }
.seat-reserved { background: #fff7e6; border-color: #ffd591; cursor: not-allowed; }
.seat-maintenance { background: #f5f5f5; border-color: #d9d9d9; cursor: not-allowed; }

.seat-number { font-size: 16px; font-weight: 600; color: #303133; }
.seat-hint { font-size: 11px; color: #909399; }

.time-schedule {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
}

.time-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 4px;
  border-radius: 4px;
  background: #f5f7fa;
}

.time-slot.slot-occupied {
  background: #fff1f0;
}

.slot-time { font-size: 12px; font-weight: 500; color: #606266; }
.slot-status { font-size: 10px; color: #909399; margin-top: 2px; }
.time-slot.slot-occupied .slot-status { color: #f56c6c; }
</style>
