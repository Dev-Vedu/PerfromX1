package com.pms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ReviewForm {
    private Long taskId;
    @Min(1) @Max(5) private int quality = 3;
    @Min(1) @Max(5) private int timeliness = 3;
    @Min(1) @Max(5) private int punctuality = 3;
    @Min(1) @Max(5) private int professionalism = 3;
    private String comments;
}
