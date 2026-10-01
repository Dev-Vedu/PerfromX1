package com.pms.service;

import com.pms.entity.*;
import com.pms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/** Creates demo accounts and sample work on first run so the app is not empty. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) return;

        User lead = userService.create(User.builder()
                .username("lead").fullName("Anjali Deshmukh").email("lead@example.com")
                .role(Role.LEAD).designation("Project Lead").department("Engineering").build(), "lead123");

        User hr = userService.create(User.builder()
                .username("hr").fullName("Rohit Kulkarni").email("hr@example.com")
                .role(Role.HR).designation("HR Manager").department("People").build(), "hr123");

        User emp1 = userService.create(User.builder()
                .username("priya").fullName("Priya Nair").email("priya@example.com")
                .role(Role.EMPLOYEE).designation("Backend Developer").department("Engineering").build(), "priya123");

        User emp2 = userService.create(User.builder()
                .username("arjun").fullName("Arjun Mehta").email("arjun@example.com")
                .role(Role.EMPLOYEE).designation("Frontend Developer").department("Engineering").build(), "arjun123");

        User emp3 = userService.create(User.builder()
                .username("sneha").fullName("Sneha Patil").email("sneha@example.com")
                .role(Role.EMPLOYEE).designation("QA Engineer").department("Quality").build(), "sneha123");

        Set<User> members = new LinkedHashSet<>(Set.of(emp1, emp2, emp3));

        Project portal = projectRepository.save(Project.builder()
                .name("Customer Portal Revamp")
                .description("Rebuild the customer-facing portal with a new billing view and self-service refunds.")
                .startDate(LocalDate.now().minusDays(20))
                .targetEndDate(LocalDate.now().plusDays(40))
                .status(ProjectStatus.ACTIVE).lead(lead).members(members).build());

        Project mobile = projectRepository.save(Project.builder()
                .name("Inventory Sync Service")
                .description("Nightly sync between the warehouse system and the storefront catalogue.")
                .startDate(LocalDate.now().minusDays(8))
                .targetEndDate(LocalDate.now().plusDays(25))
                .status(ProjectStatus.ACTIVE).lead(lead)
                .members(new LinkedHashSet<>(Set.of(emp1, emp3))).build());

        taskRepository.save(Task.builder()
                .title("Refund API endpoints").project(portal).assignee(emp1)
                .description("POST /refunds and GET /refunds/{id}, with idempotency keys and audit logging.")
                .startDate(LocalDate.now().minusDays(10))
                .deadline(LocalDate.now().minusDays(2))
                .originalDeadline(LocalDate.now().minusDays(2))
                .priority(Priority.CRITICAL).status(TaskStatus.IN_PROGRESS).progress(70).build());

        taskRepository.save(Task.builder()
                .title("Billing dashboard screens").project(portal).assignee(emp2)
                .description("Invoice list, invoice detail and the payment method switcher.")
                .startDate(LocalDate.now().minusDays(6))
                .deadline(LocalDate.now().plusDays(4))
                .originalDeadline(LocalDate.now().plusDays(4))
                .priority(Priority.HIGH).status(TaskStatus.IN_PROGRESS).progress(45).build());

        taskRepository.save(Task.builder()
                .title("Regression suite for checkout").project(portal).assignee(emp3)
                .description("Cover card, UPI and wallet paths including failed payment retries.")
                .startDate(LocalDate.now().minusDays(14))
                .deadline(LocalDate.now().minusDays(4))
                .originalDeadline(LocalDate.now().minusDays(4))
                .priority(Priority.MEDIUM).status(TaskStatus.COMPLETED).progress(100)
                .completedAt(LocalDateTime.now().minusDays(5)).build());

        taskRepository.save(Task.builder()
                .title("Warehouse feed parser").project(mobile).assignee(emp1)
                .description("Parse the nightly CSV drop, validate SKUs and queue the deltas.")
                .startDate(LocalDate.now().minusDays(3))
                .deadline(LocalDate.now().plusDays(1))
                .originalDeadline(LocalDate.now().plusDays(1))
                .priority(Priority.HIGH).status(TaskStatus.SUBMITTED).progress(100)
                .submittedAt(LocalDateTime.now().minusHours(6))
                .submissionNote("Parser and validation done. Retry logic covered by unit tests.").build());

        log.info("Demo data created. Sign in as lead/lead123, priya/priya123 or hr/hr123.");
    }
}
