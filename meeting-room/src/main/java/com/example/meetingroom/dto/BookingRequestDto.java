package com.example.meetingroom.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class BookingRequestDto {
    private Long roomId;
    private String title;
    private String organizer;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private List<Long> attendeeIds;
}
