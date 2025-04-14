package control;

import entity.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Controls operations related to withdrawal requests in the BTO system.
 * Demonstrates Single Responsibility Principle by focusing only on withdrawal-related operations.
 */
public class WithdrawalControl {
    private static final String WITHDRAWALS_FILE = "files/resources/WithdrawalList.csv";
    private List<WithdrawalRequest> withdrawalRequests;
    
    /**
     * Constructor initializes the withdrawals list from storage
     */
    public WithdrawalControl() {
        this.withdrawalRequests = loadWithdrawals();
    }
    
    /**
     * Submit a new withdrawal request
     * @param application the application to withdraw
     * @param reason the reason for withdrawal
     * @return the created request, or null if submission failed
     */
    public WithdrawalRequest submitWithdrawalRequest(Application application, String reason) {
        // Check if application can be withdrawn
        if (!application.canWithdraw()) {
            return null;
        }
        
        // Check if there's already a pending withdrawal request for this application
        for (WithdrawalRequest request : withdrawalRequests) {
            if (request.getApplication().getApplicationID().equals(application.getApplicationID()) && 
                request.getStatus() == WithdrawalStatus.PENDING) {
                return null; // Already has a pending request
            }
        }
        
        // Create new withdrawal request
        WithdrawalRequest request = new WithdrawalRequest(application, reason);
        
        // Add to list
        withdrawalRequests.add(request);
        
        // Save to file
        if (saveWithdrawals()) {
            return request;
        }
        
        return null;
    }
    
    /**
     * Get withdrawal requests for a project
     * @param project the project
     * @return list of withdrawal requests for the project
     */
    public List<WithdrawalRequest> getWithdrawalRequestsForProject(Project project) {
        List<WithdrawalRequest> projectWithdrawals = new ArrayList<>();
        
        for (WithdrawalRequest request : withdrawalRequests) {
            if (request.getApplication().getProject().getProjectID().equals(project.getProjectID()) && 
                request.getStatus() == WithdrawalStatus.PENDING) {
                projectWithdrawals.add(request);
            }
        }
        
        return projectWithdrawals;
    }
    
    /**
     * Get withdrawal requests for an applicant
     * @param applicant the applicant
     * @return list of withdrawal requests by the applicant
     */
    public List<WithdrawalRequest> getWithdrawalRequestsByApplicant(Applicant applicant) {
        List<WithdrawalRequest> applicantWithdrawals = new ArrayList<>();
        
        for (WithdrawalRequest request : withdrawalRequests) {
            if (request.getApplication().getApplicant().getNRIC().equals(applicant.getNRIC())) {
                applicantWithdrawals.add(request);
            }
        }
        
        return applicantWithdrawals;
    }
    
    /**
     * Process a withdrawal request
     * @param manager the manager processing the request
     * @param request the request to process
     * @param approve true to approve, false to reject
     * @return true if processing is successful, false otherwise
     */
    public boolean processWithdrawalRequest(HDBManager manager, WithdrawalRequest request, boolean approve) {
        // Process the request
        boolean success = request.process(manager, approve);
        
        if (success) {
            // Save changes
            return saveWithdrawals();
        }
        
        return false;
    }
    
    /**
     * Cancel a withdrawal request
     * @param request the request to cancel
     * @return true if cancellation was successful, false otherwise
     */
    public boolean cancelWithdrawalRequest(WithdrawalRequest request) {
        // Check if request is still pending
        if (request.getStatus() != WithdrawalStatus.PENDING) {
            return false;
        }
        
        // Remove from list
        withdrawalRequests.remove(request);
        
        // Save changes
        return saveWithdrawals();
    }
    
    /**
     * Load withdrawal requests from file
     * @return list of withdrawal requests
     */
    private List<WithdrawalRequest> loadWithdrawals() {
        List<WithdrawalRequest> loadedWithdrawals = new ArrayList<>();
        
        try (Scanner fileScanner = new Scanner(new File(WITHDRAWALS_FILE))) {
            // Skip header if exists
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] values = line.split(",");
                if (values.length < 6) continue; // Invalid line
                
                // Parse withdrawal data
                try {
                    String requestID = values[0].trim();
                    String applicationID = values[1].trim();
                    String reason = values[2].trim();
                    long requestDate = Long.parseLong(values[3].trim());
                    String statusStr = values[4].trim();
                    
                    // Parse remaining fields
                    String processedDateStr = values[5].trim();
                    long processedDate = processedDateStr.isEmpty() ? 0 : Long.parseLong(processedDateStr);
                    String processedBy = values.length > 6 ? values[6].trim() : "";
                    
                    // Convert status string to enum
                    WithdrawalStatus status = WithdrawalStatus.valueOf(statusStr);
                    
                    // Find application (in a real system, this would come from an application repository)
                    ApplicationControl appControl = new ApplicationControl();
                    Application application = appControl.getApplicationByID(applicationID);
                    
                    if (application != null) {
                        // Create withdrawal request
                        // Since we can't use the constructor directly (it generates a new ID),
                        // we'll use reflection to set the fields
                        WithdrawalRequest request = new WithdrawalRequest(application, reason);
                        
                        // Use reflection to set private fields
                        java.lang.reflect.Field idField = WithdrawalRequest.class.getDeclaredField("requestID");
                        idField.setAccessible(true);
                        idField.set(request, requestID);
                        
                        java.lang.reflect.Field statusField = WithdrawalRequest.class.getDeclaredField("status");
                        statusField.setAccessible(true);
                        statusField.set(request, status);
                        
                        java.lang.reflect.Field reqDateField = WithdrawalRequest.class.getDeclaredField("requestDate");
                        reqDateField.setAccessible(true);
                        reqDateField.set(request, new Date(requestDate));
                        
                        // Set processed fields if available
                        if (processedDate > 0) {
                            java.lang.reflect.Field procDateField = WithdrawalRequest.class.getDeclaredField("processedDate");
                            procDateField.setAccessible(true);
                            procDateField.set(request, new Date(processedDate));
                            
                            if (!processedBy.isEmpty()) {
                                java.lang.reflect.Field procByField = WithdrawalRequest.class.getDeclaredField("processedBy");
                                procByField.setAccessible(true);
                                procByField.set(request, processedBy);
                            }
                        }
                        
                        loadedWithdrawals.add(request);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing withdrawal data: " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Withdrawals file not found. Starting with empty list.");
        }
        
        return loadedWithdrawals;
    }
    
    /**
     * Save withdrawal requests to file
     * @return true if successful, false otherwise
     */
    private boolean saveWithdrawals() {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(WITHDRAWALS_FILE))) {
                // Write header
                writer.println("RequestID,ApplicationID,Reason,RequestDate,Status,ProcessedDate,ProcessedBy");
                
                // Write withdrawals
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                
                for (WithdrawalRequest request : withdrawalRequests) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(request.getRequestID()).append(",");
                    sb.append(request.getApplication().getApplicationID()).append(",");
                    sb.append(request.getReason()).append(",");
                    sb.append(request.getRequestDate().getTime()).append(",");
                    sb.append(request.getStatus()).append(",");
                    
                    // Add processed date and processor if available
                    if (request.getProcessedDate() != null) {
                        sb.append(request.getProcessedDate().getTime()).append(",");
                        sb.append(request.getProcessedBy() != null ? request.getProcessedBy() : "");
                    } else {
                        sb.append(","); // Empty processed date
                    }
                    
                    writer.println(sb.toString());
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving withdrawals: " + e.getMessage());
            return false;
        }
    }
}