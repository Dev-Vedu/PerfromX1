package com.pms.dto;

import com.pms.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectForm {
    private Long id;
    @NotBlank(message = "Give the project a name")
    private String name;
    private String description;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate = LocalDate.now();
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate targetEndDate;
    private ProjectStatus status = ProjectStatus.ACTIVE;
    private List<Long> memberIds = new ArrayList<>();
}
