package com.example.meetingroom.controller;

import com.example.meetingroom.common.config.JwtUtil;
import com.example.meetingroom.domain.Booking;
import com.example.meetingroom.dto.BookingRequestDto;
import com.example.meetingroom.repository.UserRepository;
import com.example.meetingroom.service.AdminLogService;
import com.example.meetingroom.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final AdminLogService adminLogService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<Booking>> getBookings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = (endDate != null ? endDate : startDate).atTime(23, 59, 59);

        return ResponseEntity.ok(bookingService.getBookingsByDate(start, end));
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDto dto,
                                           HttpServletRequest request) {
        Long userId = resolveUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        try {
            Booking savedBooking = bookingService.createBooking(dto, userId);
            adminLogService.log("BOOKING_CREATE", "예약 등록: " + dto.getTitle(),
                    Map.of("title",     dto.getTitle(),
                           "organizer", dto.getOrganizer() != null ? dto.getOrganizer() : "",
                           "roomId",    dto.getRoomId(),
                           "startTime", dto.getStartTime().toString(),
                           "endTime",   dto.getEndTime().toString()),
                    savedBooking.getId(), "BOOKING");
            return ResponseEntity.ok(savedBooking);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id,
                                           HttpServletRequest request) {
        Long userId = resolveUserId(request);
        String role = resolveRole();
        try {
            bookingService.cancelBooking(id, userId, role);
            adminLogService.log("BOOKING_CANCEL", "예약 취소 ID: " + id,
                    Map.of("bookingId", id),
                    id, "BOOKING");
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id,
                                           @RequestBody BookingRequestDto dto,
                                           HttpServletRequest request) {
        Long userId = resolveUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        String role = resolveRole();
        try {
            Booking updated = bookingService.updateBooking(id, dto, userId, role);
            adminLogService.log("BOOKING_UPDATE", "예약 수정: " + dto.getTitle(),
                    Map.of("title",     dto.getTitle(),
                           "roomId",    dto.getRoomId(),
                           "startTime", dto.getStartTime().toString(),
                           "endTime",   dto.getEndTime().toString()),
                    id, "BOOKING");
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Booking>> getMyBookings(HttpServletRequest request) {
        Long userId = resolveUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(bookingService.getMyBookings(userId));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<?> endBooking(@PathVariable Long id) {
        bookingService.endBookingNow(id);
        adminLogService.log("BOOKING_END", "예약 조기 종료 ID: " + id,
                Map.of("bookingId", id));
        return ResponseEntity.ok().build();
    }

    // JWT에서 userId 추출 (Authorization 헤더)
    private Long resolveUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        try {
            return jwtUtil.getUserId(header.substring(7));
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "USER";
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "ADMIN" : "USER";
    }
}
