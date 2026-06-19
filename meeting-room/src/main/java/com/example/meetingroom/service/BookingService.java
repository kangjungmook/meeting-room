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

    // 동시 예약 충돌을 막기 위해 SERIALIZABLE 격리 수준 사용
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

        // 신규 생성이므로 before는 null
        adminLogService.log("BOOKING_CREATE",
                "예약 등록: " + saved.getTitle() + " (" + saved.getOrganizer() + ")",
                null,
                saved.getId(), "BOOKING",
                null,
                bookingSnapshot(saved));

        Map<String, Object> createdData = new java.util.HashMap<>();
        createdData.put("id", saved.getId());
        createdData.put("title", saved.getTitle());
        createdData.put("organizer", saved.getOrganizer() != null ? saved.getOrganizer() : "");
        createdData.put("roomId", saved.getRoomId());
        createdData.put("userId", saved.getUserId());
        createdData.put("attendeeIds", saved.getAttendeeIds() != null
                ? List.copyOf(saved.getAttendeeIds()) : List.of());

        // 트랜잭션 커밋 후 lazy 로딩이 불가하므로 커밋 전에 값 캡처
        final List<Long> fcmAttendees = saved.getAttendeeIds() != null
                ? List.copyOf(saved.getAttendeeIds()) : List.of();
        final Long creatorId = userId;
        final String fcmTitle = "📅 회의 초대: " + saved.getTitle();
        final String fcmBody  = (saved.getOrganizer() != null ? saved.getOrganizer() : "")
                + "님이 초대했습니다 · "
                + saved.getStartTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
        // 반복 예약 일괄 생성 시 참석자에게 중복 FCM 알림이 가지 않도록 플래그 기본 true
        final boolean shouldNotifyAttendees = dto.getNotifyAttendees() == null || dto.getNotifyAttendees();

        // DB 잠금 해제 후 알림을 보내야 수신 측이 최신 데이터를 조회할 수 있음
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                sseService.broadcast("CREATED", createdData);
                if (shouldNotifyAttendees) {
                    fcmService.sendToAttendeesExcluding(fcmAttendees, creatorId, fcmTitle, fcmBody, "INVITATION");
                }
            }
        });
        return saved;
    }

    // 동시 예약 충돌을 막기 위해 SERIALIZABLE 격리 수준 사용
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking updateBooking(Long id, BookingRequestDto dto, Long userId, String role) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        checkOwnership(booking, userId, role, "수정");
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().isEqual(dto.getEndTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이후여야 합니다.");
        }
        List<Booking> overlaps = bookingRepository.findOverlappingBookingsExcluding(
                dto.getRoomId(), dto.getStartTime(), dto.getEndTime(), id);
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("해당 시간에 이미 예약이 존재합니다.");
        }

        populateAttendeeIds(booking);
        Map<String, Object> before = bookingSnapshot(booking);
        // 추가 초대된 참석자를 구분하기 위해 기존 참석자 목록을 미리 캡처
        final List<Long> previousAttendeeIds = booking.getAttendeeIds() != null
                ? List.copyOf(booking.getAttendeeIds()) : List.of();

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

        adminLogService.log("BOOKING_UPDATE",
                "예약 수정: " + updated.getTitle() + " (" + updated.getOrganizer() + ")",
                null,
                id, "BOOKING",
                before,
                bookingSnapshot(updated));

        Map<String, Object> updatedData = new java.util.HashMap<>();
        updatedData.put("id", updated.getId());
        updatedData.put("title", updated.getTitle());
        updatedData.put("organizer", updated.getOrganizer() != null ? updated.getOrganizer() : "");
        updatedData.put("roomId", updated.getRoomId());
        updatedData.put("userId", updated.getUserId());
        updatedData.put("attendeeIds", updated.getAttendeeIds() != null
                ? List.copyOf(updated.getAttendeeIds()) : List.of());

        final List<Long> currentAttendeeIds = updated.getAttendeeIds() != null
                ? List.copyOf(updated.getAttendeeIds()) : List.of();
        // 신규 초대 대상 = 현재 참석자 중 기존에 없던 사용자
        final List<Long> newlyInvitedIds = currentAttendeeIds.stream()
                .filter(aid -> !previousAttendeeIds.contains(aid))
                .toList();
        // 기존 참석자에게만 수정 알림 발송
        final List<Long> existingAttendeeIds = currentAttendeeIds.stream()
                .filter(previousAttendeeIds::contains)
                .toList();
        final Long editorId = userId;
        final String organizerName = updated.getOrganizer() != null ? updated.getOrganizer() : "";
        final String timeStr = updated.getStartTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
        final String updateTitle = "✏️ 회의 수정: " + updated.getTitle();
        final String updateBody  = organizerName + "님이 일정을 변경했습니다 · " + timeStr;
        final String inviteTitle = "📅 회의 초대: " + updated.getTitle();
        final String inviteBody  = organizerName + "님이 초대했습니다 · " + timeStr;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                sseService.broadcast("UPDATED", updatedData);
                if (!existingAttendeeIds.isEmpty()) {
                    fcmService.sendToAttendeesExcluding(existingAttendeeIds, editorId, updateTitle, updateBody, "BOOKING_UPDATED");
                }
                if (!newlyInvitedIds.isEmpty()) {
                    fcmService.sendToAttendeesExcluding(newlyInvitedIds, editorId, inviteTitle, inviteBody, "INVITATION");
                }
            }
        });
        return updated;
    }

    @Transactional
    public void cancelBooking(Long id, Long requestUserId, String role) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        checkOwnership(booking, requestUserId, role, "취소");

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

        final Long cancelledUserId = booking.getUserId();
        final List<Long> cancelledAttendees = booking.getAttendeeIds() != null
                ? List.copyOf(booking.getAttendeeIds()) : List.of();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                Map<String, Object> cancelledData = new java.util.HashMap<>();
                cancelledData.put("id", id);
                cancelledData.put("userId", cancelledUserId);
                cancelledData.put("attendeeIds", cancelledAttendees);
                sseService.broadcast("CANCELLED", cancelledData);
            }
        });
    }

    // 키오스크는 JWT 인증이 없으므로 roomId로 해당 회의실 예약인지 검증
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

    @Transactional
    public void endBookingNow(Long id, Long requestUserId, String role) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        checkOwnership(booking, requestUserId, role, "종료");

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

    private void checkOwnership(Booking booking, Long userId, String role, String action) {
        boolean isAdmin = "ADMIN".equals(role);
        boolean isOwner = booking.getUserId() != null && booking.getUserId().equals(userId);
        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("본인의 예약만 " + action + "할 수 있습니다.");
        }
    }

    // 예약 상태를 로그 before/after용 Map으로 직렬화
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

    private void saveAttendees(Long bookingId, List<Long> attendeeIds) {
        if (attendeeIds == null || attendeeIds.isEmpty()) return;
        attendeeIds.forEach(uid ->
            bookingAttendeeRepository.save(BookingAttendee.builder()
                .bookingId(bookingId)
                .userId(uid)
                .build())
        );
    }

    // DB에는 없는 transient 필드이므로 응답 전에 직접 채워줘야 함
    public void populateAttendeeIds(Booking booking) {
        booking.setAttendeeIds(bookingAttendeeRepository.findUserIdsByBookingId(booking.getId()));
        if (booking.getExternalAttendees() != null && !booking.getExternalAttendees().isBlank()) {
            booking.setExternalAttendeeNames(
                Arrays.asList(booking.getExternalAttendees().split(","))
            );
        }
    }

    private String toExternalStr(List<String> names) {
        if (names == null || names.isEmpty()) return null;
        String result = names.stream()
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .collect(Collectors.joining(","));
        return result.isBlank() ? null : result;
    }
}
