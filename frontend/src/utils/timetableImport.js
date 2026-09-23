/**
 * 教务系统课表 Excel 解析器
 *
 * 单元格文本格式（每行一条排课，多行=同格多课次）：
 *   课程名/(节次)周次/ 地点/教师/职称/总学时/学分
 *   例：数据库原理/(5-6节)1-12周/ 2号综合楼707/刘永立/讲师/48/4.0
 *
 * 返回 { courses, entries, warnings }
 *   courses: [{ name, teacher, title, credit, totalHours, color }]
 *   entries: [{ courseIndex, dayOfWeek, slot, startWeek, endWeek, room }]
 */

const DAY_NAMES = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日']
const SLOT_BY_TEXT = { '第1-2节': 0, '第3-4节': 1, '第5-6节': 2, '第7-8节': 3 }

const COURSE_LINE_RE = /^(.+?)\s*\/\s*[（(]\s*(\d{1,2})\s*[-－—~]\s*(\d{1,2})\s*节\s*[)）]\s*(\d{1,2})\s*[-－—~]\s*(\d{1,2})\s*周\s*\/\s*(.*?)\s*\/\s*(.*?)\s*\/\s*(.*?)\s*\/\s*(\d+(?:\.\d+)?)\s*\/\s*(\d+(?:\.\d+)?)\s*$/

/**
 * 解析课表 Excel 文件
 * @param {File|Blob} file .xlsx 文件
 * @returns {Promise<{courses:Array, entries:Array, warnings:string[], sheetName:string}>}
 */
export async function parseTimetableExcel(file) {
  const XLSX = await import('xlsx')
  const wb = XLSX.read(await file.arrayBuffer(), { type: 'array' })
  if (!wb.SheetNames.length) throw new Error('Excel 文件中没有工作表')
  const sheetName = wb.SheetNames[0]
  const ws = wb.Sheets[sheetName]
  const rows = XLSX.utils.sheet_to_json(ws, { header: 1, raw: true, defval: '' })

  // 1. 找表头行（含 星期一..星期日），记录天 → 列号
  const headerRow = rows.findIndex(r =>
    r.some(c => DAY_NAMES.includes(String(c).trim())))
  if (headerRow < 0) throw new Error('未找到表头（星期一~星期日），文件格式不符合教务课表')
  const dayCol = {}
  rows[headerRow].forEach((c, i) => {
    const idx = DAY_NAMES.indexOf(String(c).trim())
    if (idx >= 0) dayCol[i] = idx + 1
  })
  if (Object.keys(dayCol).length < 5) throw new Error('表头天数不足，文件格式不符合教务课表')

  // 2. 逐行解析
  const warnings = []
  const courseMap = new Map() // key: nameteacher
  const rawEntries = []
  let colorCounter = 0

  for (let r = headerRow + 1; r < rows.length; r++) {
    const a = String(rows[r][0] ?? '').trim()
    const b = String(rows[r][1] ?? '').trim()
    if (!['上午', '下午', '晚上'].includes(a) || !['一', '二', '三', '四', '五', '六'].includes(b)) continue

    for (const [col, day] of Object.entries(dayCol)) {
      const rawCell = String(rows[r][col] ?? '').trim()
      if (!rawCell) continue

      for (const rawLine of rawCell.split(/\r?\n/)) {
        const line = rawLine.trim()
        if (!line) continue
        const m = COURSE_LINE_RE.exec(line)
        if (!m) {
          warnings.push(`第${r + 1}行 ${DAY_NAMES[day - 1]} 无法解析，已跳过：${line.slice(0, 30)}…`)
          continue
        }
        const [, name, slotStart, slotEnd, weekStartStr, weekEndStr, room, teacher, title, hours, credit] = m
        const slotText = `第${slotStart}-${slotEnd}节`
        const slot = SLOT_BY_TEXT[slotText]
        if (slot === undefined) {
          warnings.push(`第${r + 1}行 ${DAY_NAMES[day - 1]} 节次 ${slotText} 超出第1-8节范围，已跳过`)
          continue
        }
        const s = Number(weekStartStr)
        const e = Number(weekEndStr)
        if (s > e) {
          warnings.push(`第${r + 1}行 ${DAY_NAMES[day - 1]} 起止周有误（${s}-${e}周），已跳过`)
          continue
        }
        const cleanTitle = title.trim() === '无' ? '' : title.trim()
        const key = name.trim() + '' + teacher.trim()
        if (!courseMap.has(key)) {
          colorCounter++
          courseMap.set(key, {
            name: name.trim(),
            teacher: teacher.trim(),
            title: cleanTitle,
            credit: Number(credit) || 0,
            totalHours: Number(hours) || 0,
            color: ((colorCounter - 1) % 8) + 1
          })
        } else {
          // 同一课程分段（换教室）学时不同：取最大值，不能求和
          const c = courseMap.get(key)
          c.totalHours = Math.max(c.totalHours, Number(hours) || 0)
        }
        rawEntries.push({
          courseKey: key,
          dayOfWeek: day,
          slot,
          startWeek: s,
          endWeek: e,
          room: room.replace(/\s+/g, ' ').trim()
        })
      }
    }
  }

  // 3. 转 courseIndex
  const courses = [...courseMap.values()]
  const indexByKey = new Map(courses.map((c, i) => [c.name + '' + c.teacher, i]))
  const entries = rawEntries.map(e => ({
    courseIndex: indexByKey.get(e.courseKey),
    dayOfWeek: e.dayOfWeek,
    slot: e.slot,
    startWeek: e.startWeek,
    endWeek: e.endWeek,
    room: e.room
  }))

  if (!courses.length) warnings.push('未解析出任何课程，请确认文件是教务系统导出的课表')

  return { courses, entries, warnings, sheetName }
}
