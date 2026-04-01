package com.example.meetingroom.repository;

import com.example.meetingroom.domain.UserNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserNotificationPreferenceRepository
        extends JpaRepository<UserNotificationPreference, Long> {

    Optional<UserNotificationPreference> findByUserId(Long userId);

    void deleteByUserIdIn(List<Long> userIds);

    List<UserNotificationPreference> findByReceiveReminderTrue();
}
