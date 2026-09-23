package com.yihong.growth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yihong.growth.dto.CourseDTO;
import com.yihong.growth.dto.ImportTimetableRequest;
import com.yihong.growth.dto.ScheduleEntryDTO;
import com.yihong.growth.entity.Course;
import com.yihong.growth.entity.ScheduleEntry;
import com.yihong.growth.mapper.CourseMapper;
import com.yihong.growth.mapper.ScheduleEntryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final CourseMapper courseMapper;
    private final ScheduleEntryMapper entryMapper;
    private final SettingService settingService;

    // ===== 聚合 =====

    public Map<String, Object> aggregate() {
        Map<String, String> settings = settingService.getAll();
        Map<String, Object> semester = new HashMap<>();
        semester.put("startDate", settings.getOrDefault("semester_start_date", ""));
        semester.put("totalWeeks", parseIntSafe(settings.get("semester_total_weeks"), 0));
        semester.put("examStartWeek", parseIntSafe(settings.get("semester_exam_start_week"), 0));

        Map<String, Object> data = new HashMap<>();
        data.put("semester", semester);
        data.put("courses", courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .orderByAsc(Course::getSortOrder).orderByAsc(Course::getId)));
        data.put("entries", entryMapper.selectList(new LambdaQueryWrapper<ScheduleEntry>()
                .orderByAsc(ScheduleEntry::getSortOrder).orderByAsc(ScheduleEntry::getId)));
        return data;
    }

    private int parseIntSafe(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        try { return Integer.parseInt(value.trim()); } catch (NumberFormatException e) { return fallback; }
    }

    // ===== 课程 =====

    public Course createCourse(CourseDTO dto) {
        Course c = new Course();
        c.setName(dto.getName());
        c.setTeacher(dto.getTeacher() == null ? "" : dto.getTeacher());
        c.setTitle(dto.getTitle() == null ? "" : dto.getTitle());
        c.setCredit(dto.getCredit() == null ? BigDecimal.ZERO : dto.getCredit());
        c.setTotalHours(dto.getTotalHours() == null ? 0 : dto.getTotalHours());
        c.setColor(dto.getColor() == null || dto.getColor() < 1 ? 1 : dto.getColor());
        c.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        courseMapper.insert(c);
        return c;
    }

    public boolean updateCourse(Long id, CourseDTO dto) {
        UpdateWrapper<Course> uw = new UpdateWrapper<>();
        uw.eq("id", id);
        if (dto.getName() != null) uw.set("name", dto.getName());
        if (dto.getTeacher() != null) uw.set("teacher", dto.getTeacher());
        if (dto.getTitle() != null) uw.set("title", dto.getTitle());
        if (dto.getCredit() != null) uw.set("credit", dto.getCredit());
        if (dto.getTotalHours() != null) uw.set("total_hours", dto.getTotalHours());
        if (dto.getColor() != null) uw.set("color", dto.getColor());
        if (dto.getSortOrder() != null) uw.set("sort_order", dto.getSortOrder());
        return courseMapper.update(null, uw) > 0;
    }

    /** 删除课程并级联删除其排课 */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourse(Long id) {
        if (courseMapper.selectById(id) == null) return false;
        entryMapper.delete(new LambdaQueryWrapper<ScheduleEntry>().eq(ScheduleEntry::getCourseId, id));
        return courseMapper.deleteById(id) > 0;
    }

    // ===== 排课 =====

    public ScheduleEntry createEntry(ScheduleEntryDTO dto) {
        verifyCourseExists(dto.getCourseId());
        if (dto.getEndWeek() < dto.getStartWeek())
            throw new IllegalArgumentException("结束周不能早于起始周");
        ScheduleEntry e = new ScheduleEntry();
        e.setCourseId(dto.getCourseId());
        e.setDayOfWeek(dto.getDayOfWeek());
        e.setSlot(dto.getSlot());
        e.setStartWeek(dto.getStartWeek());
        e.setEndWeek(dto.getEndWeek());
        e.setRoom(dto.getRoom() == null ? "" : dto.getRoom());
        e.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        entryMapper.insert(e);
        return e;
    }

    public boolean updateEntry(Long id, ScheduleEntryDTO dto) {
        if (dto.getCourseId() != null) verifyCourseExists(dto.getCourseId());
        UpdateWrapper<ScheduleEntry> uw = new UpdateWrapper<>();
        uw.eq("id", id);
        if (dto.getCourseId() != null) uw.set("course_id", dto.getCourseId());
        if (dto.getDayOfWeek() != null) uw.set("day_of_week", dto.getDayOfWeek());
        if (dto.getSlot() != null) uw.set("slot", dto.getSlot());
        if (dto.getStartWeek() != null) uw.set("start_week", dto.getStartWeek());
        if (dto.getEndWeek() != null) uw.set("end_week", dto.getEndWeek());
        if (dto.getRoom() != null) uw.set("room", dto.getRoom());
        if (dto.getSortOrder() != null) uw.set("sort_order", dto.getSortOrder());

        // 跨字段校验：合并后的起止周
        ScheduleEntry exist = entryMapper.selectById(id);
        if (exist == null) return false;
        Integer start = dto.getStartWeek() != null ? dto.getStartWeek() : exist.getStartWeek();
        Integer end = dto.getEndWeek() != null ? dto.getEndWeek() : exist.getEndWeek();
        if (end < start) throw new IllegalArgumentException("结束周不能早于起始周");
        return entryMapper.update(null, uw) > 0;
    }

    public boolean deleteEntry(Long id) {
        return entryMapper.deleteById(id) > 0;
    }

    private void verifyCourseExists(Long courseId) {
        if (courseMapper.selectById(courseId) == null)
            throw new IllegalArgumentException("所属课程不存在");
    }

    // ===== Excel 导入（全量替换） =====

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importReplace(ImportTimetableRequest req) {
        List<ImportTimetableRequest.CourseItem> courses = req.getCourses() == null
                ? Collections.emptyList() : req.getCourses();
        List<ImportTimetableRequest.EntryItem> entries = req.getEntries() == null
                ? Collections.emptyList() : req.getEntries();

        entryMapper.delete(null);
        courseMapper.delete(null);

        List<Long> courseIds = new ArrayList<>(courses.size());
        for (int i = 0; i < courses.size(); i++) {
            ImportTimetableRequest.CourseItem item = courses.get(i);
            if (item.getName() == null || item.getName().isBlank())
                throw new IllegalArgumentException("第" + (i + 1) + "门课程缺少名称，导入已回滚");
            Course c = new Course();
            c.setName(item.getName());
            c.setTeacher(item.getTeacher() == null ? "" : item.getTeacher());
            c.setTitle(item.getTitle() == null ? "" : item.getTitle());
            c.setCredit(item.getCredit() == null ? BigDecimal.ZERO : item.getCredit());
            c.setTotalHours(item.getTotalHours() == null ? 0 : item.getTotalHours());
            c.setColor(item.getColor() == null || item.getColor() < 1 ? 1 : item.getColor());
            c.setSortOrder(i);
            courseMapper.insert(c);
            courseIds.add(c.getId());
        }

        for (int i = 0; i < entries.size(); i++) {
            ImportTimetableRequest.EntryItem item = entries.get(i);
            if (item.getCourseIndex() == null || item.getCourseIndex() < 0 || item.getCourseIndex() >= courseIds.size())
                throw new IllegalArgumentException("第" + (i + 1) + "条排课引用了不存在的课程，导入已回滚");
            if (item.getEndWeek() < item.getStartWeek())
                throw new IllegalArgumentException("第" + (i + 1) + "条排课结束周早于起始周，导入已回滚");
            ScheduleEntry e = new ScheduleEntry();
            e.setCourseId(courseIds.get(item.getCourseIndex()));
            e.setDayOfWeek(item.getDayOfWeek());
            e.setSlot(item.getSlot());
            e.setStartWeek(item.getStartWeek());
            e.setEndWeek(item.getEndWeek());
            e.setRoom(item.getRoom() == null ? "" : item.getRoom());
            e.setSortOrder(i);
            entryMapper.insert(e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("courses", courses.size());
        result.put("entries", entries.size());
        return result;
    }
}
