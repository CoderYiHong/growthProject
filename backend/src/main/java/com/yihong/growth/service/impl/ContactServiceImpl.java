package com.yihong.growth.service.impl;

import com.yihong.growth.dto.ContactFormDTO;
import com.yihong.growth.entity.Message;
import com.yihong.growth.mapper.MessageMapper;
import com.yihong.growth.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
    // 单次 insert + 可选邮件发送，insert 本身是原子操作

    private final MessageMapper messageMapper;

    /** mailSender 可能因未配置邮件而不可用，设为可选注入 */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${contact.receive-email:}")
    private String receiveEmail;

    @Override
    public boolean sendMessage(ContactFormDTO dto, String ip) {
        // 1. 保存到数据库
        Message msg = new Message();
        msg.setName(dto.getName());
        msg.setEmail(dto.getEmail());
        msg.setSubject(dto.getSubject());
        msg.setContent(dto.getContent());
        msg.setStatus("unread");
        msg.setIp(ip);
        messageMapper.insert(msg);

        // 2. 发送邮件通知（仅当 mailSender 可用时）
        if (mailSender == null || !StringUtils.hasText(mailUsername)
                || !StringUtils.hasText(mailPassword)) {
            log.info("Gmail SMTP 未配置，留言已保存，跳过邮件通知");
            return false;
        }

        String targetEmail = StringUtils.hasText(receiveEmail)
                ? receiveEmail.trim()
                : mailUsername.trim();
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(mailUsername.trim());
            mail.setTo(targetEmail);
            mail.setReplyTo(dto.getEmail().trim());
            mail.setSubject("[YiHong. 留言] " + sanitizeHeader(dto.getSubject()));
            mail.setText(String.format(
                "来自：%s (%s)\n主题：%s\n\n%s",
                dto.getName(), dto.getEmail(), dto.getSubject(), dto.getContent()
            ));
            mailSender.send(mail);
            return true;
        } catch (Exception e) {
            // 邮件发送失败不影响留言保存
            log.warn("邮件发送失败：{}", e.getMessage());
            return false;
        }
    }

    private String sanitizeHeader(String value) {
        return value.replace('\r', ' ').replace('\n', ' ').trim();
    }
}
