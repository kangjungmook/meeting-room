package com.example.meetingroom.common.defaults;

import com.example.meetingroom.domain.NotificationSetting;

public final class NotificationSettingDefaults {
    private NotificationSettingDefaults() {}

    public static NotificationSetting create() {
        return NotificationSetting.builder()
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
                .build();
    }
}

