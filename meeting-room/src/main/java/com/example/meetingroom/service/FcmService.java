package com.example.meetingroom.service;

import com.example.meetingroom.domain.NotificationSetting;
import com.example.meetingroom.domain.UserNotificationPreference;
import com.example.meetingroom.repository.NotificationSettingRepository;
import com.example.meetingroom.repository.UserNotificationPreferenceRepository;
import com.example.meetingroom.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final UserRepository userRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserNotificationPreferenceRepository notifPrefRepository;

    /** 단일 토큰으로 알림 전송 */
    public void send(String token, String title, String body) {
        NotificationSetting setting = notificationSettingRepository.findById(1L).orElse(null);
        if (setting != null && Boolean.FALSE.equals(setting.getEnabled())) return;
        if (token == null || token.isBlank()) return;
        try {
            Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .build();
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 전송 실패 (token={}...): {}", token.substring(0, Math.min(10, token.length())), e.getMessage());
        }
    }

    /** userId로 해당 유저에게 알림 전송 */
    public void sendToUserId(Long userId, String title, String body) {
        userRepository.findById(userId).ifPresent(user -> {
            send(user.getFcmToken(), title, body);
        });
    }

    /** userId로 해당 유저에게 알림 전송 (per-user 설정 체크 포함) */
    public void sendToUserId(Long userId, String title, String body, String type) {
        if (!canReceive(userId, type)) return;
        userRepository.findById(userId).ifPresent(user -> {
            send(user.getFcmToken(), title, body);
        });
    }

    /** 참석자 userId 목록으로 알림 전송 */
    public void sendToAttendees(List<Long> userIds, String title, String body) {
        sendToAttendeesExcluding(userIds, null, title, body, null);
    }

    /** 특정 userId 제외하고 참석자들에게 알림 전송 (예약자 중복 방지) */
    public void sendToAttendeesExcluding(List<Long> userIds, Long excludeUserId,
                                         String title, String body) {
        sendToAttendeesExcluding(userIds, excludeUserId, title, body, null);
    }

    /**
     * 타입 지정 버전 — 세부 알림 설정에 따라 전송 여부 결정
     * type: "BOOKING_CREATED" | "BOOKING_UPDATED" | "INVITATION" | "REMINDER"
     */
    public void sendToAttendeesExcluding(List<Long> userIds, Long excludeUserId,
                                         String title, String body, String type) {
        if (userIds == null || userIds.isEmpty()) return;

        NotificationSetting s = notificationSettingRepository.findById(1L).orElse(null);
        if (s != null) {
            if ("BOOKING_CREATED".equals(type) && !Boolean.TRUE.equals(s.getEnableBookingCreated())) return;
            if ("BOOKING_UPDATED".equals(type) && !Boolean.TRUE.equals(s.getEnableBookingUpdated())) return;
            if ("INVITATION".equals(type)      && !Boolean.TRUE.equals(s.getEnableInvitation()))     return;
            if ("REMINDER".equals(type)        && !Boolean.TRUE.equals(s.getEnableReminder()))       return;
        }

        userIds.stream()
            .filter(uid -> excludeUserId == null || !excludeUserId.equals(uid))
            .filter(uid -> canReceive(uid, type))
            .forEach(uid -> userRepository.findById(uid).ifPresent(user -> {
                if (user.getFcmToken() != null) send(user.getFcmToken(), title, body);
            }));
    }

    /** per-user 알림 수신 여부 확인 */
    private boolean canReceive(Long userId, String type) {
        if (userId == null || type == null) return true;
        UserNotificationPreference p = notifPrefRepository.findByUserId(userId).orElse(null);
        if (p == null) return true;
        return switch (type) {
            case "INVITATION"      -> p.isReceiveInvitation();
            case "BOOKING_UPDATED" -> p.isReceiveUpdates();
            case "REMINDER"        -> p.isReceiveReminder();
            default                -> true;
        };
    }

    /** 리마인더 알림 전송 (enableReminder 체크 포함) */
    public void sendReminder(String token, String title, String body) {
        NotificationSetting s = notificationSettingRepository.findById(1L).orElse(null);
        if (s != null && !Boolean.TRUE.equals(s.getEnableReminder())) return;
        send(token, title, body);
    }
}
