package com.museum.model;

public enum ExhibitionStatus {
    PERMANENT, // Постоянная
    TEMPORARY;  // Временная

    public String getDisplayName() {
        return switch (this) {
            case PERMANENT -> "Постоянная";
            case TEMPORARY -> "Временная";
        };
    }
}

