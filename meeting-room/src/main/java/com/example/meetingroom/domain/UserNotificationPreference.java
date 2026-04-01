package com.example.meetingroom.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification_preferences")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    // ── FCM 푸시 수신 여부 (백엔드에서 전송 여부 결정) ──

    @Builder.Default
    @Column(name = "receive_invitation")
    private boolean receiveInvitation = true;

    @Builder.Default
    @Column(name = "receive_updates")
    private boolean receiveUpdates = true;

    @Builder.Default
    @Column(name = "receive_reminder")
    private boolean receiveReminder = true;

    // ── SSE 토스트 표시 여부 (프론트엔드에서 결정) ──

    @Builder.Default
    @Column(name = "toast_created")
    private boolean toastCreated = true;

    @Builder.Default
    @Column(name = "toast_updated")
    private boolean toastUpdated = true;

    @Builder.Default
    @Column(name = "toast_cancelled")
    private boolean toastCancelled = true;

    @Builder.Default
    @Column(name = "toast_duration")
    private int toastDuration = 5;

    @Builder.Default
    @Column(name = "dark_mode")
    private boolean darkMode = false;

    /** 리마인더 알림 시점 — 분 단위 CSV (예: "5,10,15"), 기본 10분 전 */
    @Builder.Default
    @Column(name = "reminder_minutes", length = 100)
    private String reminderMinutes = "10";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
