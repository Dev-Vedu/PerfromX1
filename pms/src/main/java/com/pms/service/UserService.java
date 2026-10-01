package com.pms.service;

import com.pms.entity.Role;
import com.pms.entity.User;
import com.pms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User byUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Unknown user: " + username));
    }

    public User current(Principal principal) {
        return byUsername(principal.getName());
    }

    public User byId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No user with id " + id));
    }

    public List<User> all() {
        return userRepository.findByActiveTrueOrderByFullNameAsc();
    }

    public List<User> employees() {
        return userRepository.findByRole(Role.EMPLOYEE);
    }

    public User create(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public void resetPassword(Long userId, String rawPassword) {
        User u = byId(userId);
        u.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(u);
    }

    public void setActive(Long userId, boolean active) {
        User u = byId(userId);
        u.setActive(active);
        userRepository.save(u);
    }
}
