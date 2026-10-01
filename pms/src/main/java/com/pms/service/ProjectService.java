package com.pms.service;

import com.pms.dto.ProjectForm;
import com.pms.dto.ProjectHealth;
import com.pms.entity.*;
import com.pms.repository.ProjectRepository;
import com.pms.repository.TaskRepository;
import com.pms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notifications;

    public Project byId(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No project with id " + id));
    }

    public List<Project> visibleTo(User user) {
        if (user.getRole() == Role.HR) return projectRepository.findAll();
        return projectRepository.findVisibleTo(user);
    }

    public List<Project> ledBy(User lead) {
        return projectRepository.findByLead(lead);
    }

    public Project save(ProjectForm form, User lead) {
        Project project = form.getId() == null ? new Project() : byId(form.getId());
        project.setName(form.getName());
        project.setDescription(form.getDescription());
        project.setStartDate(form.getStartDate());
        project.setTargetEndDate(form.getTargetEndDate());
        project.setStatus(form.getStatus());
        if (project.getLead() == null) project.setLead(lead);

        Set<User> members = new LinkedHashSet<>(userRepository.findAllById(form.getMemberIds()));
        Set<User> added = new LinkedHashSet<>(members);
        if (project.getMembers() != null) added.removeAll(project.getMembers());
        project.setMembers(members);

        Project saved = projectRepository.save(project);
        added.forEach(m -> notifications.push(m, "TASK",
                "You were added to the project " + saved.getName(),
                "/projects/" + saved.getId()));
        return saved;
    }

    public void addMember(Long projectId, Long userId) {
        Project p = byId(projectId);
        User u = userRepository.findById(userId).orElseThrow();
        if (p.getMembers().add(u)) {
            projectRepository.save(p);
            notifications.push(u, "TASK", "You were added to the project " + p.getName(),
                    "/projects/" + p.getId());
        }
    }

    public void removeMember(Long projectId, Long userId) {
        Project p = byId(projectId);
        p.getMembers().removeIf(m -> m.getId().equals(userId));
        projectRepository.save(p);
    }

    /** Progress, overdue load and a risk score used by the dashboard and the risk alerts. */
    public ProjectHealth health(Project project) {
        List<Task> tasks = taskRepository.findByProjectOrderByDeadlineAsc(project);
        long total = tasks.size();
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long overdue = tasks.stream().filter(Task::isOverdue).count();
        long criticalOpen = tasks.stream()
                .filter(t -> t.getPriority() == Priority.CRITICAL && t.getStatus().isOpen()).count();

        int progress = total == 0 ? 0
                : (int) Math.round(tasks.stream().mapToInt(Task::getProgress).average().orElse(0));

        int risk = 0;
        if (total > 0) {
            risk += (int) Math.round(60.0 * overdue / total);
            risk += (int) Math.min(20, criticalOpen * 7);
            risk += (int) Math.round(20.0 * (100 - progress) / 100);
        }
        if (project.getTargetEndDate() != null
                && LocalDate.now().isAfter(project.getTargetEndDate()) && completed < total) {
            risk = Math.min(100, risk + 15);
        }
        risk = Math.max(0, Math.min(100, risk));

        String level = risk >= 60 ? "High" : risk >= 30 ? "Watch" : "Low";
        String headline;
        if (total == 0) {
            headline = "No tasks yet. Assign work to start tracking.";
        } else if (overdue > 0) {
            headline = overdue + " task" + (overdue == 1 ? "" : "s") + " past deadline.";
        } else if (criticalOpen > 0) {
            headline = criticalOpen + " critical task" + (criticalOpen == 1 ? "" : "s") + " still open.";
        } else {
            headline = "On track at " + progress + "% average progress.";
        }
        return new ProjectHealth(project, total, completed, overdue, criticalOpen, progress, risk, level, headline);
    }

    public List<ProjectHealth> healthFor(List<Project> projects) {
        List<ProjectHealth> out = new ArrayList<>();
        projects.forEach(p -> out.add(health(p)));
        out.sort(Comparator.comparingInt(ProjectHealth::getRiskScore).reversed());
        return out;
    }
}
