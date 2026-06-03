<template>
  <div class="login-wrapper">
    <div class="login-bg">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
      <div class="bg-circle circle-4"></div>
      <div class="bg-circle circle-5"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="login-container">
      <div class="login-left">
        <div class="illustration">
          <div class="illu-center">
            <div class="center-icon">
              <el-icon :size="48"><School /></el-icon>
            </div>
          </div>
          <div class="illu-card card-1">
            <el-icon :size="28" color="#409EFF"><Notebook /></el-icon>
            <span>课程学习</span>
          </div>
          <div class="illu-card card-2">
            <el-icon :size="28" color="#67C23A"><CollectionTag /></el-icon>
            <span>图书借阅</span>
          </div>
          <div class="illu-card card-3">
            <el-icon :size="28" color="#E6A23C"><Chair /></el-icon>
            <span>座位预约</span>
          </div>
          <div class="illu-card card-4">
            <el-icon :size="28" color="#F56C6C"><Cpu /></el-icon>
            <span>AI助手</span>
          </div>
        </div>
      </div>

      <div class="login-right">
        <div class="login-card">
          <div class="card-header">
            <div class="header-icon">
              <el-icon :size="36" color="#409EFF"><School /></el-icon>
            </div>
            <h2>智慧校园</h2>
            <p class="header-sub">校园综合服务平台</p>
            <div class="header-divider"></div>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" size="large" class="login-form">
            <el-form-item>
              <div class="role-selector">
                <el-radio-group v-model="form.userType" size="default" @change="onRoleChange">
                  <el-radio-button value="student">学生</el-radio-button>
                </el-radio-group>
              </div>
            </el-form-item>

            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                :placeholder="usernamePlaceholder"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="login-btn"
                @click="handleLogin"
              >
                <span v-if="!loading">登 录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="card-footer">
            <div class="test-account">
              <el-icon :size="14"><InfoFilled /></el-icon>
              <span>测试账号：20233001 / Password123!</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="login-footer">
      <span>Copyright &copy; 2025 智慧校园. All rights reserved.</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const usernamePlaceholderMap = {
  student: '请输入学号',
  teacher: '请输入教师工号',
  admin: '请输入管理员账号'
}

const usernamePlaceholder = computed(() => usernamePlaceholderMap[form.userType] || '请输入账号')

const form = reactive({
  username: '',
  password: '',
  userType: 'student'
})

const rules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度在 6 到 30 个字符', trigger: 'blur' }
  ]
}

const onRoleChange = () => {
  formRef.value?.clearValidate()
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login({
      username: form.username,
      password: form.password,
      userType: form.userType
    })
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.name || form.username)
    localStorage.setItem('userId', res.data.userId)
    localStorage.setItem('userType', res.data.userType)
    ElMessage.success(`欢迎回来，${res.data.name || form.username}！`)
    router.push('/dashboard')
  } catch {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.login-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(160deg, #e8f4fd 0%, #d4e8f9 20%, #c5e0f5 40%, #e0f0e8 60%, #f0f5e8 80%, #fdf2e9 100%);
  z-index: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}

.circle-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #409EFF, transparent);
  top: -150px;
  right: -100px;
  animation: drift 12s ease-in-out infinite;
}

.circle-2 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, #67C23A, transparent);
  bottom: -80px;
  left: -60px;
  animation: drift 10s ease-in-out infinite reverse;
}

.circle-3 {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, #E6A23C, transparent);
  top: 50%;
  left: 5%;
  animation: drift 8s ease-in-out infinite;
}

.circle-4 {
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, #F56C6C, transparent);
  top: 15%;
  right: 25%;
  animation: drift 9s ease-in-out infinite reverse;
}

.circle-5 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #909399, transparent);
  bottom: 20%;
  right: 10%;
  animation: drift 11s ease-in-out infinite;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(64, 158, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(64, 158, 255, 0.04) 1px, transparent 1px);
  background-size: 60px 60px;
}

@keyframes drift {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(20px, -20px) scale(1.05); }
  66% { transform: translate(-15px, 15px) scale(0.95); }
}

.login-container {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 60px;
}

.login-left {
  flex: 1;
  max-width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.illustration {
  position: relative;
  width: 400px;
  height: 400px;
}

.illu-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
}

.center-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #409EFF, #67C23A);
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 12px 40px rgba(64, 158, 255, 0.3);
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 12px 40px rgba(64, 158, 255, 0.3); }
  50% { box-shadow: 0 12px 60px rgba(64, 158, 255, 0.5); }
}

.illu-card {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: transform 0.3s;
  min-width: 90px;
}

.illu-card:hover {
  transform: scale(1.05);
}

.card-1 {
  top: 5%;
  left: 50%;
  transform: translateX(-50%);
  animation: floatA 3.5s ease-in-out infinite;
}

.card-2 {
  top: 50%;
  right: 2%;
  transform: translateY(-50%);
  animation: floatB 4s ease-in-out infinite 0.5s;
}

.card-3 {
  bottom: 5%;
  left: 50%;
  transform: translateX(-50%);
  animation: floatA 3.5s ease-in-out infinite 1s;
}

.card-4 {
  top: 50%;
  left: 2%;
  transform: translateY(-50%);
  animation: floatB 4s ease-in-out infinite 1.5s;
}

@keyframes floatA {
  0%, 100% { transform: translateX(-50%) translateY(0); }
  50% { transform: translateX(-50%) translateY(-8px); }
}

@keyframes floatB {
  0%, 100% { transform: translateY(-50%) translateX(0); }
  50% { transform: translateY(-50%) translateX(6px); }
}

.login-right {
  flex-shrink: 0;
}

.login-card {
  width: 420px;
  padding: 44px 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 24px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.08), 0 0 0 1px rgba(0, 0, 0, 0.04);
  backdrop-filter: blur(20px);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.header-icon {
  margin-bottom: 16px;
}

.card-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a3a5c;
  margin-bottom: 6px;
  letter-spacing: 2px;
}

.header-sub {
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
}

.header-divider {
  width: 50px;
  height: 3px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  border-radius: 2px;
  margin: 0 auto;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
  transition: all 0.3s;
  padding: 4px 14px;
  background: #f8fafc;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
  background: #fff;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409EFF inset;
  background: #fff;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.login-btn {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  border: none;
  transition: all 0.3s;
  margin-top: 4px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(64, 158, 255, 0.35);
}

.login-btn:active {
  transform: translateY(0);
}

.card-footer {
  margin-top: 28px;
  text-align: center;
}

.role-selector {
  display: flex;
  justify-content: center;
}

.test-account {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: linear-gradient(135deg, #f0f5ff, #f0f9eb);
  border-radius: 10px;
  font-size: 12px;
  color: #606266;
  border: 1px solid #e8f0fe;
}

.login-footer {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 16px;
  font-size: 12px;
  color: #909399;
}

@media (max-width: 900px) {
  .login-left {
    display: none;
  }
  .login-container {
    padding: 20px;
    gap: 0;
  }
  .login-card {
    width: 100%;
    max-width: 400px;
    padding: 36px 28px;
  }
}
</style>
