package com.example.meetingroom.repository;

import com.example.meetingroom.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // 특정 날짜 범위의 예약 목록 조회 (타임라인용)
    List<Booking> findAllByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // 특정 시작 시간 범위의 예약 조회 (알림 스케줄러용)
    @Query("SELECT b FROM Booking b WHERE b.status != 'CANCELLED' " +
           "AND b.startTime >= :from AND b.startTime < :to")
    List<Booking> findConfirmedStartingBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // 중복 예약 확인 쿼리
    @Query("SELECT b FROM Booking b WHERE b.roomId = :roomId " +
            "AND b.status != 'CANCELLED' " +
            "AND b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 수정 시 자기 자신 제외한 중복 체크
    @Query("SELECT b FROM Booking b WHERE b.roomId = :roomId AND b.id != :excludeId " +
            "AND b.status != 'CANCELLED' " +
            "AND b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findOverlappingBookingsExcluding(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId
    );

    // 사용자별 예약 생성 건수 (통계용)
    @Query("SELECT b.userId, COUNT(b) FROM Booking b WHERE b.status != 'CANCELLED' GROUP BY b.userId ORDER BY COUNT(b) DESC")
    List<Object[]> countBookingsByUser();

    // 내가 예약자이거나 참석자인 예약 조회 (booking_attendees 테이블 JOIN)
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN BookingAttendee ba ON ba.bookingId = b.id " +
           "WHERE b.status != 'CANCELLED' " +
           "AND (b.userId = :userId OR ba.userId = :userId) " +
           "ORDER BY b.startTime ASC")
    List<Booking> findMyBookings(@Param("userId") Long userId);
}
