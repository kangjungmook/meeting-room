package com.example.meetingroom.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_logs")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작업 유형 코드 (예: BOOKING_CREATE, USER_APPROVE)
    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType;

    // 심각도 — DB ENUM 그대로 매핑
    @Column(name = "log_level", nullable = false,
            columnDefinition = "enum('INFO','WARN','ERROR','CRITICAL') default 'INFO'")
    @Builder.Default
    private String logLevel = "INFO";

    // 작업 수행자 ID (FK → users.id, NULL=시스템 자동)
    @Column(name = "operator_id")
    private Long operatorId;

    // 대상 리소스 종류 (예: BOOKING, USER, ROOM)
    @Column(name = "target_type", length = 50)
    private String targetType;

    // 대상 리소스 PK ID
    @Column(name = "target_id")
    private Long targetId;

    // 작업 요약 설명
    @Column(name = "description", length = 500)
    private String description;

    // 변경 전 데이터 스냅샷 JSON (수정·삭제 시)
    @Column(name = "before_data", columnDefinition = "LONGTEXT")
    private String beforeData;

    // 변경 후 데이터 스냅샷 JSON (생성·수정 시)
    @Column(name = "after_data", columnDefinition = "LONGTEXT")
    private String afterData;

    // 상세 정보 (스택트레이스 등)
    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    // 요청 API 엔드포인트 URL
    @Column(name = "request_url", length = 500)
    private String requestUrl;

    // HTTP 메서드 — DB ENUM 그대로 매핑
    @Column(name = "http_method",
            columnDefinition = "enum('GET','POST','PUT','PATCH','DELETE')")
    private String httpMethod;

    // 요청자 IP (IPv4/IPv6)
    @Column(name = "ip", length = 45)
    private String ip;

    // 요청자 User-Agent
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false,
            columnDefinition = "datetime default current_timestamp()")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "datetime default current_timestamp() on update current_timestamp()")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // logLevel 기본값 보호 (Builder.Default가 누락된 경우 대비)
        if (this.logLevel == null) this.logLevel = "INFO";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
