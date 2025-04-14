package entity;

/**
 * Enumeration of withdrawal request statuses in the BTO system.
 * Demonstrates the use of Enum in Java to represent a fixed set of values.
 */
public enum WithdrawalStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected");
    
    private final String displayValue;
    
    /**
     * Constructor for WithdrawalStatus
     * @param displayValue the display value for the status
     */
    WithdrawalStatus(String displayValue) {
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
     * Get a WithdrawalStatus from its display value
     * @param displayValue the display value
     * @return the corresponding WithdrawalStatus, or null if not found
     */
    public static WithdrawalStatus fromDisplayValue(String displayValue) {
        for (WithdrawalStatus status : values()) {
            if (status.getDisplayValue().equalsIgnoreCase(displayValue)) {
                return status;
            }
        }
        return null;
    }
}