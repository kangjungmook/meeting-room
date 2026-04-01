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

    // ── 퍼블릭 log() 오버로드 ──────────────────────────────────

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

    /** before/after 스냅샷 포함 — 수정 작업에 사용 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actionType, String description, Map<String, Object> detail,
                    Long targetId, String targetType,
                    Map<String, Object> beforeData, Map<String, Object> afterData) {
        doSave(actionType, description, detail, targetId, targetType, beforeData, afterData);
    }

    // ── 핵심 저장 ─────────────────────────────────────────────

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
                    .logLevel(resolveLogLevel(actionType))
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
            // 로그 저장 실패가 비즈니스 로직에 영향을 주지 않도록
        }
    }

    // ── 심각도 자동 분류 ──────────────────────────────────────
    // DB ENUM: 'INFO' | 'WARN' | 'ERROR' | 'CRITICAL'

    private static final Set<String> CRITICAL_ACTIONS = Set.of(
            "FULL_RESET", "USER_RESET", "BOOKING_RESET", "SYSTEM_RESET"
    );
    private static final Set<String> WARN_ACTIONS = Set.of(
            "BOOKING_CANCEL", "USER_REJECT", "USER_DELETE",
            "ROOM_DELETE", "PASSWORD_RESET", "FCM_CLEAR", "LOG_RESET"
    );
    // ERROR: 예외·실패 상황에서 명시적으로 호출 시 사용 (현재 자동분류 없음)

    private String resolveLogLevel(String actionType) {
        if (actionType == null) return "INFO";
        if (CRITICAL_ACTIONS.contains(actionType)) return "CRITICAL";
        if (WARN_ACTIONS.contains(actionType))     return "WARN";
        return "INFO";
    }

    // ── HTTP 메서드 유효값 (DB ENUM 기준) ─────────────────────
    private static final Set<String> VALID_HTTP_METHODS =
            Set.of("GET", "POST", "PUT", "PATCH", "DELETE");

    // ── 요청 컨텍스트 추출 ────────────────────────────────────

    /** JWT에서 userId 추출 → operatorId */
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
            // DB ENUM('GET','POST','PUT','PATCH','DELETE') 외 값은 null로 처리
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

    /** varchar(n) 컬럼 오버플로우 방지 */
    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
