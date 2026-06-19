package com.example.meetingroom.service;

import com.example.meetingroom.domain.NotificationSetting;
import com.example.meetingroom.domain.UserFcmToken;
import com.example.meetingroom.domain.UserNotificationPreference;
import com.example.meetingroom.repository.NotificationSettingRepository;
import com.example.meetingroom.repository.UserFcmTokenRepository;
import com.example.meetingroom.repository.UserNotificationPreferenceRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final UserFcmTokenRepository userFcmTokenRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserNotificationPreferenceRepository notifPrefRepository;

    // 전역 설정 확인 후 해당 유저의 모든 기기로 전송
    public void sendToUserId(Long userId, String title, String body) {
        if (com.google.firebase.FirebaseApp.getApps().isEmpty()) return;
        NotificationSetting setting = notificationSettingRepository.findById(1L).orElse(null);
        if (setting != null && Boolean.FALSE.equals(setting.getEnabled())) return;
        doSendToUser(userId, title, body);
    }

    // 사용자별 수신 설정(type)도 함께 확인
    public void sendToUserId(Long userId, String title, String body, String type) {
        if (!canReceive(userId, type)) return;
        sendToUserId(userId, title, body);
    }

    // 예약자 본인(excludeUserId)은 제외하고 나머지 참석자에게만 전송
    public void sendToAttendeesExcluding(List<Long> userIds, Long excludeUserId,
                                         String title, String body, String type) {
        if (userIds == null || userIds.isEmpty()) return;

        NotificationSetting s = notificationSettingRepository.findById(1L).orElse(null);
        if (s != null) {
            if (Boolean.FALSE.equals(s.getEnabled())) return;
            if ("BOOKING_CREATED".equals(type) && !Boolean.TRUE.equals(s.getEnableBookingCreated())) return;
            if ("BOOKING_UPDATED".equals(type) && !Boolean.TRUE.equals(s.getEnableBookingUpdated())) return;
            if ("INVITATION".equals(type)      && !Boolean.TRUE.equals(s.getEnableInvitation()))     return;
            if ("REMINDER".equals(type)        && !Boolean.TRUE.equals(s.getEnableReminder()))       return;
        }

        userIds.stream()
            .filter(uid -> excludeUserId == null || !excludeUserId.equals(uid))
            .filter(uid -> canReceive(uid, type))
            .forEach(uid -> doSendToUser(uid, title, body));
    }

    // 모든 등록된 기기로 전체 브로드캐스트
    public void broadcast(String title, String body) {
        if (com.google.firebase.FirebaseApp.getApps().isEmpty()) return;
        NotificationSetting setting = notificationSettingRepository.findById(1L).orElse(null);
        if (setting != null && Boolean.FALSE.equals(setting.getEnabled())) return;
        List<String> tokens = userFcmTokenRepository.findAll().stream()
            .map(UserFcmToken::getToken)
            .filter(t -> t != null && !t.isBlank())
            .toList();
        doSendMulticast(tokens, title, body);
    }

    private void doSendToUser(Long userId, String title, String body) {
        List<String> tokens = userFcmTokenRepository.findByUserId(userId).stream()
            .map(UserFcmToken::getToken)
            .filter(t -> t != null && !t.isBlank())
            .toList();
        doSendMulticast(tokens, title, body);
    }

    private void doSendMulticast(List<String> tokens, String title, String body) {
        if (tokens.isEmpty() || com.google.firebase.FirebaseApp.getApps().isEmpty()) return;
        try {
            MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .build();
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    String failedToken = tokens.get(i);
                    log.warn("FCM 전송 실패 (token={}...): {}",
                        failedToken.substring(0, Math.min(10, failedToken.length())),
                        responses.get(i).getException().getMessage());
                    // 유효하지 않은 토큰은 자동 삭제
                    userFcmTokenRepository.findByToken(failedToken)
                        .ifPresent(userFcmTokenRepository::delete);
                }
            }
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 멀티캐스트 전송 실패: {}", e.getMessage());
        }
    }

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
}
