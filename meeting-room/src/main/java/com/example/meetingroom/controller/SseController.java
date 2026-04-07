package com.example.meetingroom.controller;

import com.example.meetingroom.common.config.JwtUtil;
import com.example.meetingroom.service.SseService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;
    private final JwtUtil jwtUtil;

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@RequestParam(required = false) String token,
                                HttpServletRequest request) {
        String resolvedToken = token;
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            resolvedToken = header.substring(7);
        } else if (resolvedToken == null) {
            if (request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    if ("sseToken".equals(c.getName())) { resolvedToken = c.getValue(); break; }
                }
            }
        }
        String employeeId = null;
        if (resolvedToken != null && jwtUtil.validateToken(resolvedToken)) {
            employeeId = jwtUtil.getEmployeeId(resolvedToken);
        }
        return sseService.subscribe(employeeId);
    }
}
