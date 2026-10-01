package com.pms.service;

import com.pms.dto.ProductivityScore;
import com.pms.dto.ReviewForm;
import com.pms.dto.RewardSuggestion;
import com.pms.entity.*;
import com.pms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceReviewRepository reviewRepository;
    private final TaskRepository taskRepository;
    private final ExtensionRequestRepository extensionRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationService notifications;

    public PerformanceReview rate(ReviewForm form, Task task, User reviewer) {
        PerformanceReview review = reviewRepository.findByTask(task).orElseGet(PerformanceReview::new);
        review.setTask(task);
        review.setEmployee(task.getAssignee());
        review.setReviewer(reviewer);
        review.setQuality(form.getQuality());
        review.setTimeliness(form.getTimeliness());
        review.setPunctuality(form.getPunctuality());
        review.setProfessionalism(form.getProfessionalism());
        review.setComments(form.getComments());
        PerformanceReview saved = reviewRepository.save(review);

        notifications.push(task.getAssignee(), "REVIEW",
                "You were rated " + String.format("%.1f", saved.average()) + "/5 on " + task.getTitle(),
                "/performance/me");
        return saved;
    }

    public List<PerformanceReview> reviewsFor(User employee) {
        return reviewRepository.findByEmployeeOrderByCreatedAtDesc(employee);
    }

    /** Composite productivity score from delivery, punctuality, ratings and attendance. */
    public ProductivityScore scoreFor(User employee) {
        List<Task> tasks = taskRepository.findByAssigneeOrderByDeadlineAsc(employee);
        if (tasks.isEmpty()) return ProductivityScore.empty();

        long total = tasks.size();
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long open = total - completed;
        long overdue = tasks.stream().filter(Task::isOverdue).count();
        long onTime = tasks.stream().filter(Task::finishedOnTime).count();

        double completionRate = 100.0 * completed / total;
        double onTimeRate = completed == 0 ? 0 : 100.0 * onTime / completed;

        long extensions = extensionRepository.countByRequestedBy(employee);

        List<PerformanceReview> reviews = reviewRepository.findByEmployeeOrderByCreatedAtDesc(employee);
        double avgRating = reviews.isEmpty() ? 0
                : reviews.stream().mapToDouble(PerformanceReview::average).average().orElse(0);

        long attendanceDays = attendanceRepository
                .countByEmployeeAndWorkDateAfter(employee, LocalDate.now().minusDays(30));

        double ratingPart = reviews.isEmpty() ? 60 : (avgRating / 5.0) * 100;
        double attendancePart = Math.min(100.0, attendanceDays / 22.0 * 100);
        double extensionPenalty = Math.min(12, extensions * 3);
        double overduePenalty = Math.min(15, overdue * 5);

        double raw = 0.35 * completionRate
                + 0.25 * onTimeRate
                + 0.25 * ratingPart
                + 0.15 * attendancePart
                - extensionPenalty - overduePenalty;

        int score = (int) Math.round(Math.max(0, Math.min(100, raw)));
        String band = score >= 85 ? "Excellent" : score >= 70 ? "Strong"
                : score >= 50 ? "Steady" : "Needs support";

        return new ProductivityScore(total, completed, open, overdue, round1(completionRate),
                round1(onTimeRate), extensions, round1(avgRating), attendanceDays, score, band);
    }

    /** Suggestion only. HR or the lead decides. */
    public RewardSuggestion rewardFor(User employee) {
        ProductivityScore s = scoreFor(employee);
        String increment;
        String bonus;
        String rationale;

        if (s.getTotalTasks() == 0) {
            increment = "Not enough data";
            bonus = "None";
            rationale = "No completed work on record yet.";
        } else if (s.getScore() >= 85) {
            increment = "10-12%";
            bonus = "One month salary";
            rationale = "Consistently delivers on time with high review scores.";
        } else if (s.getScore() >= 70) {
            increment = "6-9%";
            bonus = "Half month salary";
            rationale = "Reliable delivery with a few slipped deadlines.";
        } else if (s.getScore() >= 50) {
            increment = "3-5%";
            bonus = "None";
            rationale = "Meets expectations but timeliness or quality needs attention.";
        } else {
            increment = "Hold";
            bonus = "None";
            rationale = "Delivery and deadline adherence are below the team baseline. "
                    + "Consider a support plan before the next cycle.";
        }
        return new RewardSuggestion(employee, s, increment, bonus, rationale);
    }

    public List<RewardSuggestion> rewardsFor(List<User> employees) {
        List<RewardSuggestion> out = new ArrayList<>();
        employees.forEach(e -> out.add(rewardFor(e)));
        out.sort((a, b) -> Integer.compare(b.getScore().getScore(), a.getScore().getScore()));
        return out;
    }

    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }
}
