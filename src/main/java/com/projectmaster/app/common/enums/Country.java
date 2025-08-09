package com.projectmaster.app.common.enums;

/**
 * Enum representing supported countries
 */
public enum Country {
    AUSTRALIA("Australia", "AU"),
    NEW_ZEALAND("New Zealand", "NZ"),
    UNITED_STATES("United States", "US"),
    CANADA("Canada", "CA"),
    UNITED_KINGDOM("United Kingdom", "GB");

    private final String name;
    private final String code;

    Country(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}