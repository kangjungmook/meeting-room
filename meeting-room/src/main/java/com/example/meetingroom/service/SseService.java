package com.example.meetingroom.service;

import com.example.meetingroom.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SseService {

    private final ConcurrentHashMap<String, SseEmitter> emitters          = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String>     clientToEmployee  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> onlineCounts   = new ConcurrentHashMap<>();
    private final ObjectMapper   objectMapper;
    private final UserRepository userRepository;

    public SseService(UserRepository userRepository) {
        this.userRepository = userRepository;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ── 구독 ─────────────────────────────────────────────────────
    public SseEmitter subscribe(String employeeId) {
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        emitters.put(clientId, emitter);

        if (employeeId != null) {
            clientToEmployee.put(clientId, employeeId);
            onlineCounts.computeIfAbsent(employeeId, k -> new AtomicInteger(0)).incrementAndGet();
            broadcastOnlineStatus();
        }

        Runnable cleanup = () -> handleDisconnect(clientId);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            cleanup.run();
        }

        return emitter;
    }

    // ── 연결 해제 처리 (멱등: 이미 제거된 경우 즉시 반환) ─────────
    private void handleDisconnect(String clientId) {
        if (emitters.remove(clientId) == null) return; // 이미 처리된 경우 무시

        String eid = clientToEmployee.remove(clientId);
        if (eid == null) return;

        AtomicInteger cnt = onlineCounts.get(eid);
        if (cnt != null && cnt.decrementAndGet() <= 0) {
            onlineCounts.remove(eid);
            // lastLogoutAt 기록
            userRepository.findByEmployeeId(eid).ifPresent(user -> {
                user.setLastLogoutAt(LocalDateTime.now());
                userRepository.save(user);
            });
            // 관리자 화면 사용자 데이터 갱신 트리거
            broadcast("ADMIN", Map.of("action", "USER_OFFLINE", "employeeId", eid));
        }
        broadcastOnlineStatus();
    }

    // ── 온라인 목록 전파 ─────────────────────────────────────────
    private void broadcastOnlineStatus() {
        Set<String> online = getOnlineUsers();
        String json;
        try {
            json = objectMapper.writeValueAsString(online);
        } catch (Exception e) {
            return;
        }

        List<String> dead = new ArrayList<>();
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("ONLINE").data(json));
            } catch (IOException ex) {
                dead.add(id); // 실패한 클라이언트 수집
            }
        });
        // 브로드캐스트 중 발견된 끊어진 연결 정리
        dead.forEach(this::handleDisconnect);
    }

    // ── 일반 이벤트 전파 ─────────────────────────────────────────
    public void broadcast(String eventType, Object data) {
        String json;
        try {
            json = objectMapper.writeValueAsString(Map.of("type", eventType, "data", data));
        } catch (Exception e) {
            return;
        }

        List<String> dead = new ArrayList<>();
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("booking").data(json));
            } catch (IOException ex) {
                dead.add(id);
            }
        });
        dead.forEach(this::handleDisconnect);
    }

    // ── 30초마다 heartbeat → 끊어진 연결 즉시 탐지 ──────────────
    @Scheduled(fixedRate = 30_000)
    public void heartbeat() {
        List<String> dead = new ArrayList<>();
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("ping").data(""));
            } catch (IOException ex) {
                dead.add(id);
            }
        });
        dead.forEach(this::handleDisconnect);
    }

    public Set<String> getOnlineUsers() {
        return onlineCounts.keySet();
    }
}
