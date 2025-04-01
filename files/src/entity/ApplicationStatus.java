package entity;

/**
 * Enum representing the possible application statuses in the BTO system.
 */
public enum ApplicationStatus {
    PENDING("Pending"),
    SUCCESSFUL("Successful"),
    UNSUCCESSFUL("Unsuccessful"),
    BOOKED("Booked");
    
    private final String displayValue;
    
    ApplicationStatus(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
    
    @Override
    public String toString() {
        return displayValue;
    }
    
    /**
     * Converts a string representation to the corresponding enum value.
     * @param text The string to convert
     * @return The matching ApplicationStatus enum
     * @throws IllegalArgumentException if no matching status is found
     */
    public static ApplicationStatus fromString(String text) {
        for (ApplicationStatus status : ApplicationStatus.values()) {
            if (status.displayValue.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No application status found with value: " + text);
    }
}