package com.pms.dto;

import com.pms.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Salary and bonus guidance. HR always makes the final call. */
@Getter
@AllArgsConstructor
public class RewardSuggestion {
    private final User employee;
    private final ProductivityScore score;
    /** e.g. "8-10%" */
    private final String recommendation;
    /** e.g. "One month salary" */
    private final String bonusBand;
    private final String rationale;
}
