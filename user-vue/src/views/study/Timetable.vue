<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Calendar /></el-icon> 我的课表</h2>
      <div class="header-actions">
        <el-button type="primary" size="small" @click="goToToday">
          <el-icon><Sunny /></el-icon>今天
        </el-button>
        <el-select v-model="currentWeek" placeholder="选择周次" style="width: 140px">
          <el-option v-for="w in 20" :key="w" :label="'第' + w + '周'" :value="w" />
        </el-select>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <div class="timetable">
        <div class="timetable-header">
          <div class="period-col">节次</div>
          <div v-for="(day, i) in weekDays" :key="i" class="day-col" :class="{ 'today-col': i === todayDayIndex }">
            <div class="day-name">星期{{ day }}</div>
            <div v-if="i === todayDayIndex" class="today-badge">今天</div>
          </div>
        </div>
        <div class="timetable-body">
          <div v-for="period in 12" :key="period" class="timetable-row">
            <div class="period-cell">第{{ period }}节</div>
            <div v-for="(day, di) in weekDays" :key="di" class="course-cell" :class="{ 'today-course-cell': di === todayDayIndex }">
              <template v-for="item in getCellCourses(di + 1, period)" :key="item.id">
                <div
                  v-if="item.startPeriod === period"
                  class="course-block"
                  :style="{
                    height: (item.endPeriod - item.startPeriod + 1) * 48 + 'px',
                    background: getCourseColor(item.courseName),
                    left: item._leftStyle,
                    right: item._overlapCount > 1 ? 'auto' : '2px',
                    width: item._widthStyle
                  }"
                  @click="showCourseDetail(item)"
                >
                  <div class="block-name">{{ item.courseName }}</div>
                  <div class="block-class">{{ item.classNames }}</div>
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
        <el-descriptions-item label="授课班级">{{ currentCourse.classNames }}</el-descriptions-item>
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
import { ref, computed, onMounted } from 'vue'
import { getTimetable } from '@/api/courseSchedule'
import { getCurrentSemester } from '@/api/semester'

const loading = ref(false)
const currentWeek = ref(null)
const currentSemesterId = ref(null)
const detailVisible = ref(false)
const currentCourse = ref(null)
const weekDays = ['一', '二', '三', '四', '五', '六', '日']
const timetableData = ref([])

const todayWeek = ref(1)

const todayDayIndex = computed(() => {
  const day = new Date().getDay()
  return day === 0 ? 6 : day - 1
})

const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#1890ff', '#52c41a', '#fa8c16', '#eb2f96', '#722ed1']

const getCourseColor = (name) => {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

const getCellCourses = (day, period) => {
  const courses = timetableData.value.filter((item) => {
    if (item.weekDay !== day) return false
    if (item.startSection > period || item.endSection < period) return false
    const { startWeek, endWeek } = parseWeekRange(item.weekRange)
    if (currentWeek.value && startWeek > 0 && endWeek > 0) {
      if (currentWeek.value < startWeek || currentWeek.value > endWeek) return false
    }
    return true
  })

  const slotMap = new Map()
  courses.forEach((c) => {
    const key = `${c.startSection}-${c.endSection}`
    if (!slotMap.has(key)) slotMap.set(key, [])
    slotMap.get(key).push(c)
  })
  for (const group of slotMap.values()) {
    group.forEach((c, i) => {
      c._overlapCount = group.length
      c._overlapIndex = i
      c._leftStyle = c._overlapCount > 1
        ? `calc(${c._overlapIndex * 100 / c._overlapCount}% + 2px)`
        : '2px'
      c._widthStyle = c._overlapCount > 1
        ? `calc(${100 / c._overlapCount}% - 4px)`
        : 'auto'
    })
  }

  return courses
}

const parseWeekRange = (weekRange) => {
  if (!weekRange) return { startWeek: 0, endWeek: 0 }
  const match = weekRange.match(/(\d+)-(\d+)/)
  if (match) {
    return { startWeek: parseInt(match[1]), endWeek: parseInt(match[2]) }
  }
  return { startWeek: 0, endWeek: 0 }
}

const goToToday = () => {
  currentWeek.value = todayWeek.value
}

const showCourseDetail = (item) => {
  const weekInfo = parseWeekRange(item.weekRange)
  currentCourse.value = {
    ...item,
    startWeek: weekInfo.startWeek,
    endWeek: weekInfo.endWeek,
    startPeriod: item.startSection,
    endPeriod: item.endSection
  }
  detailVisible.value = true
}

const calculateCurrentWeek = async () => {
  try {
    const res = await getCurrentSemester()
    const semester = res.data
    if (semester && semester.startDate) {
      const startDate = new Date(semester.startDate)
      const today = new Date()
      const diffDays = Math.floor((today - startDate) / (1000 * 60 * 60 * 24))
      const week = Math.max(1, Math.floor(diffDays / 7) + 1)
      todayWeek.value = week
    }
    if (semester && semester.id) {
      currentSemesterId.value = semester.id
    }
  } catch {
    todayWeek.value = 1
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTimetable({ semesterId: currentSemesterId.value })
    const rawData = res.data || []
    timetableData.value = rawData.map(item => ({
      ...item,
      id: item.courseScheduleId,
      startPeriod: item.startSection,
      endPeriod: item.endSection
    }))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await calculateCurrentWeek()
  currentWeek.value = todayWeek.value
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.header-actions { display: flex; gap: 8px; }

.timetable { border: 1px solid #e4e7ed; border-radius: 4px; overflow: hidden; }
.timetable-header { display: flex; background: #f5f7fa; border-bottom: 1px solid #e4e7ed; }
.period-col { width: 80px; padding: 10px; text-align: center; font-weight: 600; color: #606266; border-right: 1px solid #e4e7ed; }
.day-col { flex: 1; padding: 10px; text-align: center; border-right: 1px solid #e4e7ed; position: relative; }
.day-col:last-child { border-right: none; }
.today-col { background: #e6f7ff; }
.today-badge { font-size: 10px; color: #fff; background: #1890ff; border-radius: 8px; padding: 0 6px; line-height: 16px; position: absolute; top: 2px; right: 4px; }
.day-name { font-weight: 600; color: #303133; }

.timetable-body { }
.timetable-row { display: flex; border-bottom: 1px solid #ebeef5; }
.timetable-row:last-child { border-bottom: none; }
.period-cell { width: 80px; padding: 12px 8px; text-align: center; font-size: 13px; color: #909399; border-right: 1px solid #e4e7ed; background: #fafafa; }
.course-cell { flex: 1; border-right: 1px solid #ebeef5; position: relative; min-height: 48px; }
.course-cell:last-child { border-right: none; }
.today-course-cell { background: #f0f9ff; }

.course-block {
  position: absolute;
  left: 2px;
  right: 2px;
  top: 1px;
  border-radius: 6px;
  padding: 4px 6px;
  cursor: pointer;
  overflow: hidden;
  z-index: 1;
  transition: transform 0.15s, box-shadow 0.15s;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.25), 0 1px 3px rgba(0,0,0,0.12);
}

.course-block:hover {
  transform: scale(1.03);
  z-index: 2;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.25), 0 4px 16px rgba(0,0,0,0.2);
}

.block-name { font-size: 12px; font-weight: 600; color: #fff; line-height: 1.3; }
.block-class { font-size: 10px; color: rgba(255, 255, 255, 0.9); margin-top: 1px; }
.block-room { font-size: 11px; color: rgba(255, 255, 255, 0.85); margin-top: 2px; }
.block-teacher { font-size: 11px; color: rgba(255, 255, 255, 0.75); }
</style>
