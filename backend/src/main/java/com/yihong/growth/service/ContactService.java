package com.yihong.growth.service;

import com.yihong.growth.dto.ContactFormDTO;

public interface ContactService {
    /**
     * 保存留言并尝试发送邮件通知。
     *
     * @return 邮件通知是否发送成功；留言无论如何都会先保存
     */
    boolean sendMessage(ContactFormDTO dto, String ip);
}
