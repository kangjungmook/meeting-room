package com.example.meetingroom.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateUserDto {
    private String employeeId;
    private String name;
    private String password;
    private String role;
    private String status;
}
