package entity;

/**
 * Enumeration of registration statuses for HDB Officers in the BTO system.
 * Demonstrates the use of Enum in Java to represent a fixed set of values.
 */
public enum RegistrationStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected");
    
    private final String displayValue;
    
    /**
     * Constructor for RegistrationStatus
     * @param displayValue the display value for the status
     */
    RegistrationStatus(String displayValue) {
        this.displayValue = displayValue;
    }
    
    /**
     * Get the display value
     * @return display value
     */
    public String getDisplayValue() {
        return displayValue;
    }
    
    /**
     * Get a RegistrationStatus from its display value
     * @param displayValue the display value
     * @return the corresponding RegistrationStatus, or null if not found
     */
    public static RegistrationStatus fromDisplayValue(String displayValue) {
        for (RegistrationStatus status : values()) {
            if (status.getDisplayValue().equalsIgnoreCase(displayValue)) {
                return status;
            }
        }
        return null;
    }
}