<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Guide /></el-icon> AI 课程推荐</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <div v-if="recommendations.length > 0">
        <el-row :gutter="16">
          <el-col :span="8" v-for="course in recommendations" :key="course.id">
            <el-card shadow="hover" class="course-card">
              <div class="course-header">
                <el-tag :type="course.courseType === '必修' ? 'danger' : 'warning'" size="small">
                  {{ course.courseType }}
                </el-tag>
                <span class="course-credit">{{ course.credit }}学分</span>
              </div>
              <h4>{{ course.courseName }}</h4>
              <p class="course-teacher"><el-icon><User /></el-icon> {{ course.teacherName }}</p>
              <p class="course-reason">{{ course.reason }}</p>
              <el-rate v-model="course.rating" disabled show-score text-color="#ff9900" size="small" />
            </el-card>
          </el-col>
        </el-row>
      </div>
      <el-empty v-else description="暂无推荐数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCourseRecommend } from '@/api/ai'

const loading = ref(false)
const recommendations = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCourseRecommend()
    recommendations.value = res.data || []
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }

.course-card { cursor: pointer; transition: transform 0.2s; }
.course-card:hover { transform: translateY(-4px); }
.course-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.course-credit { font-size: 13px; color: #909399; }
.course-card h4 { font-size: 16px; color: #303133; margin-bottom: 8px; }
.course-teacher { font-size: 13px; color: #606266; margin-bottom: 8px; display: flex; align-items: center; gap: 4px; }
.course-reason { font-size: 12px; color: #67C23A; margin-bottom: 8px; line-height: 1.5; }
</style>
