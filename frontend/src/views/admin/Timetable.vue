<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  getAdminTimetable, createTimetableCourse, updateTimetableCourse, deleteTimetableCourse,
  createTimetableEntry, updateTimetableEntry, deleteTimetableEntry,
  importTimetable, saveSiteSettings
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DAYS, SLOT_TEXT, PALETTE, paletteOf } from '@/utils/timetable'
import { parseTimetableExcel } from '@/utils/timetableImport'

const loading = ref(false)
const semester = ref({ startDate: '', totalWeeks: 0, examStartWeek: 0 })
const courses = ref([])
const entries = ref([])

// ==================== 数据 ====================
onMounted(fetchAll)
async function fetchAll() {
  loading.value = true
  try {
    const res = await getAdminTimetable()
    if (res.code === 200 && res.data) {
      semester.value = res.data.semester || semester.value
      courses.value = res.data.courses || []
      entries.value = res.data.entries || []
    }
  } catch { ElMessage.error('获取课表数据失败') } finally { loading.value = false }
}

const courseName = (id) => {
  const c = courses.value.find(x => x.id === id)
  return c ? c.name : `课程#${id}`
}
const dayName = (d) => DAYS[(d || 1) - 1] || ''
const slotName = (s) => SLOT_TEXT[s] ?? ''

// ==================== 学期设置 ====================
const savingSemester = ref(false)
async function saveSemester() {
  if (!semester.value.startDate) return ElMessage.warning('请填写开学日期')
  if (!semester.value.totalWeeks || semester.value.totalWeeks < 1) return ElMessage.warning('总周数至少为 1')
  savingSemester.value = true
  try {
    const res = await saveSiteSettings({
      semester_start_date: semester.value.startDate,
      semester_total_weeks: String(semester.value.totalWeeks),
      semester_exam_start_week: String(semester.value.examStartWeek || 0)
    })
    if (res.code === 200) ElMessage.success('学期设置已保存')
  } catch { ElMessage.error('保存失败') } finally { savingSemester.value = false }
}

// ==================== 导入 ====================
const importInput = ref(null)
const importing = ref(false)
function pickFile() { importInput.value?.click() }

