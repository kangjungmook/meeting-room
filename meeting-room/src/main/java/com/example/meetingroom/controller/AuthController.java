package com.example.meetingroom.controller;

import com.example.meetingroom.domain.NotificationSetting;
import com.example.meetingroom.dto.UserRequestDto;
import com.example.meetingroom.repository.NotificationSettingRepository;
import com.example.meetingroom.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final NotificationSettingRepository notificationSettingRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequestDto dto) {
        NotificationSetting setting = notificationSettingRepository.findById(1L).orElse(null);
        if (setting != null && Boolean.FALSE.equals(setting.getAllowSelfRegistration())) {
            return ResponseEntity.status(403).body(Map.of("message", "현재 가입이 제한되어 있습니다. 관리자에게 문의하세요."));
        }
        try {
            authService.register(dto);
            return ResponseEntity.ok("회원가입 완료. 관리자 승인을 기다려주세요.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequestDto dto) {
        try {
            return ResponseEntity.ok(authService.login(dto));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.startsWith("REJECTED:")) {
                String reason = msg.substring("REJECTED:".length());
                return ResponseEntity.status(403).body(Map.of("rejected", true, "reason", reason));
            }
            return ResponseEntity.badRequest().body(msg);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody java.util.Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.refresh(body.get("refreshToken")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(org.springframework.security.core.Authentication auth) {
        if (auth != null) authService.logout(auth.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody java.util.Map<String, String> body,
                                            org.springframework.security.core.Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        try {
            authService.changePassword(auth.getName(), body.get("newPassword"));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}