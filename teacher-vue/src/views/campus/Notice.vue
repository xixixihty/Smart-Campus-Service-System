<template>
  <div class="notice-page">
    <h2 class="page-title">
      <el-icon><Bell /></el-icon>
      通知中心
    </h2>

    <el-card shadow="hover" v-loading="loading">
      <el-table :data="noticeList" stripe style="width: 100%" @row-click="viewDetail">
        <el-table-column prop="title" label="标题" min-width="300">
          <template #default="{ row }">
            <div class="notice-title">
              <el-badge v-if="row.isRead === false" is-dot class="unread-dot" />
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="120" />
        <el-table-column prop="publishTime" label="发布时间" min-width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click.stop="viewDetail(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="noticeList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无通知" />
      </div>
    </el-card>

    <!-- 通知详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="noticeDetail?.title || '通知详情'" width="650px">
      <div v-if="noticeDetail" class="notice-detail">
        <div class="notice-meta">
          <span>发布人：{{ noticeDetail.publisherName }}</span>
          <span>发布时间：{{ noticeDetail.publishTime }}</span>
        </div>
        <el-divider />
        <div class="notice-content" v-html="noticeDetail.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyNotices, getNoticeDetail } from '@/api/notice'
import { Bell, View } from '@element-plus/icons-vue'

const loading = ref(false)
const noticeList = ref([])
const detailVisible = ref(false)
const noticeDetail = ref(null)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMyNotices({ pageNum: 1, pageSize: 50 })
    noticeList.value = res.data?.list || []
  } catch {
    noticeList.value = []
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await getNoticeDetail(row.id)
    noticeDetail.value = res.data
    detailVisible.value = true
  } catch {
    // ignore
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.unread-dot {
  flex-shrink: 0;
}

.notice-detail .notice-meta {
  display: flex;
  gap: 24px;
  color: #909399;
  font-size: 13px;
}

.notice-content {
  line-height: 1.8;
  font-size: 14px;
  color: #303133;
}

html.dark .notice-content {
  color: #cfd3dc;
}
</style>