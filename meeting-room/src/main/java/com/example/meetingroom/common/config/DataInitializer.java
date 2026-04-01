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

    // 초기 어드민 계정 정보
    private static final String ADMIN_EMPLOYEE_ID = "admin";
    private static final String ADMIN_PASSWORD     = "Admin1234!";
    private static final String ADMIN_NAME         = "관리자";

    @Override
    public void run(String... args) {
        userRepository.findByEmployeeId(ADMIN_EMPLOYEE_ID).ifPresentOrElse(
            admin -> {
                // 이미 존재하면 비밀번호 + 상태만 강제 리셋
                admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                admin.setRole("ADMIN");
                admin.setStatus("APPROVED");
                admin.setPasswordResetRequired(false);
                userRepository.save(admin);
                log.info("어드민 계정 비밀번호 리셋 완료 [사번: {}]", ADMIN_EMPLOYEE_ID);
            },
            () -> {
                // 없으면 새로 생성
                userRepository.save(User.builder()
                    .employeeId(ADMIN_EMPLOYEE_ID)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .name(ADMIN_NAME)
                    .role("ADMIN")
                    .status("APPROVED")
                    .build());
                log.info("어드민 계정 최초 생성 완료 [사번: {}]", ADMIN_EMPLOYEE_ID);
            }
        );
    }
}
