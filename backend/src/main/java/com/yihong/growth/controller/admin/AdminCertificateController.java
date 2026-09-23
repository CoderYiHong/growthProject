package com.yihong.growth.controller.admin;

import com.yihong.growth.annotation.OperationLog;
import com.yihong.growth.common.Result;
import com.yihong.growth.dto.CertificateDTO;
import com.yihong.growth.entity.Certificate;
import com.yihong.growth.service.CertificateService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/certificates")
@RequiredArgsConstructor
public class AdminCertificateController {

    private final CertificateService service;

    @GetMapping
    public Result<List<Certificate>> list() { return Result.ok(service.listAll()); }

    @OperationLog(value = "新增证书", module = "证书管理")
    @PostMapping
    public Result<?> create(
            @Validated({Default.class, CertificateDTO.Create.class}) @RequestBody CertificateDTO dto) {
        service.create(dto);
        return Result.ok("新增成功");
    }

    @OperationLog(value = "更新证书", module = "证书管理")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody CertificateDTO dto) { service.update(id, dto); return Result.ok("更新成功"); }

    @OperationLog(value = "删除证书", module = "证书管理")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) { service.delete(id); return Result.ok("删除成功"); }
}
