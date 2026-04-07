package com.example.meetingroom.controller;

import com.example.meetingroom.domain.NotificationSetting;
import com.example.meetingroom.domain.Room;
import com.example.meetingroom.dto.AdminCreateUserDto;
import com.example.meetingroom.repository.NotificationSettingRepository;
import com.example.meetingroom.service.AdminService;
import com.example.meetingroom.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final NotificationSettingRepository notificationSettingRepository;
    private final SseService sseService;

    // Online users
    @GetMapping("/online-users")
    public ResponseEntity<?> getOnlineUsers() {
        return ResponseEntity.ok(sseService.getOnlineUsers());
    }

    // Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    // 참여 통계 (예약자 TOP5 + 참석자 TOP5)
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(adminService.getParticipationStats());
    }

    // Rooms
    @GetMapping("/rooms")
    public ResponseEntity<?> getRooms() {
        return ResponseEntity.ok(adminService.getAllRooms());
    }

    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@RequestBody Room room) {
        return ResponseEntity.ok(adminService.createRoom(room));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room req) {
        return ResponseEntity.ok(adminService.updateRoom(id, req));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        adminService.deleteRoom(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/rooms/{id}/toggle")
    public ResponseEntity<?> toggleRoom(@PathVariable Long id) {
        adminService.toggleRoom(id);
        return ResponseEntity.ok().build();
    }

    // Bookings
    @GetMapping("/bookings")
    public ResponseEntity<?> getBookings() {
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    @DeleteMapping("/bookings/reset")
    public ResponseEntity<?> resetBookings() {
        adminService.resetBookings();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/logs/reset")
    public ResponseEntity<?> resetLogs() {
        adminService.resetLogs();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        adminService.cancelBooking(id);
        return ResponseEntity.ok().build();
    }

    // Logs
    @GetMapping("/logs")
    public ResponseEntity<?> getLogs() {
        return ResponseEntity.ok(adminService.getLogs());
    }


    // 유저 직접 생성
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AdminCreateUserDto dto) {
        return ResponseEntity.ok(adminService.createUser(dto));
    }

    // 비밀번호 직접 설정
    @PatchMapping("/users/{id}/password")
    public ResponseEntity<?> setUserPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.setUserPassword(id, body.get("password"));
        return ResponseEntity.ok().build();
    }

    // FCM 토큰 초기화
    @DeleteMapping("/users/{id}/fcm-token")
    public ResponseEntity<?> clearFcmToken(@PathVariable Long id) {
        adminService.clearFcmToken(id);
        return ResponseEntity.ok().build();
    }

    // admin 제외 유저 전체 초기화
    @DeleteMapping("/users/reset")
    public ResponseEntity<?> resetUsers() {
        adminService.resetUsers();
        return ResponseEntity.ok().build();
    }

    // 유저 목록 조회
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(adminService.getUsers());
    }

    // 비밀번호 초기화
    @PatchMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        String tempPw = adminService.resetPassword(id);
        return ResponseEntity.ok(Map.of("tempPassword", tempPw));
    }

    // 유저 승인
    @PatchMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        adminService.approveUser(id);
        return ResponseEntity.ok().build();
    }

    // 유저 거절 (사유 포함)
    @PatchMapping("/users/{id}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable Long id,
                                        @RequestParam(required = false, defaultValue = "") String reason) {
        adminService.rejectUser(id, reason);
        return ResponseEntity.ok().build();
    }

    // 유저 역할 변경
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @RequestParam String role) {
        adminService.changeRole(id, role);
        return ResponseEntity.ok().build();
    }

    // 개별 유저 삭제
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // 전체 유저 알림 전송
    @PostMapping("/notification/broadcast")
    public ResponseEntity<?> broadcast(@RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "공지");
        String msg   = body.getOrDefault("body", "");
        adminService.broadcastNotification(title, msg);
        return ResponseEntity.ok().build();
    }

    // 알림 설정 조회
    @GetMapping("/notification-setting")
    public ResponseEntity<?> getNotificationSetting() {
        NotificationSetting setting = notificationSettingRepository.findById(1L)
            .orElseGet(() -> NotificationSetting.builder()
                .id(1L)
                .minutesBefore("5,10")
                .titleTemplate("🔔 회의 {minutes}분 전")
                .bodyTemplate("{title} · {time} 시작")
                .build());
        return ResponseEntity.ok(setting);
    }

    // 알림 설정 저장
    @PutMapping("/notification-setting")
    public ResponseEntity<?> saveNotificationSetting(@RequestBody NotificationSetting req) {
        req.setId(1L);
        notificationSettingRepository.save(req);
        return ResponseEntity.ok().build();
    }

    // 알림/시스템 설정 초기화
    @DeleteMapping("/notification-setting/reset")
    public ResponseEntity<?> resetNotifSetting() {
        adminService.resetNotifSetting();
        return ResponseEntity.ok().build();
    }

    // 전체 초기화 (순서 보장 — 로그는 마지막 삭제)
    @DeleteMapping("/full-reset")
    public ResponseEntity<?> doFullReset() {
        adminService.doFullReset();
        return ResponseEntity.ok().build();
    }
}




