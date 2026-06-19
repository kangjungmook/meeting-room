package com.example.meetingroom.controller;

import com.example.meetingroom.common.config.JwtUtil;
import com.example.meetingroom.common.defaults.NotificationSettingDefaults;
import com.example.meetingroom.domain.NotificationSetting;
import com.example.meetingroom.domain.UserFcmToken;
import com.example.meetingroom.domain.UserNotificationPreference;
import com.example.meetingroom.repository.NotificationSettingRepository;
import com.example.meetingroom.repository.UserFcmTokenRepository;
import com.example.meetingroom.repository.UserNotificationPreferenceRepository;
import com.example.meetingroom.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserNotificationPreferenceRepository notifPrefRepository;
    private final JwtUtil jwtUtil;

    // 승인된 사용자 목록 (참석자 자동완성용) — id 포함
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

    // FCM 토큰 저장 — 기기별 다중 토큰 지원
    @Transactional
    @PostMapping("/fcm-token")
    public ResponseEntity<?> saveFcmToken(
            @AuthenticationPrincipal String employeeId,
            @RequestBody Map<String, String> body) {
        String token = body.get("token");
        String deviceType = body.getOrDefault("deviceType", "WEB");
        if (token == null || token.isBlank()) return ResponseEntity.badRequest().build();
        if (employeeId == null) return ResponseEntity.status(401).build();

        com.example.meetingroom.domain.User user =
            userRepository.findByEmployeeId(employeeId).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        // 다른 유저에게 이미 등록된 동일 토큰은 제거
        userFcmTokenRepository.findByToken(token).ifPresent(existing -> {
            if (!existing.getUserId().equals(user.getId())) {
                userFcmTokenRepository.delete(existing);
            }
        });

        // 같은 유저에게 이미 있으면 추가하지 않음
        if (userFcmTokenRepository.findByUserIdAndToken(user.getId(), token).isEmpty()) {
            userFcmTokenRepository.save(UserFcmToken.builder()
                .userId(user.getId())
                .token(token)
                .deviceType(deviceType)
                .build());
        }

        return ResponseEntity.ok().build();
    }

    // 내 FCM 토큰 전체 삭제 (푸시 알림 끄기)
    @Transactional
    @DeleteMapping("/fcm-token")
    public ResponseEntity<?> removeFcmToken(@AuthenticationPrincipal String employeeId) {
        if (employeeId == null) return ResponseEntity.status(401).build();
        com.example.meetingroom.domain.User user =
            userRepository.findByEmployeeId(employeeId).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        userFcmTokenRepository.deleteByUserId(user.getId());
        return ResponseEntity.ok().build();
    }

    // 알림 표시 설정 조회 (토스트 유지 시간)
    @GetMapping("/notification-setting")
    public ResponseEntity<?> getNotificationSetting() {
        NotificationSetting setting = notificationSettingRepository.findById(1L)
            .orElseGet(NotificationSettingDefaults::create);
        return ResponseEntity.ok(Map.of("toastDuration", setting.getToastDuration()));
    }

    // 내 알림 설정 조회 — 없으면 기본값 반환
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

    // 내 알림 설정 저장
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

    // 튜토리얼 완료 표시
    @PatchMapping("/tutorial-done")
    public ResponseEntity<?> markTutorialDone(HttpServletRequest request) {
        Long userId = resolveUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        userRepository.findById(userId).ifPresent(user -> {
            user.setTutorialDone(true);
            userRepository.save(user);
        });
        return ResponseEntity.ok().build();
    }

    // 튜토리얼 완료 여부 조회
    @GetMapping("/tutorial-done")
    public ResponseEntity<?> getTutorialDone(HttpServletRequest request) {
        Long userId = resolveUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        boolean done = userRepository.findById(userId)
                .map(u -> Boolean.TRUE.equals(u.getTutorialDone()))
                .orElse(false);
        return ResponseEntity.ok(Map.of("done", done));
    }

    private Long resolveUserId(HttpServletRequest request) {
        return jwtUtil.resolveUserId(request);
    }
}
