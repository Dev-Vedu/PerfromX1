package com.pms.dto;

import com.pms.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TaskForm {
    private Long id;
    @NotBlank(message = "Give the task a title")
    private String title;
    private String description;
    @NotNull(message = "Pick a project")
    private Long projectId;
    private Long assigneeId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate = LocalDate.now();
    @NotNull(message = "Set a deadline")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate deadline;
    private Priority priority = Priority.MEDIUM;
}
