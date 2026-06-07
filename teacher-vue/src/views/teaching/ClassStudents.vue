<template>
  <div class="class-students-page">
    <h2 class="page-title">
      <el-icon><User /></el-icon>
      {{ className }} - 学生花名册
    </h2>

    <div class="toolbar">
      <el-button @click="router.back()">
        <el-icon><Back /></el-icon> 返回
      </el-button>
    </div>

    <el-card shadow="hover" v-loading="loading">
      <div v-if="classDetail" class="class-info">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="班级名称">{{ classDetail.className }}</el-descriptions-item>
          <el-descriptions-item label="所属专业">{{ classDetail.majorName }}</el-descriptions-item>
          <el-descriptions-item label="班主任">{{ classDetail.headTeacherName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="classDetail.status === 'ENABLED' ? 'success' : 'info'" size="small">
              {{ classDetail.status === 'ENABLED' ? '启用' : '停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ classDetail.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider />

      <el-table :data="students" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="studentNo" label="学号" min-width="140" />
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 'MALE' ? '男' : row.gender === 'FEMALE' ? '女' : row.gender }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' || row.status === '正常' || row.status === '在读' ? 'success' : 'info'" size="small">
              {{ row.status === 'ENABLED' ? '在读' : row.status === '正常' ? '正常' : row.status || '-' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="students.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无学生数据" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getClassDetail, getClassStudents } from '@/api/class'
import { User, Back } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const classId = ref(route.params.classId)
const className = ref('')
const loading = ref(false)
const classDetail = ref(null)
const students = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const [detailRes, studentsRes] = await Promise.all([
      getClassDetail(classId.value),
      getClassStudents(classId.value)
    ])
    classDetail.value = detailRes.data
    className.value = detailRes.data?.className || '班级详情'
    students.value = studentsRes.data || []
  } catch {
    students.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.class-info {
  margin-bottom: 8px;
}
</style>