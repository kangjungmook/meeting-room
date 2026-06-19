package com.example.meetingroom.repository;

import com.example.meetingroom.domain.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {
    List<UserFcmToken> findByUserId(Long userId);
    Optional<UserFcmToken> findByToken(String token);
    Optional<UserFcmToken> findByUserIdAndToken(Long userId, String token);
    boolean existsByUserId(Long userId);
    void deleteByUserId(Long userId);
    void deleteByUserIdIn(List<Long> userIds);

    @Query("SELECT COUNT(DISTINCT t.userId) FROM UserFcmToken t")
    long countDistinctUserId();
}
