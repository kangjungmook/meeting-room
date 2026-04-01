package com.example.meetingroom.repository;

import com.example.meetingroom.domain.BookingAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingAttendeeRepository extends JpaRepository<BookingAttendee, Long> {

    List<BookingAttendee> findByBookingId(Long bookingId);

    void deleteByBookingId(Long bookingId);

    @Query("SELECT ba.userId FROM BookingAttendee ba WHERE ba.bookingId = :bookingId")
    List<Long> findUserIdsByBookingId(@Param("bookingId") Long bookingId);

    // 사용자별 참석 건수 (통계용)
    @Query("SELECT ba.userId, COUNT(ba) FROM BookingAttendee ba GROUP BY ba.userId ORDER BY COUNT(ba) DESC")
    List<Object[]> countAttendancesByUser();
}