async function onFileChange(e) {
  const file = e.target.files[0]
  e.target.value = '' // 允许重复选择同一文件
  if (!file) return
  importing.value = true
  try {
    const parsed = await parseTimetableExcel(file)
    const warnText = parsed.warnings.length
      ? `\n\n有 ${parsed.warnings.length} 条记录未能解析（将跳过）：\n· ${parsed.warnings.slice(0, 5).join('\n· ')}${parsed.warnings.length > 5 ? `\n· …等共 ${parsed.warnings.length} 条` : ''}`
      : ''
    await ElMessageBox.confirm(
      `解析到 ${parsed.courses.length} 门课程、${parsed.entries.length} 条排课。` +
      `导入将【全量替换】现有课表数据，确认继续？${warnText}`,
      '导入课表',
      { type: 'warning', confirmButtonText: '导入并替换', cancelButtonText: '取消', distinguishCancelAndClose: true }
    )
    const res = await importTimetable({ courses: parsed.courses, entries: parsed.entries })
    if (res.code === 200) {
      ElMessage.success(`导入成功：${res.data.courses} 门课程、${res.data.entries} 条排课`)
      await fetchAll()
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch (err) {
    if (err === 'cancel' || err === 'close') return
    ElMessage.error(err?.response?.data?.message || err?.message || '导入失败，请确认是教务系统导出的 xlsx 文件')
  } finally { importing.value = false }
}

// ==================== 课程 ====================
const courseDrawer = ref(false)
const editingCourseId = ref(null)
const courseForm = ref({ name: '', teacher: '', title: '', credit: 0, totalHours: 0, color: 1, sortOrder: 0 })

function openCourseDrawer(item) {
  editingCourseId.value = item?.id || null
  courseForm.value = item
    ? { name: item.name, teacher: item.teacher || '', title: item.title || '', credit: Number(item.credit) || 0, totalHours: item.totalHours || 0, color: item.color || 1, sortOrder: item.sortOrder || 0 }
    : { name: '', teacher: '', title: '', credit: 0, totalHours: 0, color: 1, sortOrder: courses.value.length }
  courseDrawer.value = true
}

async function saveCourse() {
  if (!courseForm.value.name) return ElMessage.warning('请输入课程名称')
  try {
    if (editingCourseId.value) { await updateTimetableCourse(editingCourseId.value, courseForm.value); ElMessage.success('更新成功') }
    else { await createTimetableCourse(courseForm.value); ElMessage.success('新增成功') }
    courseDrawer.value = false
    await fetchAll()
  } catch (err) { ElMessage.error(err?.response?.data?.message || '操作失败') }
}

async function removeCourse(item) {
  try {
    await ElMessageBox.confirm(`删除课程「${item.name}」？该课程下的所有排课也会一并删除。`, '确认', { type: 'warning' })
    await deleteTimetableCourse(item.id)
    ElMessage.success('已删除')
    await fetchAll()
  } catch {}
}

// ==================== 排课 ====================
const entryDrawer = ref(false)
const editingEntryId = ref(null)
const entryForm = ref({ courseId: null, dayOfWeek: 1, slot: 0, startWeek: 1, endWeek: 1, room: '', sortOrder: 0 })

function openEntryDrawer(item) {
  editingEntryId.value = item?.id || null
  entryForm.value = item
    ? { courseId: item.courseId, dayOfWeek: item.dayOfWeek, slot: item.slot, startWeek: item.startWeek, endWeek: item.endWeek, room: item.room || '', sortOrder: item.sortOrder || 0 }
    : { courseId: courses.value[0]?.id || null, dayOfWeek: 1, slot: 0, startWeek: 1, endWeek: semester.value.totalWeeks || 1, room: '', sortOrder: 0 }
  entryDrawer.value = true
}

async function saveEntry() {
  if (!entryForm.value.courseId) return ElMessage.warning('请选择课程')
  if (entryForm.value.endWeek < entryForm.value.startWeek) return ElMessage.warning('结束周不能早于起始周')
  try {
    if (editingEntryId.value) { await updateTimetableEntry(editingEntryId.value, entryForm.value); ElMessage.success('更新成功') }
    else { await createTimetableEntry(entryForm.value); ElMessage.success('新增成功') }
    entryDrawer.value = false
    await fetchAll()
  } catch (err) { ElMessage.error(err?.response?.data?.message || '操作失败') }
}

async function removeEntry(item) {
  try {
    await ElMessageBox.confirm(`删除「${courseName(item.courseId)} · ${dayName(item.dayOfWeek)} ${slotName(item.slot)}」这条排课？`, '确认', { type: 'warning' })
    await deleteTimetableEntry(item.id)
    ElMessage.success('已删除')
    await fetchAll()
  } catch {}
}

// ==================== 统计 ====================
const totalCredits = computed(() => courses.value.reduce((s, c) => s + (Number(c.credit) || 0), 0))
const colorOptions = PALETTE.map((p, i) => ({ value: i + 1, bg: p.bg }))
</script>

<template>
  <div class="admin-tt" v-loading="loading">
    <div class="att-head">
      <h2>课表管理</h2>
      <div class="att-head-btns">
        <el-button :loading="importing" @click="pickFile">📥 导入课表 Excel</el-button>
        <el-button type="success" @click="openCourseDrawer()">＋ 新增课程</el-button>
        <el-button type="success" plain @click="openEntryDrawer()">＋ 新增排课</el-button>
      </div>
      <input ref="importInput" type="file" accept=".xlsx,.xls" style="display:none" @change="onFileChange" />
    </div>

    <!-- 统计 -->
    <div class="att-stats">
      <span class="atts">课程 {{ courses.length }}</span>
      <span class="atts">排课 {{ entries.length }}</span>
      <span class="atts">学分合计 {{ totalCredits }}</span>
      <span class="atts" :class="{ warn: !semester.startDate }">{{ semester.startDate ? `开学 ${semester.startDate} · ${semester.totalWeeks} 周` : '学期未配置' }}</span>
    </div>

    <!-- 学期设置 -->
    <div class="att-card">
      <div class="att-card-title">学期设置 <span class="att-hint">设置后前台课表才会显示周次与日期</span></div>
      <div class="att-semester">
        <el-date-picker v-model="semester.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开学日期" size="small" style="width:150px" />
        <el-input-number v-model="semester.totalWeeks" :min="1" :max="30" size="small" style="width:130px" />
        <span class="att-label">总周数</span>
        <el-input-number v-model="semester.examStartWeek" :min="0" :max="30" size="small" style="width:130px" />
        <span class="att-label">考试起始周（0=无考试周）</span>
        <el-button type="primary" size="small" :loading="savingSemester" @click="saveSemester">保存学期设置</el-button>
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="att-card">
      <div class="att-card-title">课程</div>
      <el-table :data="courses" stripe size="small">
        <el-table-column label="颜色" width="60">
          <template #default="{ row }"><span class="att-sw" :style="{ background: paletteOf(row.color || 1).bg }"></span></template>
        </el-table-column>
        <el-table-column prop="name" label="课程名称" min-width="180" />
        <el-table-column prop="teacher" label="教师" width="110" />
        <el-table-column prop="title" label="职称" width="90" />
        <el-table-column prop="credit" label="学分" width="70" />
        <el-table-column prop="totalHours" label="总学时" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="60" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="openCourseDrawer(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeCourse(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>暂无课程，可点击右上角「导入课表 Excel」或手动新增</template>
      </el-table>
    </div>

    <!-- 排课列表 -->
    <div class="att-card">
      <div class="att-card-title">排课</div>
      <el-table :data="entries" stripe size="small">
        <el-table-column label="课程" min-width="180">
          <template #default="{ row }">
            <span class="att-sw" :style="{ background: paletteOf(courses.find(c => c.id === row.courseId)?.color || 1).bg }"></span>
            {{ courseName(row.courseId) }}
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ dayName(row.dayOfWeek) }} · {{ slotName(row.slot) }}</template>
        </el-table-column>
        <el-table-column label="周次" width="100">
          <template #default="{ row }">第{{ row.startWeek }}-{{ row.endWeek }}周</template>
        </el-table-column>
        <el-table-column prop="room" label="地点" min-width="150" />
        <el-table-column prop="sortOrder" label="排序" width="60" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="openEntryDrawer(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeEntry(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>暂无排课</template>
      </el-table>
    </div>

    <!-- 课程抽屉 -->
    <el-drawer v-model="courseDrawer" :title="editingCourseId ? '编辑课程' : '新增课程'" size="520px">
      <el-form :model="courseForm" label-position="top">
        <el-form-item label="课程名称" required><el-input v-model="courseForm.name" maxlength="100" /></el-form-item>
        <el-form-item label="教师"><el-input v-model="courseForm.teacher" maxlength="50" /></el-form-item>
        <el-form-item label="职称"><el-input v-model="courseForm.title" maxlength="30" placeholder="讲师 / 教授，无则留空" /></el-form-item>
        <el-form-item label="学分"><el-input-number v-model="courseForm.credit" :min="0" :max="99.99" :precision="1" :step="0.5" style="width:160px" /></el-form-item>
        <el-form-item label="总学时"><el-input-number v-model="courseForm.totalHours" :min="0" :max="1000" style="width:160px" /></el-form-item>
        <el-form-item label="颜色">
          <div class="att-colors">
            <span v-for="o in colorOptions" :key="o.value" class="att-color" :class="{ on: courseForm.color === o.value }" :style="{ background: o.bg }" @click="courseForm.color = o.value"></span>
          </div>
        </el-form-item>
        <el-form-item label="排序（数字小的在前）"><el-input-number v-model="courseForm.sortOrder" :min="0" style="width:160px" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDrawer = false">取消</el-button>
        <el-button type="primary" @click="saveCourse">保存</el-button>
      </template>
    </el-drawer>

    <!-- 排课抽屉 -->
    <el-drawer v-model="entryDrawer" :title="editingEntryId ? '编辑排课' : '新增排课'" size="520px">
      <el-form :model="entryForm" label-position="top">
        <el-form-item label="课程" required>
          <el-select v-model="entryForm.courseId" placeholder="选择课程" style="width:100%">
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="星期" required>
          <el-select v-model="entryForm.dayOfWeek" style="width:160px">
            <el-option v-for="(d, i) in DAYS" :key="i" :label="d" :value="i + 1" />
          </el-select>
        </el-form-item>
        <el-form-item label="节次" required>
          <el-select v-model="entryForm.slot" style="width:160px">
            <el-option v-for="(s, i) in SLOT_TEXT" :key="i" :label="s" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="起止周" required>
          <div class="att-weeks">
            <el-input-number v-model="entryForm.startWeek" :min="1" :max="99" style="width:120px" />
            <span class="att-label">至</span>
            <el-input-number v-model="entryForm.endWeek" :min="1" :max="99" style="width:120px" />
            <span class="att-label">周</span>
          </div>
        </el-form-item>
        <el-form-item label="地点"><el-input v-model="entryForm.room" maxlength="200" placeholder="如：2号综合楼707" /></el-form-item>
        <el-form-item label="排序（数字小的在前）"><el-input-number v-model="entryForm.sortOrder" :min="0" style="width:160px" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="entryDrawer = false">取消</el-button>
        <el-button type="primary" @click="saveEntry">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.admin-tt { padding: 4px 0 24px; }

