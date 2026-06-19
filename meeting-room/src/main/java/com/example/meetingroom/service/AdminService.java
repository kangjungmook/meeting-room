package com.example.meetingroom.service;

import com.example.meetingroom.domain.AdminLog;
import com.example.meetingroom.domain.Booking;
import com.example.meetingroom.domain.NotificationSetting;
import com.example.meetingroom.domain.Room;
import com.example.meetingroom.domain.User;
import com.example.meetingroom.common.config.DataInitializer;
import com.example.meetingroom.common.defaults.NotificationSettingDefaults;
import com.example.meetingroom.dto.AdminCreateUserDto;
import com.example.meetingroom.repository.AdminLogRepository;
import com.example.meetingroom.repository.BookingAttendeeRepository;
import com.example.meetingroom.repository.BookingRepository;
import com.example.meetingroom.repository.NotificationSettingRepository;
import com.example.meetingroom.repository.RoomRepository;
import com.example.meetingroom.repository.UserFcmTokenRepository;
import com.example.meetingroom.repository.UserNotificationPreferenceRepository;
import com.example.meetingroom.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final UserNotificationPreferenceRepository userNotifPrefRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final FcmService fcmService;
    private final DataInitializer dataInitializer;

    @Transactional
    public Room createRoom(Room room) {
        Room saved = roomRepository.save(room);
        adminLogService.log("ROOM_CREATE", "회의실 생성: " + saved.getName(),
                Map.of("name", saved.getName(), "capacity", saved.getCapacity(),
                       "colorCode", saved.getColorCode(), "active", saved.getIsActive()),
                saved.getId(), "ROOM");
        return saved;
    }

    @Transactional
    public Room updateRoom(Long id, Room req) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회의실을 찾을 수 없습니다."));
        String changed = room.getName().equals(req.getName()) ? "" : room.getName() + " → " + req.getName();
        // 변경 전후 상태를 관리자 로그에 남기기 위해 스냅샷 기록
        Map<String, Object> before = new HashMap<>();
        before.put("name",     room.getName());
        before.put("capacity", room.getCapacity());
        before.put("colorCode",room.getColorCode());
        before.put("active",   room.getIsActive());
        room.setName(req.getName());
        room.setCapacity(req.getCapacity());
        room.setColorCode(req.getColorCode());
        room.setIsActive(req.getIsActive());
        Room saved = roomRepository.save(room);
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

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        bookingRepository.deleteById(id);
        if (booking != null) {
            // 이미 삭제된 뒤이므로 미리 읽어둔 값으로 before 스냅샷 구성
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

    public List<AdminLog> getLogs() {
        return adminLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void resetLogs() {
        adminLogRepository.deleteAll();
        entityManager.flush();
        entityManager.createNativeQuery("ALTER TABLE admin_logs AUTO_INCREMENT = 1").executeUpdate();
    }

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
        result.put("fcmUsers",      userFcmTokenRepository.countDistinctUserId());
        result.put("roomStats",     roomStats);
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
        // FK 제약 때문에 FCM 토큰·알림 설정을 먼저 삭제해야 유저 삭제가 가능
        userFcmTokenRepository.deleteByUserIdIn(nonAdminIds);
        userNotifPrefRepository.deleteByUserIdIn(nonAdminIds);
        userRepository.deleteAll(nonAdmins);
        entityManager.createNativeQuery("ALTER TABLE users AUTO_INCREMENT = 1").executeUpdate();
        adminLogService.log("USER_RESET", "admin 제외 유저 전체 초기화 (" + count + "명)",
                Map.of("deletedCount", count), null, "USER");
    }

    @Transactional
    public void resetNotifSetting() {
        notificationSettingRepository.save(NotificationSettingDefaults.create());
        adminLogService.log("SYSTEM_RESET", "알림/시스템 설정 초기화 (기본값 복원)",
                Map.of(), null, "SYSTEM");
    }
    @Transactional
    public void doFullReset() {
        // FK 참조 순서대로 삭제: 참석자 → 예약
        bookingAttendeeRepository.deleteAll();
        bookingRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE booking_attendees AUTO_INCREMENT = 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE bookings AUTO_INCREMENT = 1").executeUpdate();

        // FCM 토큰 → 알림 설정 → 회원 전체 (admin 포함)
        userFcmTokenRepository.deleteAll();
        userNotifPrefRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE user_fcm_tokens AUTO_INCREMENT = 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE user_notification_preferences AUTO_INCREMENT = 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE users AUTO_INCREMENT = 1").executeUpdate();

        // admin을 즉시 재생성해야 ID = 1이 보장됨
        dataInitializer.createAdmin();

        notificationSettingRepository.save(NotificationSettingDefaults.create());

        // 로그는 가장 마지막에 삭제 — 위 작업 기록을 남기기 위함
        adminLogRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE admin_logs AUTO_INCREMENT = 1").executeUpdate();
    }

    public List<User> getUsers() {
        List<User> users = userRepository.findAll();
        Set<Long> withTokens = userFcmTokenRepository.findAll().stream()
            .map(t -> t.getUserId())
            .collect(Collectors.toSet());
        users.forEach(u -> u.setHasFcmToken(withTokens.contains(u.getId())));
        return users;
    }

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

    private static final String PW_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(PW_CHARS.charAt(SECURE_RANDOM.nextInt(PW_CHARS.length())));
        return sb.toString();
    }

    @Transactional
    public String resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        String tempPw = generateTempPassword();
        user.setPassword(passwordEncoder.encode(tempPw));
        user.setPasswordResetRequired(true);
        adminLogService.log("PASSWORD_RESET",
                user.getName() + "(" + user.getEmployeeId() + ") 비밀번호 초기화",
                Map.of("name", user.getName(), "employeeId", user.getEmployeeId()),
                id, "USER",
                Map.of("passwordResetRequired", false),
                Map.of("passwordResetRequired", true));
        return tempPw;
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
        user.setPasswordResetRequired(true);
        adminLogService.log("PASSWORD_SET",
                user.getName() + "(" + user.getEmployeeId() + ") 비밀번호 직접 설정",
                Map.of("name", user.getName(), "employeeId", user.getEmployeeId()), id, "USER");
    }

    @Transactional
    public void clearFcmToken(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        userFcmTokenRepository.deleteByUserId(id);
        adminLogService.log("FCM_CLEAR",
                user.getName() + "(" + user.getEmployeeId() + ") FCM 토큰 초기화",
                Map.of("name", user.getName(), "employeeId", user.getEmployeeId()), id, "USER");
    }

    @Transactional
    public void broadcastNotification(String title, String body) {
        fcmService.broadcast(title, body);
        adminLogService.log("NOTIFICATION_BROADCAST", "전체 알림: " + title,
                Map.of("title", title, "body", body), null, "SYSTEM");
    }
}
