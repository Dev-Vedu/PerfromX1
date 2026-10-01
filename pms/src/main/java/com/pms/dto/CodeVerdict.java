package com.pms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CodeVerdict {
    private final String verdict;
    private final int score;
    private final String findings;

    public boolean isOptimized() { return "OPTIMIZED".equals(verdict); }
}
