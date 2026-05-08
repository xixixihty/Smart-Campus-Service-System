<template>
  <div class="login-wrapper">
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
      <div class="bg-shape shape-4"></div>
      <div class="bg-dots"></div>
    </div>

    <div class="login-container">
      <div class="login-left">
        <div class="brand-section">
          <div class="brand-icon">
            <el-icon :size="56"><School /></el-icon>
          </div>
          <h1 class="brand-title">智慧校园</h1>
          <p class="brand-subtitle">Smart Campus Management System</p>
          <div class="brand-divider"></div>
          <p class="brand-desc">数字化校园管理平台，助力教育信息化</p>
        </div>
        <div class="feature-list">
          <div class="feature-item">
            <el-icon :size="20"><UserFilled /></el-icon>
            <span>师生信息管理</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><Notebook /></el-icon>
            <span>教学教务管理</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><CollectionTag /></el-icon>
            <span>图书借阅管理</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><Place /></el-icon>
            <span>座位预约管理</span>
          </div>
        </div>
      </div>

      <div class="login-right">
        <div class="login-card">
          <div class="card-header">
            <h2>管理员登录</h2>
            <p>请使用教师工号登录系统</p>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" size="large" class="login-form" autocomplete="off">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入教师工号"
                :prefix-icon="User"
                clearable
                autocomplete="off"
                name="username-field"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                autocomplete="new-password"
                name="password-field"
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
              <span>测试账号：T2021001 / Password123!</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="login-footer">
      <span>Copyright &copy; 2025 智慧校园管理系统. All rights reserved.</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  userType: 'teacher'
})

const rules = {
  username: [
    { required: true, message: '请输入教师工号', trigger: 'blur' },
    { min: 3, max: 20, message: '工号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度在 6 到 30 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login({
      username: form.username.trim(),
      password: form.password,
      userType: form.userType
    })
    const token = res.data?.token
    if (!token || token === 'undefined' || token === 'null') {
      ElMessage.error('登录响应异常，未获取到有效令牌')
      return
    }
    localStorage.setItem('token', token)
    localStorage.setItem('username', res.data.name || form.username)
    localStorage.setItem('userId', res.data.userId)
    localStorage.setItem('userType', res.data.userType)
    ElMessage.success(`欢迎回来，${res.data.name || form.username}！`)
    await router.replace('/dashboard')
  } catch (e) {
    if (e?.message?.includes('Redirected')) {
      return
    }
    ElMessage.error(e?.message || '登录失败，请检查网络连接')
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
  background: linear-gradient(135deg, #0f1b2d 0%, #152238 30%, #1a3a5c 60%, #1e4d7b 100%);
  z-index: 0;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}

.shape-1 {
  width: 600px;
  height: 600px;
  background: #409EFF;
  top: -200px;
  right: -150px;
  animation: float 8s ease-in-out infinite;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: #67C23A;
  bottom: -100px;
  left: -100px;
  animation: float 10s ease-in-out infinite reverse;
}

.shape-3 {
  width: 300px;
  height: 300px;
  background: #E6A23C;
  top: 40%;
  left: 10%;
  animation: float 7s ease-in-out infinite;
}

.shape-4 {
  width: 200px;
  height: 200px;
  background: #F56C6C;
  top: 20%;
  right: 30%;
  animation: float 9s ease-in-out infinite reverse;
}

.bg-dots {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255, 255, 255, 0.06) 1px, transparent 1px);
  background-size: 40px 40px;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

.login-container {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 80px;
}

.login-left {
  max-width: 420px;
  color: #fff;
}

.brand-section {
  margin-bottom: 48px;
}

.brand-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 4px;
  margin-bottom: 8px;
}

.brand-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  letter-spacing: 2px;
  text-transform: uppercase;
}

.brand-divider {
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  border-radius: 2px;
  margin: 24px 0;
}

.brand-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
}

.feature-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
}

.login-right {
  flex-shrink: 0;
}

.login-card {
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-header {
  text-align: center;
  margin-bottom: 36px;
}

.card-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #1a3a5c;
  margin-bottom: 8px;
}

.card-header p {
  font-size: 14px;
  color: #909399;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
  transition: all 0.3s;
  padding: 4px 12px;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409EFF inset;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #409EFF 0%, #2d6aa0 100%);
  border: none;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.card-footer {
  margin-top: 24px;
  text-align: center;
}

.test-account {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f0f5ff;
  border-radius: 8px;
  font-size: 12px;
  color: #606266;
}

.login-footer {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 16px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
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
