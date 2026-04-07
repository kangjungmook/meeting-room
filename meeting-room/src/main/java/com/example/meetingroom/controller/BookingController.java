package com.example.meetingroom.controller;

import com.example.meetingroom.common.config.JwtUtil;
import com.example.meetingroom.domain.Booking;
import com.example.meetingroom.dto.BookingRequestDto;
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

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
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
            return ResponseEntity.ok(savedBooking);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id,
                                           HttpServletRequest request) {
        Long userId = resolveUserId(request);
        String role = resolveRole();
        try {
            bookingService.cancelBooking(id, userId, role);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
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
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Booking>> getMyBookings(HttpServletRequest request) {
        Long userId = resolveUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(bookingService.getMyBookings(userId));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<?> endBooking(@PathVariable Long id, HttpServletRequest request) {
        Long userId = resolveUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        String role = resolveRole();
        try {
            bookingService.endBookingNow(id, userId, role);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    // 키오스크 전용 — 인증 없이 roomId로 소속 검증 후 종료
    @PatchMapping("/{id}/end-kiosk")
    public ResponseEntity<?> endBookingKiosk(@PathVariable Long id,
                                             @RequestParam Long roomId) {
        try {
            bookingService.endBookingNowKiosk(id, roomId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
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
