<template>
  <el-tag
    :type="resolvedType"
    size="small"
    class="status-badge"
    :class="`status-badge--${mode}`"
  >
    <template v-if="mode === 'syncStatus' && status === '已同步'">
      <el-icon class="status-badge__icon"><Select /></el-icon>
      {{ status }}
    </template>
    <template v-else-if="mode === 'syncStatus' && status !== '已同步'">
      <el-icon class="status-badge__icon status-badge__icon--circle"><Warning /></el-icon>
      {{ status || '未同步' }}
    </template>
    <template v-else>
      {{ status }}
    </template>
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'
import { Select, Warning } from '@element-plus/icons-vue'

const props = defineProps({
  /** 状态值，如：启用/禁用/正常/待审批 等 */
  status: { type: String, default: '' },
  /** 模式：status=通用状态，syncStatus=同步状态 */
  mode: { type: String, default: 'status' },
  /** 自定义颜色映射，可覆盖默认映射 */
  typeMap: { type: Object, default: () => ({}) }
})

/**
 * 默认状态值 -> el-tag type 映射
 * 覆盖智慧校园全业务域的所有状态值
 */
const defaultTypeMap = {
  // === 成功/正常类（绿色） ===
  '启用': 'success', '正常': 'success', '在职': 'success', '在读': 'success',
  '可用': 'success', '开课': 'success', '发布': 'success', '已发布': 'success',
  '已同步': 'success', '已归还': 'success', '已考': 'success',
  '已批准': 'success', '已选': 'success', '进行中': 'success', '空闲': 'success',
  'ACTIVE': 'success', 'ENROLLED': 'success',

  // === 危险/异常类（红色） ===
  '禁用': 'danger', '停用': 'danger', '离职': 'danger', ' resigned': 'danger',
  '休学': 'danger', '退学': 'danger', '已结束': 'danger', '逾期': 'danger',
  '已驳回': 'danger', '已取消': 'danger', 'DISABLED': 'danger', 'DROPPED': 'danger',
  'SUSPENDED': 'danger', 'LOCKED': 'danger',

  // === 警告/进行中类（橙色） ===
  '未开始': 'warning', '待审批': 'warning', '待考': 'warning',
  '借出中': 'warning', '借阅中': 'warning', '使用中': 'warning', '暂离': 'warning',
  '候补': 'warning', 'PENDING': 'warning',

  // === 信息/中性类（灰色） ===
  '维修': 'info', '毕业': 'info', 'GRADUATED': 'info',
  '未同步': 'info', 'INACTIVE': 'info', 'COMPLETED': 'info',
  'REVOKED': 'info', 'CLOSED': 'info',

  // === 课程类型 ===
  '必修': 'success', '选修': 'warning', '公选': 'info',

  // === 请假类型 ===
  '事假': 'warning', '病假': 'danger', '公假': 'success',

  // === 性别（辅助） ===
  '男': 'info', '女': 'danger',

  // === 申请人类型 ===
  '学生': '', '教师': 'success'
}

const resolvedType = computed(() => {
  if (props.typeMap[props.status] !== undefined) {
    return props.typeMap[props.status]
  }
  return defaultTypeMap[props.status] || 'info'
})
</script>

<style scoped>
/* 状态标签统一样式 - 圆角胶囊状，对标参考图 */
.status-badge {
  border-radius: 12px !important;
  padding: 4px 14px !important;
  font-size: 13px !important;
  font-weight: 500 !important;
  border: none !important;
  display: inline-flex !important;
  align-items: center !important;
  gap: 4px !important;
}

/* 同步状态标签特殊样式 */
.status-badge--syncStatus {
  min-width: 72px;
  justify-content: center;
}

.status-badge__icon {
  font-size: 12px;
}

.status-badge__icon--circle {
  color: #909399;
}
</style>
