package com.pms.entity;

public enum RequestStatus {
    PENDING("Waiting on lead"), APPROVED("Approved"), REJECTED("Rejected");

    private final String label;
    RequestStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
