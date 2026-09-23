package com.yihong.growth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yihong.growth.dto.CertificateDTO;
import com.yihong.growth.entity.Certificate;
import com.yihong.growth.mapper.CertificateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateMapper mapper;

    public List<Certificate> listPublic() {
        return mapper.selectList(new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getVisible, 1)
                .orderByDesc(Certificate::getFeatured)
                .orderByAsc(Certificate::getSortOrder)
                .orderByDesc(Certificate::getIssueDate));
    }

    public List<Certificate> listAll() {
        return mapper.selectList(new LambdaQueryWrapper<Certificate>()
                .orderByAsc(Certificate::getSortOrder));
    }

    public Certificate create(CertificateDTO dto) {
        Certificate c = toEntity(dto);
        c.setId(null);
        mapper.insert(c);
        return c;
    }

    public void update(Long id, CertificateDTO dto) {
        UpdateWrapper<Certificate> uw = new UpdateWrapper<>();
        uw.eq("id", id);
        if (dto.getName() != null) uw.set("name", dto.getName());
        if (dto.getCategory() != null) uw.set("category", dto.getCategory());
        if (dto.getIssuer() != null) uw.set("issuer", dto.getIssuer());
        if (dto.getIssueDate() != null) uw.set("issue_date", dto.getIssueDate());
        if (dto.getDescription() != null) uw.set("description", dto.getDescription());
        if (dto.getCredentialNo() != null) uw.set("credential_no", dto.getCredentialNo());
        if (dto.getVerificationUrl() != null) uw.set("verification_url", dto.getVerificationUrl());
        if (dto.getImageUrl() != null) uw.set("image_url", dto.getImageUrl());
        if (dto.getVisible() != null) uw.set("visible", dto.getVisible() ? 1 : 0);
        if (dto.getFeatured() != null) uw.set("featured", dto.getFeatured() ? 1 : 0);
        if (dto.getSortOrder() != null) uw.set("sort_order", dto.getSortOrder());
        mapper.update(null, uw);
    }

    public void delete(Long id) { mapper.deleteById(id); }

    private Certificate toEntity(CertificateDTO dto) {
        Certificate c = new Certificate();
        c.setName(dto.getName()); c.setCategory(dto.getCategory());
        c.setIssuer(dto.getIssuer()); c.setIssueDate(dto.getIssueDate());
        c.setDescription(dto.getDescription()); c.setCredentialNo(dto.getCredentialNo());
        c.setVerificationUrl(dto.getVerificationUrl()); c.setImageUrl(dto.getImageUrl());
        c.setVisible(dto.getVisible() != null && dto.getVisible() ? 1 : 0);
        c.setFeatured(dto.getFeatured() != null && dto.getFeatured() ? 1 : 0);
        c.setSortOrder(dto.getSortOrder());
        return c;
    }
}
