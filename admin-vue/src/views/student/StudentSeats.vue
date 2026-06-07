<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><OfficeBuilding /></el-icon> 座位预约</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="可预约座位" name="seats">
        <el-card shadow="never">
          <el-form :inline="true" class="search-form">
            <el-form-item label="预约日期">
              <el-date-picker v-model="queryForm.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD"
                style="width: 180px" @change="fetchSeats" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchSeats"><el-icon><Search /></el-icon>查询</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="table-card">
          <el-table :data="seatData" v-loading="seatLoading" stripe border max-height="calc(100vh - 340px)">
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="seatNumber" label="座位号" width="120" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === '可用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时段" min-width="300" align="center">
              <template #default="{ row }">
                <div class="time-slots">
                  <el-button v-for="(slot, idx) in row.scheduleList" :key="idx"
                    :type="getSlotType(slot)" size="small" style="margin: 2px"
                    :disabled="!slot.available"
                    @click="handleReserve(row, slot)">
                    {{ slot.time }}
                    <br />{{ slot.available ? '可预约' : slot.reason || '不可用' }}
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我的预约" name="my">
        <el-card shadow="never" class="table-card">
          <el-table :data="myReservationData" v-loading="myReservationLoading" stripe border max-height="calc(100vh - 280px)" empty-text="暂无预约记录">
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="seatNumber" label="座位号" width="120" align="center" />
            <el-table-column prop="areaName" label="区域" width="120" align="center" />
            <el-table-column prop="date" label="日期" width="120" align="center" />
            <el-table-column prop="startTime" label="开始时间" width="100" align="center" />
            <el-table-column prop="endTime" label="结束时间" width="100" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === '已预约'" type="success" link @click="handleCheckIn(row)">
                  <el-icon><Check /></el-icon>签到
                </el-button>
                <el-button v-if="row.status === '已签到'" type="warning" link @click="handleCheckOut(row)">
                  <el-icon><Close /></el-icon>签退
                </el-button>
                <el-button v-if="row.status === '已预约' || row.status === '已签到'" type="danger" link @click="handleCancel(row)">
                  <el-icon><Delete /></el-icon>取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:current-page="myReservationQuery.pageNum" v-model:page-size="myReservationQuery.pageSize"
              :page-sizes="[10, 20, 50]" :total="myReservationTotal" layout="total, sizes, prev, pager, next"
              @size-change="fetchMyReservations" @current-change="fetchMyReservations" />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentSeatList, getStudentSeatSchedule, reserveSeat, cancelSeatReservation, checkInSeat, checkOutSeat, getMySeatReservations } from '@/api/student'

const activeTab = ref('seats')

const seatLoading = ref(false)
const seatData = ref([])
const today = new Date().toISOString().slice(0, 10)
const queryForm = reactive({ date: today })

const myReservationLoading = ref(false)
const myReservationData = ref([])
const myReservationTotal = ref(0)
const myReservationQuery = reactive({ pageNum: 1, pageSize: 10 })

const fetchSeats = async () => {
  seatLoading.value = true
  try {
    const res = await getStudentSeatList({ date: queryForm.date })
    const seats = (res.data || []).map(seat => ({
      ...seat,
      scheduleList: []
    }))
    seatData.value = seats

    // 并行加载每个座位的时段
    const schedulePromises = seats.map(async (seat, index) => {
      try {
        const scheduleRes = await getStudentSeatSchedule(seat.id, queryForm.date)
        seatData.value[index].scheduleList = scheduleRes.data?.timeSlots || scheduleRes.data?.scheduleList || []
      } catch {
        seatData.value[index].scheduleList = []
      }
    })
    await Promise.all(schedulePromises)
  } catch (e) {
    console.error('加载座位列表失败:', e)
  } finally {
    seatLoading.value = false
  }
}

const fetchMyReservations = async () => {
  myReservationLoading.value = true
  try {
    const res = await getMySeatReservations(myReservationQuery)
    myReservationData.value = res.data?.records || res.data?.list || []
    myReservationTotal.value = res.data?.total || 0
  } catch (e) {
    console.error('加载预约记录失败:', e)
  } finally {
    myReservationLoading.value = false
  }
}

const onTabChange = (tab) => {
  if (tab === 'my') fetchMyReservations()
}

const getSlotType = (slot) => {
  if (slot.available) return 'primary'
  return 'info'
}

const statusTagType = (status) => {
  const map = { '已预约': 'warning', '已签到': 'success', '已取消': 'info', '已过期': 'info', '已签退': 'success' }
  return map[status] || 'info'
}

const handleReserve = async (row, slot) => {
  try {
    // 计算结束时间（时间段为1小时）
    const hour = parseInt(slot.time.split(':')[0])
    const nextHour = String(hour + 1).padStart(2, '0')
    const endTime = `${nextHour}:00`

    await ElMessageBox.confirm(
      `确定预约 ${row.seatNumber} 号座位（${queryForm.date} ${slot.time}-${endTime}）吗？`,
      '预约确认', { type: 'info' }
    )
    await reserveSeat({
      seatId: row.id,
      date: queryForm.date,
      startTime: slot.time,
      endTime: endTime
    })
    ElMessage.success('预约成功')
    fetchSeats()
  } catch {}
}

const handleCheckIn = async (row) => {
  try {
    await checkInSeat(row.id)
    ElMessage.success('签到成功')
    fetchMyReservations()
  } catch (e) {
    ElMessage.error(e?.message || '签到失败')
  }
}

const handleCheckOut = async (row) => {
  try {
    await checkOutSeat(row.id)
    ElMessage.success('签退成功')
    fetchMyReservations()
  } catch (e) {
    ElMessage.error(e?.message || '签退失败')
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该预约吗？', '取消确认', { type: 'warning' })
    await cancelSeatReservation(row.id)
    ElMessage.success('取消成功')
    fetchMyReservations()
  } catch {}
}

onMounted(() => fetchSeats())
</script>