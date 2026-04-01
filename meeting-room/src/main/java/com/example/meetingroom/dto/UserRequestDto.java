package com.example.meetingroom.dto;

import lombok.Getter;

@Getter
public class UserRequestDto {
    private String employeeId;
    private String password;
    private String name;
    private boolean rememberMe;
}