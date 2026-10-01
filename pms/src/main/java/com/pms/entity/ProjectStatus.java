package com.pms.entity;

public enum ProjectStatus {
    PLANNING("Planning"), ACTIVE("Active"), ON_HOLD("On hold"), CLOSED("Closed");

    private final String label;
    ProjectStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
