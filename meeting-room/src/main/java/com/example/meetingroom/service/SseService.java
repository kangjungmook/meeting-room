package com.example.meetingroom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SseService {

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> clientToEmployee = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> onlineCounts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SseService() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public SseEmitter subscribe(String employeeId) {
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        emitters.put(clientId, emitter);

        if (employeeId != null) {
            clientToEmployee.put(clientId, employeeId);
            onlineCounts.computeIfAbsent(employeeId, k -> new AtomicInteger(0)).incrementAndGet();
            broadcastOnlineStatus();
        }

        Runnable cleanup = () -> {
            emitters.remove(clientId);
            String eid = clientToEmployee.remove(clientId);
            if (eid != null) {
                AtomicInteger cnt = onlineCounts.get(eid);
                if (cnt != null && cnt.decrementAndGet() <= 0) {
                    onlineCounts.remove(eid);
                }
                broadcastOnlineStatus();
            }
        };
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

    public Set<String> getOnlineUsers() {
        return onlineCounts.keySet();
    }

    private void broadcastOnlineStatus() {
        Set<String> online = getOnlineUsers();
        String json;
        try {
            json = objectMapper.writeValueAsString(online);
        } catch (Exception e) {
            return;
        }
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("ONLINE").data(json));
            } catch (IOException ex) {
                emitters.remove(id);
            }
        });
    }

    public void broadcast(String eventType, Object data) {
        String json;
        try {
            json = objectMapper.writeValueAsString(Map.of("type", eventType, "data", data));
        } catch (Exception e) {
            return;
        }

        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("booking").data(json));
            } catch (IOException ex) {
                emitters.remove(id);
            }
        });
    }
}
