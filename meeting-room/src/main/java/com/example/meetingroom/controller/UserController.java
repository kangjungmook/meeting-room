package com.example.meetingroom.controller;

import com.example.meetingroom.common.config.JwtUtil;
import com.example.meetingroom.domain.NotificationSetting;
import com.example.meetingroom.domain.UserNotificationPreference;
import com.example.meetingroom.repository.NotificationSettingRepository;
import com.example.meetingroom.repository.UserNotificationPreferenceRepository;
import com.example.meetingroom.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserNotificationPreferenceRepository notifPrefRepository;
    private final JwtUtil jwtUtil;

    /** 승인된 사용자 목록 (참석자 자동완성용) — id 포함 */
    @GetMapping
    public ResponseEntity<?> getApprovedUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
            .filter(u -> "APPROVED".equals(u.getStatus()) && !"ADMIN".equals(u.getRole()))
            .map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "name", u.getName(),
                "employeeId", u.getEmployeeId()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /** FCM 토큰 저장 */
    @PostMapping("/fcm-token")
    public ResponseEntity<?> saveFcmToken(
            @AuthenticationPrincipal String employeeId,
            @RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) return ResponseEntity.badRequest().build();
        if (employeeId == null) return ResponseEntity.status(401).build();
        userRepository.findAllByFcmToken(token).forEach(u -> {
            if (!u.getEmployeeId().equals(employeeId)) {
                u.setFcmToken(null);
                userRepository.save(u);
            }
        });
        userRepository.findByEmployeeId(employeeId).ifPresent(user -> {
            user.setFcmToken(token);
            userRepository.save(user);
        });
        return ResponseEntity.ok().build();
    }

    /** 알림 표시 설정 조회 (토스트 유지 시간) */
    @GetMapping("/notification-setting")
    public ResponseEntity<?> getNotificationSetting() {
        NotificationSetting setting = notificationSettingRepository.findById(1L)
            .orElseGet(() -> NotificationSetting.builder()
                .id(1L).minutesBefore("5,10")
                .titleTemplate("🔔 회의 {minutes}분 전")
                .bodyTemplate("{title} · {time} 시작")
                .toastDuration(5)
                .build());
        return ResponseEntity.ok(Map.of("toastDuration", setting.getToastDuration()));
    }

    /** 내 알림 설정 조회 — 없으면 기본값 반환 */
    @GetMapping("/notification-preference")
    public ResponseEntity<?> getNotificationPreference(HttpServletRequest request) {
        Long userId = resolveUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(
            notifPrefRepository.findByUserId(userId)
                .orElseGet(() -> UserNotificationPreference.builder()
                    .userId(userId).build())
        );
    }

    /** 내 알림 설정 저장 */
    @PutMapping("/notification-preference")
    public ResponseEntity<?> saveNotificationPreference(
            HttpServletRequest request,
            @RequestBody UserNotificationPreference pref) {
        Long userId = resolveUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();

        // 기존 레코드 조회 후 id·createdAt 유지 (upsert)
        notifPrefRepository.findByUserId(userId).ifPresent(existing -> {
            pref.setId(existing.getId());
            pref.setCreatedAt(existing.getCreatedAt());
        });
        pref.setUserId(userId);
        return ResponseEntity.ok(notifPrefRepository.save(pref));
    }

    private Long resolveUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        try {
            return jwtUtil.getUserId(header.substring(7));
        } catch (Exception e) {
            return null;
        }
    }
}
