package com.bankapp.bankapp_backend.enums;

public enum BankName {
    NOVA_BANK("Nova Bank"),
    STATE_BANK("State Bank"),
    CITY_BANK("City Bank"),
    NATIONAL_BANK("National Bank"),
    FEDERAL_BANK("Federal Bank"),
    METRO_BANK("Metro Bank"),
    REGIONAL_BANK("Regional Bank"),
    COMMERCIAL_BANK("Commercial Bank"),
    PEOPLE_BANK("People's Bank"),
    TRUST_BANK("Trust Bank");

    private final String displayName;

    BankName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
