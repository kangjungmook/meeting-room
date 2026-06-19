package com.example.meetingroom.repository;

import com.example.meetingroom.domain.BookingAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingAttendeeRepository extends JpaRepository<BookingAttendee, Long> {

    void deleteByBookingId(Long bookingId);

    @Query("SELECT ba.userId FROM BookingAttendee ba WHERE ba.bookingId = :bookingId")
    List<Long> findUserIdsByBookingId(@Param("bookingId") Long bookingId);

}
