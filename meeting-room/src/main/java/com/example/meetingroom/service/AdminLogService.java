package com.example.meetingroom.service;

import com.example.meetingroom.common.config.JwtUtil;
import com.example.meetingroom.domain.AdminLog;
import com.example.meetingroom.repository.AdminLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminLogService {

    private final AdminLogRepository adminLogRepository;
    private final ObjectMapper objectMapper;
    private final SseService sseService;
    private final JwtUtil jwtUtil;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actionType, String description) {
        doSave(actionType, description, null, null, null, null, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actionType, String description, Map<String, Object> detail) {
        doSave(actionType, description, detail, null, null, null, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actionType, String description, Map<String, Object> detail,
                    Long targetId, String targetType) {
        doSave(actionType, description, detail, targetId, targetType, null, null);
    }

    // 변경 전후 스냅샷이 필요한 수정·삭제 작업에 사용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actionType, String description, Map<String, Object> detail,
                    Long targetId, String targetType,
                    Map<String, Object> beforeData, Map<String, Object> afterData) {
        doSave(actionType, description, detail, targetId, targetType, beforeData, afterData);
    }

    private void doSave(String actionType, String description, Map<String, Object> detail,
                        Long targetId, String targetType,
                        Map<String, Object> beforeData, Map<String, Object> afterData) {
        try {
            adminLogRepository.save(AdminLog.builder()
                    .actionType(truncate(actionType, 100))
                    .description(truncate(description, 500))
                    .detail(toJson(detail))
                    .ip(getClientIp())
                    .userAgent(truncate(getUserAgent(), 500))
                    .operatorId(getOperatorId())
                    .targetId(targetId)
                    .targetType(truncate(targetType, 50))
                    .beforeData(toJson(beforeData))
                    .afterData(toJson(afterData))
                    .requestUrl(truncate(getRequestUrl(), 500))
                    .httpMethod(getHttpMethod())
                    .build());

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sseService.broadcast("ADMIN", Map.of("action", actionType));
                }
            });
        } catch (Exception e) {
            // 로그 저장 실패는 조용히 무시 — 본 비즈니스 트랜잭션에 영향 없어야 함
        }
    }

    // DB ENUM과 불일치하는 메서드는 저장 시 오류를 막기 위해 null 처리
    private static final Set<String> VALID_HTTP_METHODS =
            Set.of("GET", "POST", "PUT", "PATCH", "DELETE");

    private Long getOperatorId() {
        try {
            HttpServletRequest req = getRequest();
            if (req == null) return null;
            String header = req.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) return null;
            return jwtUtil.getUserId(header.substring(7));
        } catch (Exception e) { return null; }
    }

    private String getRequestUrl() {
        try {
            HttpServletRequest req = getRequest();
            return req != null ? req.getRequestURI() : null;
        } catch (Exception e) { return null; }
    }

    private String getHttpMethod() {
        try {
            HttpServletRequest req = getRequest();
            if (req == null) return null;
            String method = req.getMethod();
            return VALID_HTTP_METHODS.contains(method) ? method : null;
        } catch (Exception e) { return null; }
    }

    private String getClientIp() {
        try {
            HttpServletRequest req = getRequest();
            if (req == null) return null;
            String xff = req.getHeader("X-Forwarded-For");
            return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
        } catch (Exception e) { return null; }
    }

    private String getUserAgent() {
        try {
            HttpServletRequest req = getRequest();
            return req != null ? req.getHeader("User-Agent") : null;
        } catch (Exception e) { return null; }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String toJson(Map<String, Object> map) {
        if (map == null) return null;
        try { return objectMapper.writeValueAsString(map); } catch (Exception e) { return null; }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
