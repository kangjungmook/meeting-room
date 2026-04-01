package com.example.meetingroom.controller;

import com.example.meetingroom.common.config.JwtUtil;
import com.example.meetingroom.service.SseService;
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
    public SseEmitter subscribe(@RequestParam(required = false) String token) {
        String employeeId = null;
        if (token != null && jwtUtil.validateToken(token)) {
            employeeId = jwtUtil.getEmployeeId(token);
        }
        return sseService.subscribe(employeeId);
    }
}
