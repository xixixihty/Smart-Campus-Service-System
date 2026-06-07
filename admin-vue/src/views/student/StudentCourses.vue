<template>
  <div class="page-container">
    <div class="page-header">
      <h2><el-icon><Notebook /></el-icon> 选课中心</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="选课中心" name="available">
        <el-card shadow="never">
          <el-form :inline="true" :model="queryForm" class="search-form">
            <el-form-item label="课程名称">
              <el-input v-model="queryForm.courseName" placeholder="请输入课程名称" clearable style="width: 200px" />
            </el-form-item>
            <el-form-item label="课程类型">
              <el-select v-model="queryForm.type" placeholder="请选择类型" clearable style="width: 150px">
                <el-option label="必修" value="必修" />
                <el-option label="选修" value="选修" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
              <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="table-card">
          <el-table :data="tableData" v-loading="loading" stripe border max-height="calc(100vh - 340px)">
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="courseName" label="课程名称" min-width="180" align="center" />
            <el-table-column prop="courseCode" label="课程代码" width="120" align="center" />
            <el-table-column prop="teacherName" label="授课教师" width="100" align="center" />
            <el-table-column prop="type" label="课程类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.type === '必修' ? 'success' : 'warning'" size="small">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="credit" label="学分" width="80" align="center" />
            <el-table-column prop="capacity" label="容量" width="80" align="center" />
            <el-table-column prop="selectedCount" label="已选人数" width="100" align="center" />
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleSelect(row)">
                  <el-icon><Plus /></el-icon>选课
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
              :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
              @size-change="fetchData" @current-change="fetchData" />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我的选课" name="my">
        <el-card shadow="never" class="table-card">
          <el-table :data="myTableData" v-loading="myLoading" stripe border max-height="calc(100vh - 280px)" empty-text="暂未选课，请前往选课中心选课">
            <el-table-column prop="courseId" label="课程ID" width="80" align="center" />
            <el-table-column prop="courseName" label="课程名称" min-width="180" align="center" />
            <el-table-column prop="credit" label="学分" width="80" align="center" />
            <el-table-column prop="semesterName" label="学期" width="160" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === '已选' ? 'success' : row.status === '候补' ? 'warning' : 'info'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === '已选'" type="danger" link @click="handleMyDrop(row)">
                  <el-icon><Close /></el-icon>退课
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentCourseList, selectCourse, dropCourse, getMyCourseSelection } from '@/api/student'

const activeTab = ref('available')
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const myLoading = ref(false)
const myTableData = ref([])

const queryForm = reactive({ courseName: '', type: '', pageNum: 1, pageSize: 10 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudentCourseList(queryForm)
    tableData.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载课程列表失败:', e)
  } finally {
    loading.value = false
  }
}

const fetchMyCourses = async () => {
  myLoading.value = true
  try {
    const res = await getMyCourseSelection()
    myTableData.value = res.data || []
  } catch (e) {
    console.error('加载我的选课失败:', e)
  } finally {
    myLoading.value = false
  }
}

const onTabChange = (tab) => {
  if (tab === 'my') {
    fetchMyCourses()
  }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchData() }
const handleReset = () => { Object.assign(queryForm, { courseName: '', type: '', pageNum: 1, pageSize: 10 }); fetchData() }

const handleSelect = async (row) => {
  try {
    await ElMessageBox.confirm(`确定选择课程「${row.courseName}」吗？`, '选课确认', { type: 'info' })
    await selectCourse({ courseId: row.id })
    ElMessage.success('选课成功')
    fetchData()
  } catch {}
}

const handleMyDrop = async (row) => {
  try {
    await ElMessageBox.confirm(`确定退选课程「${row.courseName}」吗？`, '退课确认', { type: 'warning' })
    await dropCourse(row.courseId)
    ElMessage.success('退课成功')
    fetchMyCourses()
    if (activeTab.value === 'available') fetchData()
  } catch {}
}

onMounted(() => fetchData())
</script>