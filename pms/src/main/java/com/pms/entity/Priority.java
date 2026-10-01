package com.pms.entity;

public enum Priority {
    LOW("Low", 1), MEDIUM("Medium", 2), HIGH("High", 3), CRITICAL("Critical", 4);

    private final String label;
    private final int weight;
    Priority(String label, int weight) { this.label = label; this.weight = weight; }
    public String getLabel() { return label; }
    public int getWeight() { return weight; }
}
