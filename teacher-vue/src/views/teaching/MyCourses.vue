<template>
  <div class="my-courses-page">
    <h2 class="page-title">
      <el-icon><Notebook /></el-icon>
      我的课程
    </h2>

    <div class="toolbar">
      <span class="toolbar-label">学期：</span>
      <el-select v-model="semesterId" placeholder="请选择学期" @change="fetchCourses" style="width: 240px">
        <el-option v-for="s in semesterList" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
    </div>

    <el-card shadow="hover" v-loading="loading">
      <el-table :data="courseList" stripe style="width: 100%">
        <el-table-column prop="courseCode" label="课程代码" width="140" />
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="hours" label="学时" width="80" />
        <el-table-column prop="type" label="课程类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="row.type === 'REQUIRED' ? 'danger' : row.type === 'ELECTIVE' ? 'warning' : 'info'">
              {{ row.type === 'REQUIRED' ? '必修' : row.type === 'ELECTIVE' ? '选修' : row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' || row.status === '开课' ? 'success' : 'info'" size="small">
              {{ row.status === 'ENABLED' ? '开课' : row.status === '开课' ? '开课' : '停课' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="courseList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无课程数据" />
      </div>
    </el-card>

    <!-- 课程详情弹窗 -->
    <el-dialog v-model="detailVisible" title="课程详情" width="600px">
      <el-descriptions v-if="courseDetail" :column="2" border size="small">
        <el-descriptions-item label="课程代码">{{ courseDetail.courseCode }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ courseDetail.courseName }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ courseDetail.credit }}</el-descriptions-item>
        <el-descriptions-item label="学时">{{ courseDetail.hours }}</el-descriptions-item>
        <el-descriptions-item label="课程类型">{{ courseDetail.type }}</el-descriptions-item>
        <el-descriptions-item label="容量">{{ courseDetail.capacity }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ courseDetail.status }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ courseDetail.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTeachingCourses, getCourseDetail } from '@/api/course'
import { getSemesterList } from '@/api/semester'
import { Notebook, View } from '@element-plus/icons-vue'

const loading = ref(false)
const semesterId = ref(null)
const semesterList = ref([])
const courseList = ref([])
const detailVisible = ref(false)
const courseDetail = ref(null)

const fetchSemesters = async () => {
  try {
    const res = await getSemesterList({ pageNum: 1, pageSize: 50 })
    semesterList.value = res.data?.list || []
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

const fetchCourses = async () => {
  if (!semesterId.value) return
  loading.value = true
  try {
    const res = await getTeachingCourses(semesterId.value)
    courseList.value = res.data || []
  } catch {
    courseList.value = []
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await getCourseDetail(row.id)
    courseDetail.value = res.data
    detailVisible.value = true
  } catch {
    // ignore
  }
}

onMounted(async () => {
  await fetchSemesters()
  if (semesterId.value) {
    await fetchCourses()
  }
})
</script>