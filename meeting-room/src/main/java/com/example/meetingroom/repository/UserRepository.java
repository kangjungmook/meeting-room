package com.example.meetingroom.repository;

import com.example.meetingroom.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
    Optional<User> findByRefreshToken(String refreshToken);
    List<User> findAllByFcmToken(String fcmToken);
    long countByStatus(String status);
    long countByFcmTokenIsNotNull();
}