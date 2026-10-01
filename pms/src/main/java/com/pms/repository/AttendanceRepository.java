package com.pms.repository;

import com.pms.entity.Attendance;
import com.pms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeAndWorkDate(User employee, LocalDate workDate);
    List<Attendance> findByEmployeeAndWorkDateAfter(User employee, LocalDate after);
    long countByEmployeeAndWorkDateAfter(User employee, LocalDate after);
}
