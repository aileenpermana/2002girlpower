package entity;

/**
 * Enum representing the marital status options for users in the BTO system.
 */
public enum MaritalStatus {
    SINGLE("Single"),
    MARRIED("Married");
    
    private final String displayValue;
    
    MaritalStatus(String displayValue) {
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
     * @return The matching MaritalStatus enum
     * @throws IllegalArgumentException if no matching status is found
     */
    public static MaritalStatus fromString(String text) {
        for (MaritalStatus status : MaritalStatus.values()) {
            if (status.displayValue.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No marital status found with value: " + text);
    }
}