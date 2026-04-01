package com.example.meetingroom.repository;

import com.example.meetingroom.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByIsActiveTrue();
    long countByIsActiveTrue();
}
