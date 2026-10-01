package com.pms.service;

import com.pms.entity.*;
import com.pms.repository.ExtensionRequestRepository;
import com.pms.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtensionService {

    private final ExtensionRequestRepository requestRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notifications;

    public ExtensionRequest byId(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No request with id " + id));
    }

    public ExtensionRequest raise(Task task, User employee, LocalDate newDeadline, String reason) {
        ExtensionRequest req = ExtensionRequest.builder()
                .task(task).requestedBy(employee)
                .currentDeadline(task.getDeadline())
                .requestedDeadline(newDeadline)
                .reason(reason)
                .build();
        ExtensionRequest saved = requestRepository.save(req);
        notifications.push(task.getProject().getLead(), "APPROVAL",
                employee.getFullName() + " asked to move " + task.getTitle() + " to " + newDeadline,
                "/extensions");
        return saved;
    }

    public void decide(Long requestId, boolean approve, String comment) {
        ExtensionRequest req = byId(requestId);
        req.setStatus(approve ? RequestStatus.APPROVED : RequestStatus.REJECTED);
        req.setLeadComment(comment);
        req.setDecidedAt(LocalDateTime.now());
        requestRepository.save(req);

        Task task = req.getTask();
        if (approve) {
            task.setDeadline(req.getRequestedDeadline());
            task.setExtensionCount(task.getExtensionCount() + 1);
            taskRepository.save(task);
        }
        notifications.push(req.getRequestedBy(), "APPROVAL",
                "Extension " + (approve ? "approved" : "rejected") + " for " + task.getTitle(),
                "/tasks/" + task.getId());
    }

    public List<ExtensionRequest> pendingForLead(User lead) {
        return requestRepository.findForLeadByStatus(lead, RequestStatus.PENDING);
    }

    public List<ExtensionRequest> raisedBy(User user) {
        return requestRepository.findByRequestedByOrderByCreatedAtDesc(user);
    }

    public List<ExtensionRequest> forTask(Task task) {
        return requestRepository.findByTaskOrderByCreatedAtDesc(task);
    }
}
