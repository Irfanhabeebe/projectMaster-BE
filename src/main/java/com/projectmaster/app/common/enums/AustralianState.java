package com.projectmaster.app.common.enums;

/**
 * Enum representing Australian states and territories
 */
public enum AustralianState {
    NSW("New South Wales"),
    VIC("Victoria"),
    QLD("Queensland"),
    WA("Western Australia"),
    SA("South Australia"),
    TAS("Tasmania"),
    ACT("Australian Capital Territory"),
    NT("Northern Territory");

    private final String fullName;

    AustralianState(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCode() {
        return this.name();
    }
}