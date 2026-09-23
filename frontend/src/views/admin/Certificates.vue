<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAdminCertificates, createCertificate, updateCertificate, deleteCertificate, uploadFile } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([]); const loading = ref(false)
const searchKeyword = ref(''); const filterCategory = ref('')
const drawerVisible = ref(false); const editingId = ref(null)
const form = ref({ name: '', category: '厂商认证', issuer: '', issueDate: '', description: '', credentialNo: '', verificationUrl: '', imageUrl: '', visible: true, featured: false, sortOrder: 0 })
const uploading = ref(false)

const categories = ['厂商认证', '语言能力', '职业资格', '荣誉奖项']
const filtered = computed(() => {
  let r = [...list.value]
  if (searchKeyword.value) { const kw = searchKeyword.value.toLowerCase(); r = r.filter(c => c.name.toLowerCase().includes(kw) || c.issuer.toLowerCase().includes(kw)) }
  if (filterCategory.value) r = r.filter(c => c.category === filterCategory.value)
  return r
})

onMounted(fetchList)
async function fetchList() { loading.value = true; try { const res = await getAdminCertificates(); if (res.code === 200) list.value = res.data || [] } catch {} finally { loading.value = false } }

function openDrawer(item) {
  editingId.value = item?.id || null
  form.value = item ? { ...item, visible: item.visible === 1, featured: item.featured === 1 } : { name: '', category: '厂商认证', issuer: '', issueDate: '', description: '', credentialNo: '', verificationUrl: '', imageUrl: '', visible: true, featured: false, sortOrder: 0 }
  drawerVisible.value = true
}

async function handleSave() {
  if (!form.value.name) return ElMessage.warning('请输入证书名称')
  try {
    if (editingId.value) { await updateCertificate(editingId.value, form.value); ElMessage.success('更新成功') }
    else { await createCertificate(form.value); ElMessage.success('新增成功') }
    drawerVisible.value = false; await fetchList()
  } catch { ElMessage.error('操作失败') }
}

async function handleDelete(item) {
  try { await ElMessageBox.confirm(`删除「${item.name}」？`, '确认', { type: 'warning' }); await deleteCertificate(item.id); ElMessage.success('已删除'); await fetchList() } catch {}
}

async function toggleVisible(item) {
  try { await updateCertificate(item.id, { visible: !item.visible }); item.visible = item.visible === 1 ? 0 : 1; ElMessage.success('已更新') } catch { ElMessage.error('操作失败') }
}

async function handleUpload(e) {
  const file = e.target.files[0]; if (!file) return
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('文件不能超过 5MB'); return }
  uploading.value = true
  try {
    const res = await uploadFile(file)
    if (res.code === 200) { form.value.imageUrl = res.data.url; ElMessage.success('上传成功') }
    else { ElMessage.error(res.message || '上传失败') }
  } catch (err) {
    const msg = err?.response?.data?.message || err?.message || '上传失败'
    ElMessage.error(msg)
  } finally { uploading.value = false }
}
</script>

<template>
  <div class="admin-certs">
    <div class="ac-head"><h2>证书资质管理</h2><el-button type="success" @click="openDrawer()">＋ 新增证书</el-button></div>
    <div class="ac-stats">
      <span class="acs">全部 {{ list.length }}</span>
      <span class="acs">已公开 {{ list.filter(c=>c.visible===1).length }}</span>
      <span class="acs">已隐藏 {{ list.filter(c=>c.visible===0).length }}</span>
      <span class="acs">重点 {{ list.filter(c=>c.featured===1).length }}</span>
    </div>
    <div class="ac-bar">
      <el-input v-model="searchKeyword" placeholder="搜索名称或机构..." size="small" clearable style="width:220px" />
      <el-select v-model="filterCategory" placeholder="全部分类" size="small" clearable style="width:130px">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
    </div>
    <div class="ac-table">
      <el-table :data="filtered" stripe size="small" v-loading="loading">
        <el-table-column label="图片" width="80">
          <template #default="{ row }"><img v-if="row.imageUrl" :src="row.imageUrl" style="width:48px;height:36px;object-fit:cover;border-radius:4px" /><span v-else style="color:#ccc">—</span></template>
        </el-table-column>
        <el-table-column prop="name" label="证书名称" min-width="240" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="issuer" label="颁发机构" width="140" />
        <el-table-column prop="issueDate" label="获得日期" width="100" />
        <el-table-column label="公开" width="70">
          <template #default="{ row }"><el-switch :model-value="row.visible===1" size="small" @change="toggleVisible(row)" /></template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="60" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="openDrawer(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="drawerVisible" :title="editingId ? '编辑证书' : '新增证书'" size="680px">
      <el-form :model="form" label-position="top">
        <el-form-item label="证书名称" required><el-input v-model="form.name" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="分类"><el-select v-model="form.category" style="width:100%"><el-option v-for="c in categories" :key="c" :label="c" :value="c" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="获得日期"><el-input v-model="form.issueDate" placeholder="如 2025-09" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="颁发机构"><el-input v-model="form.issuer" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="证书编号"><el-input v-model="form.credentialNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="验证网址"><el-input v-model="form.verificationUrl" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="证书图片">
          <div style="display:flex;gap:12px;align-items:center">
            <label class="upload-btn"><input type="file" accept=".jpg,.jpeg,.png,.webp" @change="handleUpload" hidden :disabled="uploading" />{{ uploading ? '上传中...' : '选择图片' }}</label>
            <img v-if="form.imageUrl" :src="form.imageUrl" style="width:120px;height:80px;object-fit:cover;border-radius:6px;border:1px solid #ddd" />
            <el-button v-if="form.imageUrl" size="small" @click="form.imageUrl = ''">移除</el-button>
          </div>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="公开"><el-switch v-model="form.visible" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="重点"><el-switch v-model="form.featured" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="drawerVisible = false">取消</el-button><el-button type="success" @click="handleSave">保存</el-button></template>
    </el-drawer>
  </div>
</template>

<style scoped>
.admin-certs { padding: 20px 24px; }
.ac-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.ac-head h2 { font-size: 20px; font-weight: 700; }
.ac-stats { display: flex; gap: 8px; margin-bottom: 12px; }
.acs { padding: 4px 12px; background: #F4F4F5; border-radius: 12px; font-size: 12px; color: #53675a; }
.ac-bar { display: flex; gap: 8px; margin-bottom: 12px; }
.ac-table { background: #fff; border-radius: 8px; padding: 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.upload-btn { padding: 8px 16px; background: #eaf5ec; color: #2f9142; border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 500; display: inline-block; }
.upload-btn:hover { background: #d4edda; }
</style>
