<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getAdminProfile, updateAdminProfile, changePassword, uploadFile } from '@/api/admin'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()

const form = reactive({
  nickname: 'Orange',
  email: '',
  bio: '一名正在持续成长中的全栈开发者'
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const avatarUrl = ref('')
const saving = ref(false)
const changingPwd = ref(false)

onMounted(async () => {
  try {
    const res = await getAdminProfile()
    if (res.code === 200 && res.data) {
      const p = res.data
      form.nickname = p.nickname || form.nickname
      form.email = p.email || form.email
      if (p.bio) form.bio = p.bio
      if (p.avatar) avatarUrl.value = p.avatar
    }
  } catch (e) {
    console.error('获取个人信息失败', e)
  }
})

async function handleSaveProfile() {
  saving.value = true
  try {
    const res = await updateAdminProfile({
      nickname: form.nickname,
      email: form.email,
      bio: form.bio
    })
    if (res.code === 200) {
      ElMessage.success('个人设置保存成功')
      // 更新本地 store
      if (authStore.userInfo) {
        authStore.userInfo.nickname = form.nickname
        authStore.userInfo.email = form.email
      }
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleChangePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  changingPwd.value = true
  try {
    const res = await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    changingPwd.value = false
  }
}

function handleUploadAvatar() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.jpg,.jpeg,.png,.gif,.webp'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('文件不能超过 5MB')
      return
    }
    try {
      const res = await uploadFile(file)
      if (res.code === 200 && res.data) {
        avatarUrl.value = res.data.url
        ElMessage.success('头像上传成功')
      }
    } catch (err) {
      ElMessage.error('上传失败')
    }
  }
  input.click()
}
</script>

<template>
  <div class="admin-profile">
    <div class="profile-grid">
      <!-- 基本信息 -->
      <div class="card">
        <h4 class="card-title">基本信息</h4>
        <div class="avatar-section">
          <div class="avatar-preview" @click="handleUploadAvatar">
            <span v-if="!avatarUrl">{{ (form.nickname || 'YH').charAt(0) }}</span>
            <img v-else :src="avatarUrl" alt="avatar" />
          </div>
          <span class="avatar-hint">点击更换头像</span>
        </div>
        <el-form :model="form" label-position="top">
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" />
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input v-model="form.bio" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item>
            <el-button type="success" :loading="saving" @click="handleSaveProfile" style="width:100%">
              {{ saving ? '保存中...' : '保存修改' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 修改密码 -->
      <div class="card">
        <h4 class="card-title">修改密码</h4>
        <el-form :model="passwordForm" label-position="top">
          <el-form-item label="当前密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="warning" :loading="changingPwd" @click="handleChangePassword" style="width:100%">
              {{ changingPwd ? '修改中...' : '修改密码' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

.card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

.card-title {
  font-size: var(--font-size-md);
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--color-border-light);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity var(--transition-fast);
  overflow: hidden;
}

.avatar-preview:hover {
  opacity: 0.8;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-hint {
  font-size: var(--font-size-xs);
  color: var(--color-muted);
  margin-top: 8px;
}

@media (max-width: 768px) {
  .profile-grid { grid-template-columns: 1fr; }
}
</style>
