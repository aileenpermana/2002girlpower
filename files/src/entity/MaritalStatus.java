package entity;

/**
 * Enumeration of marital statuses in the BTO system.
 * Demonstrates the use of Enum in Java to represent a fixed set of values.
 */
public enum MaritalStatus {
    SINGLE("Single"),
    MARRIED("Married");
    
    private final String displayValue;
    
    /**
     * Constructor for MaritalStatus
     * @param displayValue the display value for the marital status
     */
    MaritalStatus(String displayValue) {
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
     * Get a MaritalStatus from its display value
     * @param displayValue the display value
     * @return the corresponding MaritalStatus, or null if not found
     */
    public static MaritalStatus fromDisplayValue(String displayValue) {
        for (MaritalStatus status : values()) {
            if (status.getDisplayValue().equalsIgnoreCase(displayValue)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * Convert a string representation to a MaritalStatus
     * @param str the string representation (can be the enum name or display value)
     * @return the corresponding MaritalStatus, or null if not found
     */
    public static MaritalStatus fromString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        
        // Try to match by enum name
        try {
            return MaritalStatus.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try to match by display value
            return fromDisplayValue(str);
        }
    }
}