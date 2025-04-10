package entity;

/**
 * Enumeration of flat types in the BTO system.
 * Demonstrates the use of Enum in Java to represent a fixed set of values.
 */
public enum FlatType {
    TWO_ROOM("2-Room"),
    THREE_ROOM("3-Room");
    
    private final String displayValue;
    
    /**
     * Constructor for FlatType
     * @param displayValue the display value for the flat type
     */
    FlatType(String displayValue) {
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
     * Get a FlatType from its display value
     * @param displayValue the display value
     * @return the corresponding FlatType, or null if not found
     */
    public static FlatType fromDisplayValue(String displayValue) {
        for (FlatType type : values()) {
            if (type.getDisplayValue().equalsIgnoreCase(displayValue)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Convert a string representation to a FlatType
     * @param str the string representation (can be the enum name or display value)
     * @return the corresponding FlatType, or null if not found
     */
    public static FlatType fromString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        
        // Try to match by enum name
        try {
            return FlatType.valueOf(str.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            // Try to match by display value
            return fromDisplayValue(str);
        }
    }
}