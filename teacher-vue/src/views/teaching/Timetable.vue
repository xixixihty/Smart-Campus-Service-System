<template>
  <div class="timetable-page">
    <h2 class="page-title">
      <el-icon><Calendar /></el-icon>
      我的课表
    </h2>

    <div class="toolbar">
      <span class="toolbar-label">学期：</span>
      <el-select v-model="semesterId" placeholder="请选择学期" @change="fetchTimetable" style="width: 240px">
        <el-option v-for="s in semesterList" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
    </div>

    <el-card shadow="hover" v-loading="loading">
      <div v-if="timetableData.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无课表数据" />
      </div>
      <div v-else class="timetable">
        <!-- 星期头 -->
        <div class="timetable-header">
          <div class="period-col">节次</div>
          <div v-for="day in weekDays" :key="day.value" class="day-col">
            {{ day.label }}
          </div>
        </div>
        <!-- 节次行 -->
        <div v-for="section in maxSections" :key="section" class="timetable-row">
          <div class="period-cell">第{{ section }}节</div>
          <div v-for="day in 7" :key="day" class="course-cell">
            <div
              v-for="item in getCellItems(day, section)"
              :key="item.courseScheduleId"
              class="course-block"
              :style="{
                backgroundColor: getCourseColor(item.courseName),
                height: (item.endSection - item.startSection + 1) * 48 + 'px'
              }"
            >
              <div class="block-name">{{ item.courseName }}</div>
              <div class="block-info">{{ item.classroomName }}</div>
              <div class="block-info">{{ item.classNames }}</div>
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

// 只在起始节次渲染课程块，避免重复
const getCellItems = (day, section) => {
  return timetableData.value.filter(
    t => t.weekDay === day && t.startSection === section
  )
}

const fetchSemesters = async () => {
  try {
    const res = await getSemesterList({ pageNum: 1, pageSize: 50 })
    semesterList.value = res.data?.list || []
    // 默认选中当前学期（isCurrent=true），而非第一个学期
    const current = semesterList.value.find(s => s.isCurrent)
    if (current) {
      semesterId.value = current.id
    } else if (semesterList.value.length > 0) {
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
.timetable-page { }

.toolbar { margin-bottom: 16px; }
.toolbar-label { margin-right: 8px; font-size: 14px; color: #606266; }

.timetable {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.timetable-header {
  display: flex;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.period-col {
  width: 80px;
  padding: 10px;
  text-align: center;
  font-weight: 600;
  color: #606266;
  border-right: 1px solid #e4e7ed;
}

.day-col {
  flex: 1;
  padding: 10px;
  text-align: center;
  border-right: 1px solid #e4e7ed;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.day-col:last-child { border-right: none; }

.timetable-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
}

.timetable-row:last-child { border-bottom: none; }

.period-cell {
  width: 80px;
  padding: 12px 8px;
  text-align: center;
  font-size: 13px;
  color: #909399;
  border-right: 1px solid #e4e7ed;
  background: #fafafa;
}

.course-cell {
  flex: 1;
  border-right: 1px solid #ebeef5;
  position: relative;
  min-height: 48px;
}

.course-cell:last-child { border-right: none; }

.course-block {
  position: absolute;
  left: 2px;
  right: 2px;
  top: 1px;
  border-radius: 6px;
  padding: 4px 6px;
  overflow: hidden;
  z-index: 1;
  color: #fff;
  font-size: 12px;
  line-height: 1.4;
}

.block-name {
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  line-height: 1.3;
}

.block-info {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 1px;
}

html.dark .timetable-header {
  background: #262727;
}

html.dark .period-col,
html.dark .day-col {
  color: #cfd3dc;
  border-color: #363637;
}

html.dark .timetable-row {
  border-color: #363637;
}

html.dark .period-cell {
  color: #909399;
  border-color: #363637;
  background: #1a1a1a;
}

html.dark .course-cell {
  border-color: #363637;
}
</style>
