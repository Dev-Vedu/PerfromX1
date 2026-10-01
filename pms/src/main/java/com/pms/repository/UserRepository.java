package com.pms.repository;

import com.pms.entity.Role;
import com.pms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
    List<User> findByActiveTrueOrderByFullNameAsc();
    boolean existsByUsername(String username);
}
