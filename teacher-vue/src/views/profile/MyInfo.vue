<template>
  <div class="profile-page">
    <h2 class="page-title">
      <el-icon><User /></el-icon>
      个人信息
    </h2>

    <el-card shadow="hover" v-loading="loading" style="max-width: 700px">
      <el-descriptions v-if="userInfo" :column="2" border size="default" label-width="120">
        <el-descriptions-item label="姓名">{{ userInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ userInfo.gender === 'MALE' ? '男' : userInfo.gender === 'FEMALE' ? '女' : userInfo.gender || '--' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userInfo.phone || '--' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfo.email || '--' }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ userInfo.title || '--' }}</el-descriptions-item>
        <el-descriptions-item label="所属学院" :span="2">{{ userInfo.collegeName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="入职时间" :span="2">{{ userInfo.joinDate || '--' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else-if="!loading" description="无法获取个人信息">
        <el-button type="primary" @click="fetchInfo">重新加载</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyInfo } from '@/api/myInfo'
import { User } from '@element-plus/icons-vue'

const loading = ref(false)
const userInfo = ref(null)

const fetchInfo = async () => {
  loading.value = true
  try {
    const res = await getMyInfo()
    userInfo.value = res.data
  } catch {
    userInfo.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchInfo()
})
</script>