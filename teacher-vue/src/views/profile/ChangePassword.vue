<template>
  <div class="password-page">
    <h2 class="page-title">
      <el-icon><Lock /></el-icon>
      修改密码
    </h2>

    <el-card shadow="hover" style="max-width: 500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="default">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="form.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            <span v-if="!submitting">确认修改</span>
            <span v-else>修改中...</span>
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { updatePassword } from '@/api/myInfo'
import { Lock } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度在 6 到 30 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await updatePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.clear()
    router.replace('/login')
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  formRef.value?.resetFields()
}
</script>