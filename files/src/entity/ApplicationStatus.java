package entity;

/**
 * Enumeration of application statuses in the BTO system.
 * Demonstrates the use of Enum in Java to represent a fixed set of values.
 */
public enum ApplicationStatus {
    PENDING("Pending"),
    SUCCESSFUL("Successful"),
    UNSUCCESSFUL("Unsuccessful"),
    BOOKED("Booked");
    
    private final String displayValue;
    
    /**
     * Constructor for ApplicationStatus
     * @param displayValue the display value for the status
     */
    ApplicationStatus(String displayValue) {
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
     * Get an ApplicationStatus from its display value
     * @param displayValue the display value
     * @return the corresponding ApplicationStatus, or null if not found
     */
    public static ApplicationStatus fromDisplayValue(String displayValue) {
        for (ApplicationStatus status : values()) {
            if (status.getDisplayValue().equalsIgnoreCase(displayValue)) {
                return status;
            }
        }
        return null;
    }
}