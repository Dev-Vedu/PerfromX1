package com.pms.entity;

public enum Role {
    LEAD("Project Lead"),
    EMPLOYEE("Employee"),
    HR("HR / Admin");

    private final String label;
    Role(String label) { this.label = label; }
    public String getLabel() { return label; }
    public String authority() { return "ROLE_" + name(); }
}
