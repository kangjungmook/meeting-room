package com.example.meetingroom.scheduler;

import com.example.meetingroom.domain.Booking;
import com.example.meetingroom.domain.NotificationSetting;
import com.example.meetingroom.domain.UserNotificationPreference;
import com.example.meetingroom.repository.AdminLogRepository;
import com.example.meetingroom.repository.BookingAttendeeRepository;
import com.example.meetingroom.repository.BookingRepository;
import com.example.meetingroom.repository.NotificationSettingRepository;
import com.example.meetingroom.repository.UserNotificationPreferenceRepository;
import com.example.meetingroom.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final BookingRepository bookingRepository;
    private final BookingAttendeeRepository bookingAttendeeRepository;
    private final UserNotificationPreferenceRepository userNotifPrefRepository;
    private final FcmService fcmService;
    private final NotificationSettingRepository notificationSettingRepository;
    private final AdminLogRepository adminLogRepository;

    /** 매일 새벽 3시 — 1개월 지난 로그 자동 삭제 */
    @Scheduled(cron = "0 0 3 * * *")
    @org.springframework.transaction.annotation.Transactional
    public void purgeOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(1);
        int deleted = adminLogRepository.deleteOlderThan(cutoff);
        if (deleted > 0) {
            log.info("만료 로그 자동 삭제: {}건 (기준: {})", deleted, cutoff.toLocalDate());
        }
    }

    /** 매분 실행 — 사용자별 리마인더 시점 기반 알림 */
    @Scheduled(fixedDelay = 60_000)
    public void sendUpcomingReminders() {
        NotificationSetting setting = notificationSettingRepository.findById(1L)
            .orElseGet(() -> NotificationSetting.builder()
                .id(1L)
                .titleTemplate("🔔 회의 {minutes}분 전")
                .bodyTemplate("{title} · {time} 시작")
                .build());

        if (!Boolean.TRUE.equals(setting.getEnabled()) || !Boolean.TRUE.equals(setting.getEnableReminder())) return;

        // 리마인더 수신 ON인 사용자들의 설정 목록
        List<UserNotificationPreference> prefs = userNotifPrefRepository.findByReceiveReminderTrue();
        if (prefs.isEmpty()) return;

        // 분 값 → 해당 분을 원하는 userId Set
        Map<Integer, Set<Long>> minuteToUsers = new HashMap<>();
        for (UserNotificationPreference p : prefs) {
            String rm = (p.getReminderMinutes() != null && !p.getReminderMinutes().isBlank())
                    ? p.getReminderMinutes() : "10";
            for (String s : rm.split(",")) {
                try {
                    int m = Integer.parseInt(s.trim());
                    minuteToUsers.computeIfAbsent(m, k -> new HashSet<>()).add(p.getUserId());
                } catch (NumberFormatException ignored) {}
            }
        }

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        for (Map.Entry<Integer, Set<Long>> entry : minuteToUsers.entrySet()) {
            int minutes = entry.getKey();
            Set<Long> wantThisMinute = entry.getValue();

            LocalDateTime from = now.plusMinutes(minutes);
            LocalDateTime to   = from.plusMinutes(1);

            List<Booking> bookings = bookingRepository.findConfirmedStartingBetween(from, to);
            if (bookings.isEmpty()) continue;

            String timeStr = from.toLocalTime().toString().substring(0, 5);
            String titleTpl = setting.getTitleTemplate();
            String bodyTpl  = setting.getBodyTemplate();

            for (Booking b : bookings) {
                String title = titleTpl.replace("{minutes}", String.valueOf(minutes));
                String body  = bodyTpl
                        .replace("{title}", b.getTitle() != null ? b.getTitle() : "")
                        .replace("{time}",  timeStr);

                // 예약자
                if (b.getUserId() != null && wantThisMinute.contains(b.getUserId())) {
                    fcmService.sendToUserId(b.getUserId(), title, body, "REMINDER");
                }

                // 참석자 (예약자 제외)
                List<Long> attendeeIds = bookingAttendeeRepository.findUserIdsByBookingId(b.getId());
                for (Long uid : attendeeIds) {
                    if (!uid.equals(b.getUserId()) && wantThisMinute.contains(uid)) {
                        fcmService.sendToUserId(uid, title, body, "REMINDER");
                    }
                }

                log.info("리마인더 [{}분 전] bookingId={} title={}", minutes, b.getId(), b.getTitle());
            }
        }
    }
}
