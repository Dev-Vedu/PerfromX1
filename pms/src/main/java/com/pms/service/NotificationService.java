package com.pms.service;

import com.pms.entity.Notification;
import com.pms.entity.User;
import com.pms.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void push(User to, String category, String message, String link) {
        pushOnce(to, category, message, link, null);
    }

    /** Sends only if no notification with the same dedupe key exists for this user. */
    public void pushOnce(User to, String category, String message, String link, String dedupeKey) {
        if (to == null) return;
        if (dedupeKey != null && notificationRepository.existsByRecipientAndDedupeKey(to, dedupeKey)) return;
        notificationRepository.save(Notification.builder()
                .recipient(to).category(category).message(message)
                .link(link).dedupeKey(dedupeKey).build());
    }

    public List<Notification> recent(User user, int limit) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user, PageRequest.of(0, limit));
    }

    public long unreadCount(User user) {
        return notificationRepository.countByRecipientAndSeenFalse(user);
    }

    @Transactional
    public void markAllSeen(User user) {
        notificationRepository.markAllSeen(user);
    }
}
