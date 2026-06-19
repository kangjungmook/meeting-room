package com.example.meetingroom.common.config;

import com.example.meetingroom.domain.User;
import com.example.meetingroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 어드민 계정 초기값 (AdminService.doFullReset()에서도 사용)
    public static final String ADMIN_EMPLOYEE_ID = "admin";
    public static final String ADMIN_PASSWORD     = "Admin1234!";
    public static final String ADMIN_NAME         = "관리자";

    @Override
    public void run(String... args) {
        if (userRepository.findByEmployeeId(ADMIN_EMPLOYEE_ID).isEmpty()) {
            createAdmin();
            log.info("어드민 계정 최초 생성 완료 [사번: {}]", ADMIN_EMPLOYEE_ID);
        }
    }

    public User createAdmin() {
        return userRepository.save(User.builder()
            .employeeId(ADMIN_EMPLOYEE_ID)
            .password(passwordEncoder.encode(ADMIN_PASSWORD))
            .name(ADMIN_NAME)
            .role("ADMIN")
            .status("APPROVED")
            .passwordResetRequired(false)
            .build());
    }
}
