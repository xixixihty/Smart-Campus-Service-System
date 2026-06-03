<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><List /></el-icon> 我的预约</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 200px">
            <el-option label="待签到" value="待签到" />
            <el-option label="使用中" value="使用中" />
            <el-option label="暂离" value="暂离" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已取消" value="已取消" />
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
        <el-table-column prop="seatNumber" label="座位编号" width="100" />
        <el-table-column prop="areaName" label="区域" width="100" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="100" />
        <el-table-column prop="endTime" label="结束时间" width="100" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '待签到' ? 'warning' : row.status === '使用中' ? 'success' : row.status === '暂离' ? 'warning' : row.status === '已完成' ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="handleDetail(row)"><el-icon><View /></el-icon>详情</el-button>
            <el-button v-if="row.status === '待签到'" type="success" size="small" @click="handleCheckIn(row)">
              签到
            </el-button>
            <el-button v-if="row.status === '使用中'" type="warning" size="small" @click="handleLeave(row)">
              暂离
            </el-button>
            <el-button v-if="row.status === '使用中'" type="danger" size="small" @click="handleCheckOut(row)">
              签退
            </el-button>
            <el-button v-if="row.status === '暂离'" type="danger" size="small" @click="handleCheckOut(row)">
              签退
            </el-button>
            <el-button v-if="row.status === '待签到'" type="danger" size="small" @click="handleCancel(row)">
              取消
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

    <el-dialog v-model="detailVisible" title="预约详情" width="480px">
      <el-descriptions :column="1" border v-if="currentReservation">
        <el-descriptions-item label="预约编号">{{ currentReservation.reservationNo || currentReservation.id }}</el-descriptions-item>
        <el-descriptions-item label="预约人">{{ currentReservation.userName }}</el-descriptions-item>
        <el-descriptions-item label="座位编号">{{ currentReservation.seatNumber }}</el-descriptions-item>
        <el-descriptions-item label="区域">{{ currentReservation.areaName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ currentReservation.date }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentReservation.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentReservation.endTime }}</el-descriptions-item>
        <el-descriptions-item label="签退/暂离时间">{{ currentReservation.leaveTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentReservation.status === '待签到' ? 'warning' : currentReservation.status === '使用中' ? 'success' : currentReservation.status === '暂离' ? 'warning' : currentReservation.status === '已完成' ? 'success' : 'danger'" size="small">
            {{ currentReservation.status }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReservationList, checkIn, checkOut, cancelReservation, leaveSeat } from '@/api/seatReservation'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentReservation = ref(null)

const queryForm = reactive({ pageNum: 1, pageSize: 10, status: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getReservationList(queryForm)
    tableData.value = res.data.list || res.data || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { queryForm.status = ''; handleSearch() }

const handleCheckIn = async (row) => {
  await checkIn(row.id)
  ElMessage.success('签到成功')
  fetchData()
}

const handleCheckOut = (row) => {
  ElMessageBox.confirm('确定要签退吗？', '提示', { type: 'warning' }).then(async () => {
    await checkOut(row.id)
    ElMessage.success('签退成功')
    fetchData()
  }).catch(() => {})
}

const handleLeave = async (row) => {
  await leaveSeat(row.id)
  ElMessage.success('已暂离')
  fetchData()
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消预约吗？', '提示', { type: 'warning' }).then(async () => {
    await cancelReservation(row.id)
    ElMessage.success('已取消')
    fetchData()
  }).catch(() => {})
}

const handleDetail = (row) => {
  currentReservation.value = row
  detailVisible.value = true
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
