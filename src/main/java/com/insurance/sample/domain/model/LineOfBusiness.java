package com.insurance.sample.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum LineOfBusiness {
    Property("Property"),
    Casualty("Casualty"),
    ANH("A&H"),
    Marine("Marine");

    private final String displayName;

    LineOfBusiness(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    public static LineOfBusiness fromDisplayName(String value) {
        for (LineOfBusiness lob : values()) {
            if (lob.displayName.equalsIgnoreCase(value)) {
                return lob;
            }
        }
        throw new IllegalArgumentException("Unknown LineOfBusiness: " + value);
    }
}
