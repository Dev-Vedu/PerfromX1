package com.pms.service;

import com.pms.entity.Attendance;
import com.pms.entity.User;
import com.pms.repository.AttendanceRepository;
import com.pms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public void recordLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            LocalDate today = LocalDate.now();
            Attendance a = attendanceRepository.findByEmployeeAndWorkDate(user, today)
                    .orElseGet(() -> Attendance.builder()
                            .employee(user).workDate(today)
                            .firstLogin(LocalDateTime.now()).build());
            a.setLastSeen(LocalDateTime.now());
            attendanceRepository.save(a);
        });
    }

    public void touch(User user) {
        attendanceRepository.findByEmployeeAndWorkDate(user, LocalDate.now()).ifPresent(a -> {
            a.setLastSeen(LocalDateTime.now());
            attendanceRepository.save(a);
        });
    }

    public long daysPresentSince(User user, LocalDate since) {
        return attendanceRepository.countByEmployeeAndWorkDateAfter(user, since);
    }
}
