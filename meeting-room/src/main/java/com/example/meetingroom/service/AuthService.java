package com.example.meetingroom.service;

import com.example.meetingroom.common.config.JwtUtil;
import com.example.meetingroom.domain.User;
import com.example.meetingroom.dto.UserRequestDto;
import com.example.meetingroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SseService sseService;
    private final AdminLogService adminLogService;

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("비밀번호는 6자 이상이어야 합니다.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("비밀번호에 대문자를 1개 이상 포함해야 합니다.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("비밀번호에 소문자를 1개 이상 포함해야 합니다.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("비밀번호에 숫자를 1개 이상 포함해야 합니다.");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?`~].*")) {
            throw new IllegalArgumentException("비밀번호에 특수문자를 1개 이상 포함해야 합니다.");
        }
    }

    public void register(UserRequestDto dto) {
        if (userRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new IllegalArgumentException("이미 존재하는 사번입니다.");
        }
        validatePassword(dto.getPassword());
        User user = User.builder()
                .employeeId(dto.getEmployeeId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .build();
        userRepository.save(user);
        sseService.broadcast("ADMIN", Map.of("action", "USER_REGISTER"));
    }

    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> login(UserRequestDto dto) {
        User user = userRepository.findByEmployeeId(dto.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사번입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        if ("REJECTED".equals(user.getStatus())) {
            String reason = user.getRejectReason() != null ? user.getRejectReason() : "";
            throw new IllegalArgumentException("REJECTED:" + reason);
        }
        if (!"APPROVED".equals(user.getStatus())) {
            throw new IllegalArgumentException("관리자 승인 대기 중입니다.");
        }

        user.setLastLoginAt(LocalDateTime.now());

        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.generateToken(user.getEmployeeId(), user.getRole(), user.getName(), user.getId()));
        result.put("passwordResetRequired", Boolean.TRUE.equals(user.getPasswordResetRequired()));

        adminLogService.log("USER_LOGIN", user.getName() + " (" + user.getEmployeeId() + ") 로그인",
                Map.of("employeeId", user.getEmployeeId(), "name", user.getName()));

        if (dto.isRememberMe()) {
            String refreshToken = UUID.randomUUID().toString();
            user.setRefreshToken(refreshToken);
            user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(30));
            result.put("refreshToken", refreshToken);
        } else {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
        }

        return result;
    }

    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> refresh(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (user.getRefreshTokenExpiry() == null || user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            throw new IllegalArgumentException("만료된 토큰입니다.");
        }

        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(30));

        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.generateToken(user.getEmployeeId(), user.getRole(), user.getName(), user.getId()));
        result.put("passwordResetRequired", Boolean.TRUE.equals(user.getPasswordResetRequired()));
        result.put("refreshToken", refreshToken);
        return result;
    }

    @org.springframework.transaction.annotation.Transactional
    public void logout(String employeeId) {
        userRepository.findByEmployeeId(employeeId).ifPresent(user -> {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            adminLogService.log("USER_LOGOUT",
                    user.getName() + " (" + user.getEmployeeId() + ") 로그아웃",
                    Map.of("employeeId", user.getEmployeeId(), "name", user.getName()));
        });
    }

    @org.springframework.transaction.annotation.Transactional
    public void changePassword(String employeeId, String newPassword) {
        validatePassword(newPassword);
        User user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetRequired(false);
    }
}
