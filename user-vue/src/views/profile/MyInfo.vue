<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><User /></el-icon> 个人信息</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <template v-if="info">
        <el-descriptions :column="2" border size="default" class="info-descriptions">
          <el-descriptions-item label="姓名" :span="1">{{ info.name }}</el-descriptions-item>
          <el-descriptions-item label="学号" :span="1">{{ info.userNo }}</el-descriptions-item>
          <el-descriptions-item label="性别" :span="1">{{ info.gender }}</el-descriptions-item>
          <el-descriptions-item label="所属学院" :span="1">{{ info.collegeName }}</el-descriptions-item>
          <el-descriptions-item v-if="info.majorName" label="专业" :span="1">{{ info.majorName }}</el-descriptions-item>
          <el-descriptions-item v-if="info.className" label="班级" :span="1">{{ info.className }}</el-descriptions-item>
          <el-descriptions-item v-if="info.enrollmentDate" label="入学时间" :span="1">{{ formatDateTime(info.enrollmentDate) }}</el-descriptions-item>
          <el-descriptions-item label="手机号" :span="1">{{ info.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="info.idCard" label="身份证号" :span="2">{{ info.idCard }}</el-descriptions-item>
          <el-descriptions-item label="账号状态" :span="1">
            <el-tag :type="info.accountStatus === '正常' ? 'success' : 'danger'" size="small">{{ info.accountStatus }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态" :span="1">
            <el-tag :type="info.status === '正常' || info.status === '在读' ? 'success' : 'danger'" size="small">{{ info.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(info.createTime) }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <el-empty v-else description="暂无个人信息" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User } from '@element-plus/icons-vue'
import { getMyInfo } from '@/api/myInfo'

const loading = ref(false)
const info = ref(null)

const formatDateTime = (val) => {
  if (!val) return '-'
  return String(val).replace('T', ' ')
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getMyInfo()
    info.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-container { padding: 0; }
.info-descriptions { margin-top: 8px; }
</style>
