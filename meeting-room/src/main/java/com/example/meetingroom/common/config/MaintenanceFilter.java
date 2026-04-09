package com.example.meetingroom.common.config;

import com.example.meetingroom.domain.NotificationSetting;
import com.example.meetingroom.repository.NotificationSettingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@Order(10) // JwtFilter(기본 order) 이후 실행
@RequiredArgsConstructor
public class MaintenanceFilter extends OncePerRequestFilter {

    private final NotificationSettingRepository notificationSettingRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/") || path.startsWith("/api/admin/")
                || path.startsWith("/api/sse/") || path.startsWith("/kiosk")) {
            filterChain.doFilter(request, response);
            return;
        }

        NotificationSetting setting = notificationSettingRepository.findById(1L).orElse(null);
        if (setting != null && Boolean.TRUE.equals(setting.getMaintenanceMode())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                response.setStatus(503);
                response.setContentType("application/json;charset=UTF-8");
                objectMapper.writeValue(response.getWriter(),
                        Map.of("message", "현재 시스템 점검 중입니다. 잠시 후 다시 시도해주세요."));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
