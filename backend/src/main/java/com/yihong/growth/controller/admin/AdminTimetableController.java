package com.yihong.growth.controller.admin;

import com.yihong.growth.annotation.OperationLog;
import com.yihong.growth.common.Result;
import com.yihong.growth.dto.CourseDTO;
import com.yihong.growth.dto.ImportTimetableRequest;
import com.yihong.growth.dto.ScheduleEntryDTO;
import com.yihong.growth.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/timetable")
@RequiredArgsConstructor
public class AdminTimetableController {

    private final TimetableService service;

    @GetMapping
    public Result<Map<String, Object>> aggregate() {
        return Result.ok(service.aggregate());
    }

    // ===== 课程 =====

    @OperationLog(value = "新增课程", module = "课表")
    @PostMapping("/courses")
    public Result<?> createCourse(@Valid @RequestBody CourseDTO dto) {
        service.createCourse(dto);
        return Result.ok("新增成功");
    }

    @OperationLog(value = "更新课程", module = "课表")
    @PutMapping("/courses/{id}")
    public Result<?> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDTO dto) {
        if (!service.updateCourse(id, dto)) return Result.error(404, "课程不存在");
        return Result.ok("更新成功");
    }

    @OperationLog(value = "删除课程", module = "课表")
    @DeleteMapping("/courses/{id}")
    public Result<?> deleteCourse(@PathVariable Long id) {
        if (!service.deleteCourse(id)) return Result.error(404, "课程不存在");
        return Result.ok("删除成功");
    }

    // ===== 排课 =====

    @OperationLog(value = "新增排课", module = "课表")
    @PostMapping("/entries")
    public Result<?> createEntry(@Valid @RequestBody ScheduleEntryDTO dto) {
        service.createEntry(dto);
        return Result.ok("新增成功");
    }

    @OperationLog(value = "更新排课", module = "课表")
    @PutMapping("/entries/{id}")
    public Result<?> updateEntry(@PathVariable Long id, @Valid @RequestBody ScheduleEntryDTO dto) {
        if (!service.updateEntry(id, dto)) return Result.error(404, "排课不存在");
        return Result.ok("更新成功");
    }

    @OperationLog(value = "删除排课", module = "课表")
    @DeleteMapping("/entries/{id}")
    public Result<?> deleteEntry(@PathVariable Long id) {
        if (!service.deleteEntry(id)) return Result.error(404, "排课不存在");
        return Result.ok("删除成功");
    }

    // ===== 导入（全量替换） =====

    @OperationLog(value = "导入课表", module = "课表")
    @PostMapping("/import")
    public Result<Map<String, Object>> importTimetable(@Valid @RequestBody ImportTimetableRequest req) {
        return Result.ok("导入成功", service.importReplace(req));
    }
}
