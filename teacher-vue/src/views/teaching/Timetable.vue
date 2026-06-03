<template>
  <div class="timetable-page">
    <h2 class="page-title">
      <el-icon><Calendar /></el-icon>
      我的课表
    </h2>

    <div class="toolbar">
      <span class="toolbar-label">学期：</span>
      <el-select v-model="semesterId" placeholder="请选择学期" @change="fetchTimetable" style="width: 240px">
        <el-option v-for="s in semesterList" :key="s.id" :label="s.semesterName" :value="s.id" />
      </el-select>
    </div>

    <el-card shadow="hover" v-loading="loading">
      <div v-if="timetableData.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无课表数据" />
      </div>
      <div v-else class="timetable-grid">
        <!-- 星期头 -->
        <div class="timetable-header">
          <div class="time-label">节次</div>
          <div v-for="day in weekDays" :key="day.value" class="day-header">
            {{ day.label }}
          </div>
        </div>
        <!-- 节次行 -->
        <div v-for="section in maxSections" :key="section" class="timetable-row">
          <div class="time-label">第{{ section }}节</div>
          <div v-for="day in 7" :key="day" class="timetable-cell">
            <div
              v-for="item in getCellItems(day, section)"
              :key="item.courseScheduleId"
              class="course-block"
              :style="{ backgroundColor: getCourseColor(item.courseName) }"
            >
              <div class="course-name">{{ item.courseName }}</div>
              <div class="course-info">{{ item.classroomName }}</div>
              <div class="course-info">{{ item.classNames }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyTimetable } from '@/api/timetable'
import { getSemesterList } from '@/api/semester'
import { Calendar } from '@element-plus/icons-vue'

const loading = ref(false)
const semesterId = ref(null)
const semesterList = ref([])
const timetableData = ref([])

const weekDays = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]

const colorPalette = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
  '#00d2ff', '#a18cd1', '#f093fb', '#4facfe', '#43e97b'
]

const courseColorMap = {}
let colorIndex = 0

const getCourseColor = (courseName) => {
  if (!courseColorMap[courseName]) {
    courseColorMap[courseName] = colorPalette[colorIndex % colorPalette.length]
    colorIndex++
  }
  return courseColorMap[courseName]
}

const maxSections = ref(8)

const getCellItems = (day, section) => {
  return timetableData.value.filter(
    t => t.weekDay === day && t.startSection <= section && t.endSection >= section
  )
}

const fetchSemesters = async () => {
  try {
    const res = await getSemesterList({ pageNum: 1, pageSize: 50 })
    semesterList.value = res.data?.list || []
    if (semesterList.value.length > 0) {
      semesterId.value = semesterList.value[0].id
    }
  } catch {
    // ignore
  }
}

const fetchTimetable = async () => {
  if (!semesterId.value) return
  loading.value = true
  try {
    const res = await getMyTimetable(semesterId.value)
    timetableData.value = res.data || []
    // 计算最大节次
    if (timetableData.value.length > 0) {
      maxSections.value = Math.max(...timetableData.value.map(t => t.endSection || 0), 8)
    }
  } catch {
    timetableData.value = []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchSemesters()
  if (semesterId.value) {
    await fetchTimetable()
  }
})
</script>

<style scoped>
.timetable-grid {
  width: 100%;
  overflow-x: auto;
}

.timetable-header,
.timetable-row {
  display: grid;
  grid-template-columns: 80px repeat(7, 1fr);
  min-width: 900px;
}

.timetable-header {
  background: #f5f7fa;
  border-radius: 4px 4px 0 0;
}

.time-label,
.day-header {
  padding: 10px 4px;
  text-align: center;
  font-weight: 600;
  font-size: 13px;
  color: #606266;
  border: 1px solid #ebeef5;
}

.timetable-cell {
  border: 1px solid #ebeef5;
  min-height: 60px;
  padding: 2px;
  vertical-align: top;
}

.course-block {
  padding: 6px 8px;
  border-radius: 4px;
  margin: 2px;
  color: #fff;
  font-size: 12px;
  line-height: 1.5;
}

.course-name {
  font-weight: 600;
  font-size: 13px;
}

.course-info {
  opacity: 0.9;
  font-size: 11px;
}

html.dark .timetable-header {
  background: #262727;
}

html.dark .time-label,
html.dark .day-header {
  color: #cfd3dc;
  border-color: #363637;
}

html.dark .timetable-cell {
  border-color: #363637;
}
</style>