package com.yihong.growth.controller;

import com.yihong.growth.common.Result;
import com.yihong.growth.entity.Certificate;
import com.yihong.growth.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService service;

    @GetMapping
    public Result<List<Certificate>> list() {
        // 公共接口不返回 credentialNo
        List<Certificate> list = service.listPublic();
        list.forEach(c -> c.setCredentialNo(null));
        return Result.ok(list);
    }
}
