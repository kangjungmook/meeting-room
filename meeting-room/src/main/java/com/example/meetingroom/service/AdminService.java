package com.example.meetingroom.service;

import com.example.meetingroom.domain.AdminLog;
import com.example.meetingroom.domain.Booking;
import com.example.meetingroom.domain.NotificationSetting;
import com.example.meetingroom.domain.Room;
import com.example.meetingroom.domain.User;
import com.example.meetingroom.dto.AdminCreateUserDto;
import com.example.meetingroom.repository.AdminLogRepository;
import com.example.meetingroom.repository.BookingAttendeeRepository;
import com.example.meetingroom.repository.BookingRepository;
import com.example.meetingroom.repository.NotificationSettingRepository;
import com.example.meetingroom.repository.RoomRepository;
import com.example.meetingroom.repository.UserNotificationPreferenceRepository;
import com.example.meetingroom.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final BookingAttendeeRepository bookingAttendeeRepository;
    private final AdminLogRepository adminLogRepository;
    private final AdminLogService adminLogService;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final UserNotificationPreferenceRepository userNotifPrefRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final FcmService fcmService;

    // ── Rooms ──
    @Transactional
    public Room createRoom(Room room) {
        Room saved = roomRepository.save(room);
        Long id = saved.getId() != null ? saved.getId().longValue() : null;
        adminLogService.log("ROOM_CREATE", "회의실 생성: " + saved.getName(),
                Map.of("name", saved.getName(), "capacity", saved.getCapacity(),
                       "colorCode", saved.getColorCode(), "active", saved.getIsActive()),
                id, "ROOM");
        return saved;
    }

    @Transactional
    public Room updateRoom(Long id, Room req) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회의실을 찾을 수 없습니다."));
        String changed = room.getName().equals(req.getName()) ? "" : room.getName() + " → " + req.getName();
        // Before 스냅샷
        Map<String, Object> before = new HashMap<>();
        before.put("name",     room.getName());
        before.put("capacity", room.getCapacity());
        before.put("colorCode",room.getColorCode());
        before.put("active",   room.getIsActive());
        // 변경 적용
        room.setName(req.getName());
        room.setCapacity(req.getCapacity());
        room.setColorCode(req.getColorCode());
        room.setIsActive(req.getIsActive());
        Room saved = roomRepository.save(room);
        // After 스냅샷
        Map<String, Object> after = new HashMap<>();
        after.put("name",     saved.getName());
        after.put("capacity", saved.getCapacity());
        after.put("colorCode",saved.getColorCode());
        after.put("active",   saved.getIsActive());
        adminLogService.log("ROOM_UPDATE",
                "회의실 수정: " + saved.getName() + (changed.isBlank() ? "" : " (" + changed + ")"),
                null, id, "ROOM", before, after);
        return saved;
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회의실을 찾을 수 없습니다."));
        adminLogService.log("ROOM_DELETE", "회의실 삭제: " + room.getName(),
                Map.of("name", room.getName()), id, "ROOM");
        roomRepository.deleteById(id);
    }

    @Transactional
    public void toggleRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회의실을 찾을 수 없습니다."));
        boolean wasActive = room.getIsActive();
        room.setIsActive(!wasActive);
        roomRepository.save(room);
        adminLogService.log("ROOM_TOGGLE",
                "회의실 " + (room.getIsActive() ? "활성화" : "비활성화") + ": " + room.getName(),
                null, id, "ROOM",
                Map.of("active", wasActive),
                Map.of("active", room.getIsActive()));
    }

    // ── Bookings ──
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        bookingRepository.deleteById(id);
        if (booking != null) {
            // before = 삭제 직전 예약 상태 스냅샷
            Map<String, Object> before = new HashMap<>();
            before.put("id",        id);
            before.put("title",     booking.getTitle()     != null ? booking.getTitle()     : "");
            before.put("organizer", booking.getOrganizer() != null ? booking.getOrganizer() : "");
            before.put("startTime", booking.getStartTime() != null ? booking.getStartTime().toString() : "");
            before.put("endTime",   booking.getEndTime()   != null ? booking.getEndTime().toString()   : "");
            before.put("status",    booking.getStatus()    != null ? booking.getStatus().name()        : "");
            adminLogService.log("BOOKING_CANCEL",
                    "예약 강제 취소: " + (booking.getTitle() != null ? booking.getTitle() : "ID " + id),
                    null, id, "BOOKING", before, Map.of("status", "DELETED"));
        } else {
            adminLogService.log("BOOKING_CANCEL", "예약 강제 취소 ID: " + id,
                    null, id, "BOOKING");
        }
    }

    // ── Logs ──
    public List<AdminLog> getLogs() {
        return adminLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void resetLogs() {
        adminLogRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE admin_logs AUTO_INCREMENT = 1").executeUpdate();
    }

    // ── Dashboard ──
    public Map<String, Object> getDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        List<Booking> todayBookings = bookingRepository.findAllByStartTimeBetween(todayStart, todayEnd);
        List<Map<String, Object>> roomStats = roomRepository.findAll().stream().map(room -> {
            long count = todayBookings.stream()
                    .filter(b -> b.getRoomId() != null && b.getRoomId().equals(room.getId().longValue()))
                    .count();
            return Map.<String, Object>of("roomId", room.getId(), "roomName", room.getName(), "todayCount", count);
        }).toList();
        Map<String, Object> result = new HashMap<>();
        result.put("totalRooms",    roomRepository.count());
        result.put("activeRooms",   roomRepository.countByIsActiveTrue());
        result.put("todayBookings", todayBookings.size());
        result.put("totalBookings", bookingRepository.count());
        result.put("totalUsers",    userRepository.count());
        result.put("pendingUsers",  userRepository.countByStatus("PENDING"));
        result.put("fcmUsers",      userRepository.countByFcmTokenIsNotNull());
        result.put("roomStats",     roomStats);
        return result;
    }

    // ── 참여 통계 (TOP 5) ──
    public Map<String, Object> getParticipationStats() {
        // 예약자별 TOP 5 (취소 제외)
        List<Object[]> bookingRows = bookingRepository.countBookingsByUser();
        List<Map<String, Object>> topBookers = bookingRows.stream()
                .limit(5)
                .map(row -> {
                    Long uid   = ((Number) row[0]).longValue();
                    long count = ((Number) row[1]).longValue();
                    String name = userRepository.findById(uid).map(User::getName).orElse("(탈퇴)");
                    Map<String, Object> m = new HashMap<>();
                    m.put("userId", uid); m.put("name", name); m.put("count", count);
                    return m;
                })
                .toList();

        // 참석자별 TOP 5 (booking_attendees 기준)
        List<Object[]> attendeeRows = bookingAttendeeRepository.countAttendancesByUser();
        List<Map<String, Object>> topAttendees = attendeeRows.stream()
                .limit(5)
                .map(row -> {
                    Long uid   = ((Number) row[0]).longValue();
                    long count = ((Number) row[1]).longValue();
                    String name = userRepository.findById(uid).map(User::getName).orElse("(탈퇴)");
                    Map<String, Object> m = new HashMap<>();
                    m.put("userId", uid); m.put("name", name); m.put("count", count);
                    return m;
                })
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("topBookers",   topBookers);
        result.put("topAttendees", topAttendees);
        return result;
    }

    public List<Room> getAllRooms() { return roomRepository.findAll(); }

    @Transactional
    public void resetBookings() {
        long count = bookingRepository.count();
        bookingRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE bookings AUTO_INCREMENT = 1").executeUpdate();
        adminLogService.log("BOOKING_RESET", "예약 데이터 전체 초기화 (" + count + "건)",
                Map.of("deletedCount", count), null, "BOOKING");
    }

    @Transactional
    public void resetUsers() {
        List<User> nonAdmins = userRepository.findAll().stream()
                .filter(u -> !"ADMIN".equals(u.getRole())).toList();
        int count = nonAdmins.size();
        List<Long> nonAdminIds = nonAdmins.stream().map(User::getId).toList();
        userNotifPrefRepository.deleteByUserIdIn(nonAdminIds); // 연관 알림 설정 먼저 삭제
        userRepository.deleteAll(nonAdmins);
        entityManager.createNativeQuery("ALTER TABLE users AUTO_INCREMENT = 1").executeUpdate();
        adminLogService.log("USER_RESET", "admin 제외 유저 전체 초기화 (" + count + "명)",
                Map.of("deletedCount", count), null, "USER");
    }

    @Transactional
    public void resetNotifSetting() {
        notificationSettingRepository.save(NotificationSetting.builder()
                .id(1L)
                .minutesBefore("5,10")
                .titleTemplate("🔔 회의 {minutes}분 전")
                .bodyTemplate("{title} · {time} 시작")
                .toastDuration(5)
                .enabled(true)
                .enableBookingCreated(true)
                .enableBookingUpdated(true)
                .enableInvitation(true)
                .enableReminder(true)
                .allowSelfRegistration(true)
                .maintenanceMode(false)
                .build());
        adminLogService.log("SYSTEM_RESET", "알림/시스템 설정 초기화 (기본값 복원)",
                Map.of(), null, "SYSTEM");
    }

    /**
     * 전체 초기화 — admin 포함 모든 데이터 삭제 후 PK 1부터 재시작
     * 순서: 예약(FK 먼저) → 회원 전체 → admin 재생성(ID=1) → 알림설정 → 로그
     */
    @Transactional
    public void doFullReset() {
        // 1. 예약 참석자 → 예약 (FK 순서)
        bookingAttendeeRepository.deleteAll();
        bookingRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE booking_attendees AUTO_INCREMENT = 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE bookings AUTO_INCREMENT = 1").executeUpdate();

        // 2. 알림 설정 → 회원 전체 (admin 포함)
        userNotifPrefRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE user_notification_preferences AUTO_INCREMENT = 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE users AUTO_INCREMENT = 1").executeUpdate();

        // 3. admin 즉시 재생성 → ID = 1
        userRepository.save(User.builder()
                .employeeId("admin")
                .password(passwordEncoder.encode("Admin1234!"))
                .name("관리자")
                .role("ADMIN")
                .status("APPROVED")
                .passwordResetRequired(false)
                .build());

        // 4. 알림/시스템 설정 기본값 복원
        notificationSettingRepository.save(NotificationSetting.builder()
                .id(1L).minutesBefore("5,10")
                .titleTemplate("🔔 회의 {minutes}분 전").bodyTemplate("{title} · {time} 시작")
                .toastDuration(5).enabled(true)
                .enableBookingCreated(true).enableBookingUpdated(true)
                .enableInvitation(true).enableReminder(true)
                .allowSelfRegistration(true).maintenanceMode(false).build());

        // 5. 로그 마지막 삭제
        adminLogRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE admin_logs AUTO_INCREMENT = 1").executeUpdate();
    }

    public List<User> getUsers() { return userRepository.findAll(); }

    @Transactional
    public void approveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        String oldStatus = user.getStatus();
        user.setStatus("APPROVED");
        adminLogService.log("USER_APPROVE", user.getName() + "(" + user.getEmployeeId() + ") 승인",
                Map.of("name", user.getName(), "employeeId", user.getEmployeeId()),
                id, "USER",
                Map.of("status", oldStatus != null ? oldStatus : ""),
                Map.of("status", "APPROVED"));
    }

    @Transactional
    public void resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        user.setPassword(passwordEncoder.encode("1111"));
        user.setPasswordResetRequired(true);
        adminLogService.log("PASSWORD_RESET",
                user.getName() + "(" + user.getEmployeeId() + ") 비밀번호 초기화",
                Map.of("name", user.getName(), "employeeId", user.getEmployeeId()),
                id, "USER",
                Map.of("passwordResetRequired", false),
                Map.of("passwordResetRequired", true, "resetTo", "1111"));
    }

    @Transactional
    public void changeRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        String oldRole = user.getRole();
        user.setRole(role);
        adminLogService.log("USER_ROLE_CHANGE",
                user.getName() + "(" + user.getEmployeeId() + ") " + oldRole + " → " + role,
                Map.of("name", user.getName(), "employeeId", user.getEmployeeId()),
                id, "USER",
                Map.of("role", oldRole != null ? oldRole : ""),
                Map.of("role", role));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        adminLogService.log("USER_DELETE",
                user.getName() + "(" + user.getEmployeeId() + ") 삭제",
                Map.of("role", user.getRole(), "status", user.getStatus()),
                id, "USER");
        userRepository.deleteById(id);
    }

    @Transactional
    public void rejectUser(Long id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        String oldStatus = user.getStatus();
        user.setStatus("REJECTED");
        user.setRejectReason(reason != null ? reason.trim() : "");
        adminLogService.log("USER_REJECT", user.getName() + "(" + user.getEmployeeId() + ") 거절",
                Map.of("name", user.getName(), "employeeId", user.getEmployeeId()),
                id, "USER",
                Map.of("status", oldStatus != null ? oldStatus : ""),
                Map.of("status", "REJECTED", "reason", user.getRejectReason()));
    }

    @Transactional
    public User createUser(AdminCreateUserDto dto) {
        if (userRepository.existsByEmployeeId(dto.getEmployeeId()))
            throw new IllegalArgumentException("이미 존재하는 사번입니다.");
        User user = User.builder()
                .employeeId(dto.getEmployeeId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .role(dto.getRole() != null ? dto.getRole() : "USER")
                .status(dto.getStatus() != null ? dto.getStatus() : "APPROVED")
                .build();
        User saved = userRepository.save(user);
        adminLogService.log("USER_CREATE",
                saved.getName() + "(" + saved.getEmployeeId() + ") 직접 생성",
                Map.of("role", saved.getRole(), "status", saved.getStatus()),
                saved.getId(), "USER");
        return saved;
    }

    @Transactional
    public void setUserPassword(Long id, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        user.setPassword(passwordEncoder.encode(password));
        user.setPasswordResetRequired(false);
        adminLogService.log("PASSWORD_SET",
                user.getName() + "(" + user.getEmployeeId() + ") 비밀번호 직접 설정",
                Map.of("name", user.getName(), "employeeId", user.getEmployeeId()), id, "USER");
    }

    @Transactional
    public void clearFcmToken(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        user.setFcmToken(null);
        adminLogService.log("FCM_CLEAR",
                user.getName() + "(" + user.getEmployeeId() + ") FCM 토큰 초기화",
                Map.of("name", user.getName(), "employeeId", user.getEmployeeId()), id, "USER");
    }

    @Transactional(readOnly = true)
    public void broadcastNotification(String title, String body) {
        userRepository.findAll().stream()
            .filter(u -> u.getFcmToken() != null && !u.getFcmToken().isBlank())
            .forEach(u -> fcmService.send(u.getFcmToken(), title, body));
        adminLogService.log("NOTIFICATION_BROADCAST", "전체 알림: " + title,
                Map.of("title", title, "body", body), null, "SYSTEM");
    }
}
