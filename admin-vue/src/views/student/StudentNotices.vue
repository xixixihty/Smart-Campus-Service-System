<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Bell /></el-icon> 通知公告</h2>
    </div>

    <el-card shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="queryForm.title" placeholder="请输入标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border max-height="calc(100vh - 280px)">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="发布人" width="100" align="center" />
        <el-table-column prop="publishTime" label="发布时间" width="170" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link @click="handleView(row)"><el-icon><View /></el-icon>详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="通知详情" width="600px" destroy-on-close>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ detailData.publisherName }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ detailData.publishTime }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div v-html="detailData.content" style="max-height: 400px; overflow-y: auto" />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStudentNoticeList, getStudentNoticeDetail } from '@/api/student'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detailData = ref({})

const queryForm = reactive({ title: '', pageNum: 1, pageSize: 10 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudentNoticeList(queryForm)
    tableData.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载通知列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { Object.assign(queryForm, { title: '', pageNum: 1, pageSize: 10 }); fetchData() }

const handleView = async (row) => {
  try {
    const res = await getStudentNoticeDetail(row.id)
    detailData.value = res.data || {}
    detailVisible.value = true
  } catch (e) {
    console.error('加载通知详情失败:', e)
  }
}

onMounted(() => fetchData())
</script>