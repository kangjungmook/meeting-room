package com.example.meetingroom.service;

import com.example.meetingroom.domain.Booking;
import com.example.meetingroom.domain.BookingAttendee;
import com.example.meetingroom.dto.BookingRequestDto;
import com.example.meetingroom.repository.BookingAttendeeRepository;
import com.example.meetingroom.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingAttendeeRepository bookingAttendeeRepository;
    private final SseService sseService;
    private final FcmService fcmService;
    private final AdminLogService adminLogService;

    public BookingService(BookingRepository bookingRepository,
                          BookingAttendeeRepository bookingAttendeeRepository,
                          @Lazy SseService sseService,
                          FcmService fcmService,
                          @Lazy AdminLogService adminLogService) {
        this.bookingRepository = bookingRepository;
        this.bookingAttendeeRepository = bookingAttendeeRepository;
        this.sseService = sseService;
        this.fcmService = fcmService;
        this.adminLogService = adminLogService;
    }

    // ── 조회 ──────────────────────────────────────────────────
    public List<Booking> getBookingsByDate(LocalDateTime start, LocalDateTime end) {
        List<Booking> bookings = bookingRepository.findAllByStartTimeBetween(start, end);
        bookings.forEach(this::populateAttendeeIds);
        return bookings;
    }

    public List<Booking> getMyBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findMyBookings(userId);
        bookings.forEach(this::populateAttendeeIds);
        return bookings;
    }

    // ── 예약 생성 ──────────────────────────────────────────────
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking createBooking(BookingRequestDto dto, Long userId) {
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().isEqual(dto.getEndTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이후여야 합니다.");
        }
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                dto.getRoomId(), dto.getStartTime(), dto.getEndTime());
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("해당 시간에 이미 예약이 존재합니다.");
        }

        Booking booking = Booking.builder()
                .roomId(dto.getRoomId())
                .userId(userId)
                .title(dto.getTitle())
                .organizer(dto.getOrganizer())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .description(dto.getDescription())
                .externalAttendees(toExternalStr(dto.getExternalAttendees()))
                .build();

        Booking saved = bookingRepository.save(booking);
        saveAttendees(saved.getId(), dto.getAttendeeIds());
        populateAttendeeIds(saved);

        // ── 관리자 로그 (after 전용 — 신규 생성이므로 before 없음) ──
        adminLogService.log("BOOKING_CREATE",
                "예약 등록: " + saved.getTitle() + " (" + saved.getOrganizer() + ")",
                null,
                saved.getId(), "BOOKING",
                null,
                bookingSnapshot(saved));

        Map<String, Object> createdData = Map.of(
                "id", saved.getId(), "title", saved.getTitle(),
                "organizer", saved.getOrganizer() != null ? saved.getOrganizer() : "",
                "roomId", saved.getRoomId());

        // FCM 전송에 필요한 값을 트랜잭션 커밋 전에 캡처
        final List<Long> fcmAttendees = saved.getAttendeeIds() != null
                ? List.copyOf(saved.getAttendeeIds()) : List.of();
        final Long creatorId = userId;
        final String fcmTitle = "📅 회의 초대: " + saved.getTitle();
        final String fcmBody  = (saved.getOrganizer() != null ? saved.getOrganizer() : "")
                + "님이 초대했습니다 · "
                + saved.getStartTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));

        // SSE + FCM 모두 커밋 이후에 실행 — DB 잠금이 먼저 해제됨
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                sseService.broadcast("CREATED", createdData);
                fcmService.sendToAttendeesExcluding(fcmAttendees, creatorId, fcmTitle, fcmBody, "INVITATION");
            }
        });
        return saved;
    }

    // ── 예약 수정 ──────────────────────────────────────────────
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking updateBooking(Long id, BookingRequestDto dto, Long userId, String role) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        boolean isAdmin = "ADMIN".equals(role);
        boolean isOwner = booking.getUserId() != null && booking.getUserId().equals(userId);
        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("본인의 예약만 수정할 수 있습니다.");
        }
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().isEqual(dto.getEndTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이후여야 합니다.");
        }
        List<Booking> overlaps = bookingRepository.findOverlappingBookingsExcluding(
                dto.getRoomId(), dto.getStartTime(), dto.getEndTime(), id);
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("해당 시간에 이미 예약이 존재합니다.");
        }

        // ── Before 스냅샷 (수정 전) ──
        populateAttendeeIds(booking);
        Map<String, Object> before = bookingSnapshot(booking);

        booking.setRoomId(dto.getRoomId());
        booking.setTitle(dto.getTitle());
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(dto.getEndTime());
        booking.setDescription(dto.getDescription());
        booking.setExternalAttendees(toExternalStr(dto.getExternalAttendees()));

        Booking updated = bookingRepository.save(booking);
        bookingAttendeeRepository.deleteByBookingId(id);
        bookingAttendeeRepository.flush();
        saveAttendees(id, dto.getAttendeeIds());
        populateAttendeeIds(updated);

        // ── After 스냅샷 (수정 후) + 관리자 로그 ──
        adminLogService.log("BOOKING_UPDATE",
                "예약 수정: " + updated.getTitle() + " (" + updated.getOrganizer() + ")",
                null,
                id, "BOOKING",
                before,
                bookingSnapshot(updated));

        Map<String, Object> updatedData = Map.of(
                "id", updated.getId(), "title", updated.getTitle(),
                "organizer", updated.getOrganizer() != null ? updated.getOrganizer() : "",
                "roomId", updated.getRoomId());

        final List<Long> fcmAttendees = updated.getAttendeeIds() != null
                ? List.copyOf(updated.getAttendeeIds()) : List.of();
        final Long editorId = userId;
        final String fcmTitle = "✏️ 회의 수정: " + updated.getTitle();
        final String fcmBody  = (updated.getOrganizer() != null ? updated.getOrganizer() : "")
                + "님이 일정을 변경했습니다 · "
                + updated.getStartTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                sseService.broadcast("UPDATED", updatedData);
                fcmService.sendToAttendeesExcluding(fcmAttendees, editorId, fcmTitle, fcmBody, "BOOKING_UPDATED");
            }
        });
        return updated;
    }

    // ── 예약 취소 ──────────────────────────────────────────────
    @Transactional
    public void cancelBooking(Long id, Long requestUserId, String role) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        boolean isAdmin = "ADMIN".equals(role);
        boolean isOwner = booking.getUserId() != null && booking.getUserId().equals(requestUserId);
        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
        }

        // ── Before 스냅샷 ──
        populateAttendeeIds(booking);
        Map<String, Object> before = bookingSnapshot(booking);

        bookingAttendeeRepository.deleteByBookingId(id);
        bookingRepository.deleteById(id);

        adminLogService.log("BOOKING_CANCEL",
                "예약 취소: " + booking.getTitle() + " (" + booking.getOrganizer() + ")",
                null,
                id, "BOOKING",
                before,
                Map.of("status", "DELETED"));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { sseService.broadcast("CANCELLED", Map.of("id", id)); }
        });
    }

    // ── 키오스크 예약 즉시 종료 (인증 없음, roomId로 소속 검증) ──
    @Transactional
    public void endBookingNowKiosk(Long id, Long roomId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        if (!roomId.equals(booking.getRoomId())) {
            throw new IllegalArgumentException("해당 회의실의 예약이 아닙니다.");
        }
        Map<String, Object> before = Map.of("endTime", booking.getEndTime().toString());
        booking.setEndTime(LocalDateTime.now());
        bookingRepository.save(booking);
        adminLogService.log("BOOKING_END",
                "키오스크 조기 종료: " + booking.getTitle(),
                null, id, "BOOKING",
                before, Map.of("endTime", booking.getEndTime().toString()));
    }

    // ── 예약 즉시 종료 ─────────────────────────────────────────
    @Transactional
    public void endBookingNow(Long id, Long requestUserId, String role) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        boolean isAdmin = "ADMIN".equals(role);
        boolean isOwner = booking.getUserId() != null && booking.getUserId().equals(requestUserId);
        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("본인의 예약만 종료할 수 있습니다.");
        }

        Map<String, Object> before = Map.of("endTime", booking.getEndTime().toString());
        booking.setEndTime(LocalDateTime.now());
        bookingRepository.save(booking);

        adminLogService.log("BOOKING_END",
                "예약 즉시 종료: " + booking.getTitle(),
                null,
                id, "BOOKING",
                before,
                Map.of("endTime", booking.getEndTime().toString()));
    }

    // ── 내부 유틸 ──────────────────────────────────────────────

    /** 예약 상태를 로그용 Map으로 직렬화 */
    private Map<String, Object> bookingSnapshot(Booking b) {
        Map<String, Object> m = new HashMap<>();
        m.put("title",       b.getTitle()     != null ? b.getTitle()                   : "");
        m.put("organizer",   b.getOrganizer() != null ? b.getOrganizer()               : "");
        m.put("roomId",      b.getRoomId()    != null ? b.getRoomId()                  : "");
        m.put("startTime",   b.getStartTime() != null ? b.getStartTime().toString()    : "");
        m.put("endTime",     b.getEndTime()   != null ? b.getEndTime().toString()      : "");
        m.put("description", b.getDescription() != null ? b.getDescription()           : "");
        if (b.getAttendeeIds() != null && !b.getAttendeeIds().isEmpty()) {
            m.put("attendeeIds", b.getAttendeeIds());
        }
        return m;
    }

    /** 참석자 ID 목록을 booking_attendees 테이블에 저장 */
    private void saveAttendees(Long bookingId, List<Long> attendeeIds) {
        if (attendeeIds == null || attendeeIds.isEmpty()) return;
        attendeeIds.forEach(uid ->
            bookingAttendeeRepository.save(BookingAttendee.builder()
                .bookingId(bookingId)
                .userId(uid)
                .build())
        );
    }

    /** Booking 엔티티에 attendeeIds / externalAttendeeNames 주입 (응답 직렬화용) */
    public void populateAttendeeIds(Booking booking) {
        booking.setAttendeeIds(bookingAttendeeRepository.findUserIdsByBookingId(booking.getId()));
        if (booking.getExternalAttendees() != null && !booking.getExternalAttendees().isBlank()) {
            booking.setExternalAttendeeNames(
                Arrays.asList(booking.getExternalAttendees().split(","))
            );
        }
    }

    /** 외부 참석자 이름 목록을 콤마 구분 문자열로 변환 */
    private String toExternalStr(List<String> names) {
        if (names == null || names.isEmpty()) return null;
        String result = names.stream()
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .collect(Collectors.joining(","));
        return result.isBlank() ? null : result;
    }
}
