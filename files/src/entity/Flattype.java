package entity;

/**
 * Enum representing the available flat types in the BTO system.
 */
public enum FlatType {
    TWO_ROOM("2-Room"),
    THREE_ROOM("3-Room");
    
    private final String displayValue;
    
    FlatType(String displayValue) {
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
     * @return The matching FlatType enum
     * @throws IllegalArgumentException if no matching flat type is found
     */
    public static FlatType fromString(String text) {
        for (FlatType type : FlatType.values()) {
            if (type.displayValue.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No flat type found with value: " + text);
    }
}