package com.pms.entity;

public enum TaskStatus {
    ASSIGNED("Assigned"),
    IN_PROGRESS("In progress"),
    SUBMITTED("Submitted for review"),
    REVISION("Revision requested"),
    COMPLETED("Completed");

    private final String label;
    TaskStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    public boolean isOpen() { return this != COMPLETED; }
}
