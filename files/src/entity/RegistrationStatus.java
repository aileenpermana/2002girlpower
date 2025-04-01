// In entity/RegistrationStatus.java
package entity;

public enum RegistrationStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected");
    
    private final String displayValue;
    
    RegistrationStatus(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
}