package com.yihong.growth.service;

import com.yihong.growth.dto.LoginDTO;
import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginDTO loginDTO);
    void logout(String token);
    boolean validateToken(String token);
    boolean changePassword(String oldPassword, String newPassword);
}
