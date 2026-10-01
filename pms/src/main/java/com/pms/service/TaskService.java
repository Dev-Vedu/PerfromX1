package com.pms.service;

import com.pms.dto.TaskForm;
import com.pms.entity.*;
import com.pms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkLogRepository workLogRepository;
    private final NotificationService notifications;

    public Task byId(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No task with id " + id));
    }

    public List<Task> forProject(Project project) {
        return taskRepository.findByProjectOrderByDeadlineAsc(project);
    }

    public List<Task> forAssignee(User user) {
        return taskRepository.findByAssigneeOrderByDeadlineAsc(user);
    }

    public List<Task> awaitingReview(User lead) {
        return taskRepository.findByLeadAndStatus(lead, TaskStatus.SUBMITTED);
    }

    public List<Task> overdue() {
        return taskRepository.findOverdue(LocalDate.now());
    }

    public List<Task> dueWithin(int days) {
        return taskRepository.findDueBetween(LocalDate.now(), LocalDate.now().plusDays(days));
    }

    public Task assign(TaskForm form) {
        Task task = form.getId() == null ? new Task() : byId(form.getId());
        boolean isNew = task.getId() == null;

        task.setTitle(form.getTitle());
        task.setDescription(form.getDescription());
        task.setProject(projectRepository.findById(form.getProjectId()).orElseThrow());
        task.setAssignee(form.getAssigneeId() == null ? null
                : userRepository.findById(form.getAssigneeId()).orElse(null));
        task.setStartDate(form.getStartDate());
        task.setDeadline(form.getDeadline());
        if (task.getOriginalDeadline() == null) task.setOriginalDeadline(form.getDeadline());
        task.setPriority(form.getPriority());

        Task saved = taskRepository.save(task);
        if (isNew && saved.getAssignee() != null) {
            notifications.push(saved.getAssignee(), "TASK",
                    "New task: " + saved.getTitle() + " — due " + saved.getDeadline(),
                    "/tasks/" + saved.getId());
        }
        return saved;
    }

    /** Employees update this at end of session; the prompt lives in the layout footer. */
    public void updateProgress(Task task, User employee, int progress, String note) {
        int before = task.getProgress();
        int clamped = Math.max(0, Math.min(100, progress));
        task.setProgress(clamped);
        if (clamped > 0 && task.getStatus() == TaskStatus.ASSIGNED) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
        taskRepository.save(task);

        workLogRepository.save(WorkLog.builder()
                .task(task).employee(employee)
                .progressBefore(before).progressAfter(clamped)
                .note(note).build());
    }

    public void submitForReview(Task task, User employee, String note, MultipartFile file) {
        task.setStatus(TaskStatus.SUBMITTED);
        task.setSubmittedAt(LocalDateTime.now());
        task.setSubmissionNote(note);
        task.setProgress(100);

        if (file != null && !file.isEmpty()) {
            try {
                Path dir = Paths.get("uploads").toAbsolutePath().normalize();
                Files.createDirectories(dir);
                String stored = UUID.randomUUID() + "-" +
                        Paths.get(file.getOriginalFilename()).getFileName().toString();
                Files.copy(file.getInputStream(), dir.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
                task.setAttachmentPath(stored);
                task.setAttachmentName(file.getOriginalFilename());
            } catch (IOException e) {
                throw new IllegalStateException("Could not save the attachment: " + e.getMessage(), e);
            }
        }
        taskRepository.save(task);

        workLogRepository.save(WorkLog.builder()
                .task(task).employee(employee).progressBefore(task.getProgress())
                .progressAfter(100).note("Submitted for review").build());

        notifications.push(task.getProject().getLead(), "REVIEW",
                employee.getFullName() + " submitted " + task.getTitle() + " for review",
                "/tasks/" + task.getId());
    }

    public void complete(Task task, String comment) {
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setProgress(100);
        task.setReviewComment(comment);
        taskRepository.save(task);
        notifications.push(task.getAssignee(), "REVIEW",
                "Your task " + task.getTitle() + " was marked complete",
                "/tasks/" + task.getId());
    }

    public void requestRevision(Task task, String comment) {
        task.setStatus(TaskStatus.REVISION);
        task.setReviewComment(comment);
        task.setProgress(Math.min(task.getProgress(), 90));
        taskRepository.save(task);
        notifications.push(task.getAssignee(), "REVIEW",
                "Revisions requested on " + task.getTitle(),
                "/tasks/" + task.getId());
    }

    public List<WorkLog> logs(Task task) {
        return workLogRepository.findByTaskOrderByLoggedAtDesc(task);
    }

    public boolean canEmployeeSee(Task task, User user) {
        if (user.getRole() == Role.HR) return true;
        if (task.getProject().getLead().getId().equals(user.getId())) return true;
        return task.getAssignee() != null && task.getAssignee().getId().equals(user.getId());
    }
}
