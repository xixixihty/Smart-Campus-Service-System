<template>
  <div class="my-classes-page">
    <h2 class="page-title">
      <el-icon><Grid /></el-icon>
      我的班级
    </h2>

    <div class="toolbar">
      <span class="toolbar-label">学期：</span>
      <el-select v-model="semesterId" placeholder="请选择学期" @change="fetchClasses" style="width: 240px">
        <el-option v-for="s in semesterList" :key="s.id" :label="s.semesterName" :value="s.id" />
      </el-select>
    </div>

    <el-card shadow="hover" v-loading="loading">
      <el-table :data="classList" stripe style="width: 100%">
        <el-table-column prop="className" label="班级名称" min-width="180" />
        <el-table-column prop="majorName" label="所属专业" min-width="150" />
        <el-table-column prop="headTeacherName" label="班主任" min-width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small">
              {{ row.status === 'ENABLED' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewStudents(row)">
              <el-icon><View /></el-icon> 花名册
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTeachingClasses } from '@/api/class'
import { getSemesterList } from '@/api/semester'
import { Grid, View } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const semesterId = ref(null)
const semesterList = ref([])
const classList = ref([])

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

const fetchClasses = async () => {
  if (!semesterId.value) return
  loading.value = true
  try {
    const res = await getTeachingClasses(semesterId.value)
    classList.value = res.data || []
  } catch {
    classList.value = []
  } finally {
    loading.value = false
  }
}

const viewStudents = (row) => {
  router.push(`/class-students/${row.id}`)
}

onMounted(async () => {
  await fetchSemesters()
  if (semesterId.value) {
    await fetchClasses()
  }
})
</script>