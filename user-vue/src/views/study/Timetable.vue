<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Calendar /></el-icon> 我的课表</h2>
      <div class="header-actions">
        <el-select v-model="currentWeek" placeholder="选择周次" style="width: 140px">
          <el-option v-for="w in 20" :key="w" :label="'第' + w + '周'" :value="w" />
        </el-select>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <div class="timetable">
        <div class="timetable-header">
          <div class="period-col">节次</div>
          <div v-for="(day, i) in weekDays" :key="i" class="day-col">
            <div class="day-name">星期{{ day }}</div>
          </div>
        </div>
        <div class="timetable-body">
          <div v-for="period in 12" :key="period" class="timetable-row">
            <div class="period-cell">第{{ period }}节</div>
            <div v-for="(day, di) in weekDays" :key="di" class="course-cell">
              <template v-for="item in getCellCourses(di + 1, period)" :key="item.id">
                <div
                  v-if="item.startPeriod === period"
                  class="course-block"
                  :style="{
                    height: (item.endPeriod - item.startPeriod + 1) * 48 + 'px',
                    background: getCourseColor(item.courseName)
                  }"
                  @click="showCourseDetail(item)"
                >
                  <div class="block-name">{{ item.courseName }}</div>
                  <div class="block-room">{{ item.classroomName }}</div>
                  <div class="block-teacher">{{ item.teacherName }}</div>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="课程详情" width="450px">
      <el-descriptions :column="1" border v-if="currentCourse">
        <el-descriptions-item label="课程名称">{{ currentCourse.courseName }}</el-descriptions-item>
        <el-descriptions-item label="授课教师">{{ currentCourse.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="上课地点">{{ currentCourse.classroomName }}</el-descriptions-item>
        <el-descriptions-item label="上课时间">
          星期{{ ['一', '二', '三', '四', '五', '六', '日'][currentCourse.weekDay - 1] }}
          第{{ currentCourse.startPeriod }}-{{ currentCourse.endPeriod }}节
        </el-descriptions-item>
        <el-descriptions-item label="教学周">第{{ currentCourse.startWeek }}-{{ currentCourse.endWeek }}周</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTimetable } from '@/api/courseSchedule'

const loading = ref(false)
const currentWeek = ref(1)
const detailVisible = ref(false)
const currentCourse = ref(null)
const weekDays = ['一', '二', '三', '四', '五', '六', '日']
const timetableData = ref([])

const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#1890ff', '#52c41a', '#fa8c16', '#eb2f96', '#722ed1']

const getCourseColor = (name) => {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

const getCellCourses = (day, period) => {
  return timetableData.value.filter(
    (item) => item.weekDay === day && item.startPeriod <= period && item.endPeriod >= period
  )
}

const showCourseDetail = (item) => {
  currentCourse.value = item
  detailVisible.value = true
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTimetable({ week: currentWeek.value })
    timetableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.header-actions { display: flex; gap: 8px; }

.timetable { border: 1px solid #e4e7ed; border-radius: 4px; overflow: hidden; }
.timetable-header { display: flex; background: #f5f7fa; border-bottom: 1px solid #e4e7ed; }
.period-col { width: 80px; padding: 10px; text-align: center; font-weight: 600; color: #606266; border-right: 1px solid #e4e7ed; }
.day-col { flex: 1; padding: 10px; text-align: center; border-right: 1px solid #e4e7ed; }
.day-col:last-child { border-right: none; }
.day-name { font-weight: 600; color: #303133; }

.timetable-body { }
.timetable-row { display: flex; border-bottom: 1px solid #ebeef5; }
.timetable-row:last-child { border-bottom: none; }
.period-cell { width: 80px; padding: 12px 8px; text-align: center; font-size: 13px; color: #909399; border-right: 1px solid #e4e7ed; background: #fafafa; }
.course-cell { flex: 1; border-right: 1px solid #ebeef5; position: relative; min-height: 48px; }
.course-cell:last-child { border-right: none; }

.course-block {
  position: absolute;
  left: 2px;
  right: 2px;
  top: 1px;
  border-radius: 4px;
  padding: 4px 6px;
  cursor: pointer;
  overflow: hidden;
  z-index: 1;
  transition: transform 0.15s;
}

.course-block:hover {
  transform: scale(1.02);
  z-index: 2;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.block-name { font-size: 12px; font-weight: 600; color: #fff; line-height: 1.3; }
.block-room { font-size: 11px; color: rgba(255, 255, 255, 0.85); margin-top: 2px; }
.block-teacher { font-size: 11px; color: rgba(255, 255, 255, 0.75); }
</style>