.att-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; flex-wrap: wrap; gap: 8px; }
.att-head h2 { font-size: 20px; font-weight: 700; color: #17231b; margin: 0; }
.att-head-btns { display: flex; gap: 8px; flex-wrap: wrap; }

.att-stats { display: flex; gap: 6px; margin-bottom: 14px; flex-wrap: wrap; }
.atts { font-size: 12px; padding: 3px 12px; border-radius: 10px; background: #F4F4F5; color: #606266; }
.atts.warn { background: #FDF6EC; color: #E6A23C; }

.att-card { background: #fff; border: 1px solid #e8e9eb; border-radius: 10px; padding: 14px 16px; margin-bottom: 14px; }
.att-card-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 12px; }
.att-hint { font-size: 12px; font-weight: 400; color: #909399; margin-left: 8px; }

.att-semester { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.att-label { font-size: 13px; color: #606266; }
.att-weeks { display: flex; align-items: center; gap: 8px; }

.att-sw { display: inline-block; width: 12px; height: 12px; border-radius: 3px; margin-right: 6px; vertical-align: -1px; }

.att-colors { display: flex; gap: 8px; flex-wrap: wrap; }
.att-color { width: 26px; height: 26px; border-radius: 6px; cursor: pointer; border: 2px solid transparent; transition: all .12s; }
.att-color:hover { transform: scale(1.1); }
.att-color.on { border-color: #17231b; box-shadow: 0 0 0 2px #fff, 0 0 0 4px rgba(23,35,27,.35); }
</style>
