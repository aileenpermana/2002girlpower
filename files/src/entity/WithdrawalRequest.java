package entity;

import java.util.Date;

/**
 * Represents a withdrawal request for a BTO application in the system.
 * Demonstrates the use of a separate class to handle withdrawal requests.
 */
public class WithdrawalRequest {
    private String requestID;
    private Application application;
    private String reason;
    private Date requestDate;
    private WithdrawalStatus status;
    private Date processedDate;
    private String processedBy; // NRIC of the manager who processed the request
    
    /**
     * Constructor for WithdrawalRequest
     * @param application the application to withdraw
     * @param reason the reason for withdrawal
     */
    public WithdrawalRequest(Application application, String reason) {
        this.requestID = generateRequestID(application);
        this.application = application;
        this.reason = reason;
        this.requestDate = new Date(); // Current date
        this.status = WithdrawalStatus.PENDING;
        this.processedDate = null;
        this.processedBy = null;
    }
    
    /**
     * Generate a request ID based on the application
     * @param application the application
     * @return a unique request ID
     */
    private String generateRequestID(Application application) {
        return "WDR-" + application.getApplicationID() + "-" + System.currentTimeMillis() % 10000;
    }
    
    /**
     * Get the request ID
     * @return request ID
     */
    public String getRequestID() {
        return requestID;
    }
    
    /**
     * Get the application
     * @return the application
     */
    public Application getApplication() {
        return application;
    }
    
    /**
     * Get the reason for withdrawal
     * @return reason
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * Set the reason for withdrawal
     * @param reason the reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    /**
     * Get the request date
     * @return request date
     */
    public Date getRequestDate() {
        return requestDate;
    }
    
    /**
     * Get the status of the withdrawal request
     * @return status
     */
    public WithdrawalStatus getStatus() {
        return status;
    }
    
    /**
     * Set the status of the withdrawal request
     * @param status the new status
     */
    public void setStatus(WithdrawalStatus status) {
        this.status = status;
    }
    
    /**
     * Get the processed date
     * @return processed date
     */
    public Date getProcessedDate() {
        return processedDate;
    }
    
    /**
     * Set the processed date
     * @param processedDate the processed date
     */
    public void setProcessedDate(Date processedDate) {
        this.processedDate = processedDate;
    }
    
    /**
     * Get the ID of the manager who processed the request
     * @return manager ID
     */
    public String getProcessedBy() {
        return processedBy;
    }
    
    /**
     * Set the ID of the manager who processed the request
     * @param processedBy manager ID
     */
    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }
    
    /**
     * Process the withdrawal request
     * @param manager the manager processing the request
     * @param approve true to approve, false to reject
     * @return true if processing is successful, false otherwise
     */
    public boolean process(HDBManager manager, boolean approve) {
        if (status != WithdrawalStatus.PENDING) {
            return false; // Already processed
        }
        
        // Update status
        status = approve ? WithdrawalStatus.APPROVED : WithdrawalStatus.REJECTED;
        processedDate = new Date();
        processedBy = manager.getNRIC();
        
        // If approved, update application status
        if (approve) {
            // Process the actual withdrawal via the manager
            return manager.processWithdrawalRequest(application, true);
        }
        
        return true;
    }
}