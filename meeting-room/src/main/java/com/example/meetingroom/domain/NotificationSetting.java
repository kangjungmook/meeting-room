package com.example.meetingroom.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_settings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSetting {

    @Id
    private Long id; // 항상 1 (싱글톤)

    @Column(name = "minutes_before", nullable = false)
    private String minutesBefore;

    @Column(name = "title_template", nullable = false)
    private String titleTemplate;

    @Column(name = "body_template", nullable = false)
    private String bodyTemplate;

    @Column(name = "toast_duration", nullable = false)
    @Builder.Default
    private int toastDuration = 5;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    // ── 세부 알림 타입별 ON/OFF ──

    @Column(name = "enable_booking_created", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean enableBookingCreated = true;

    @Column(name = "enable_booking_updated", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean enableBookingUpdated = true;

    @Column(name = "enable_invitation", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean enableInvitation = true;

    @Column(name = "enable_reminder", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean enableReminder = true;

    // ── 시스템 설정 ──

    @Column(name = "allow_self_registration", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean allowSelfRegistration = true;

    @Column(name = "maintenance_mode", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean maintenanceMode = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
