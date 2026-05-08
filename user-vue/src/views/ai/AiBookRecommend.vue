<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Star /></el-icon> AI 图书推荐</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <div v-if="recommendations.length > 0" class="book-list">
        <el-row :gutter="16">
          <el-col :span="6" v-for="book in recommendations" :key="book.id">
            <el-card shadow="hover" class="book-card">
              <div class="book-cover">
                <el-icon :size="48" color="#409EFF"><CollectionTag /></el-icon>
              </div>
              <div class="book-info">
                <h4>{{ book.bookName }}</h4>
                <p class="book-author">{{ book.author }}</p>
                <p class="book-reason">{{ book.reason }}</p>
                <el-rate v-model="book.rating" disabled show-score text-color="#ff9900" size="small" />
              </div>
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
import { getBookRecommend } from '@/api/ai'

const loading = ref(false)
const recommendations = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getBookRecommend()
    recommendations.value = res.data || []
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }

.book-card { cursor: pointer; transition: transform 0.2s; }
.book-card:hover { transform: translateY(-4px); }
.book-cover { text-align: center; padding: 20px 0; background: #f0f5ff; border-radius: 8px; margin-bottom: 12px; }
.book-info h4 { font-size: 15px; color: #303133; margin-bottom: 4px; }
.book-author { font-size: 13px; color: #909399; margin-bottom: 6px; }
.book-reason { font-size: 12px; color: #67C23A; margin-bottom: 8px; line-height: 1.5; }
</style>
