package com.pms.repository;

import com.pms.entity.Notification;
import com.pms.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    long countByRecipientAndSeenFalse(User recipient);

    boolean existsByRecipientAndDedupeKey(User recipient, String dedupeKey);

    @Modifying
    @Query("update Notification n set n.seen = true where n.recipient = :user and n.seen = false")
    void markAllSeen(@Param("user") User user);
}
