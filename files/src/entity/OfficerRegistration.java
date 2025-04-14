package entity;

import java.util.Date;

/**
 * Represents an officer registration for a BTO project in the system.
 * Demonstrates the use of a separate class to handle officer registrations.
 */
public class OfficerRegistration {
    private String registrationID;
    private HDBOfficer officer;
    private Project project;
    private RegistrationStatus status;
    private Date registrationDate;
    private Date processedDate;
    private String processedBy; // NRIC of the manager who processed the registration
    
    /**
     * Constructor for OfficerRegistration
     * @param officer the officer registering
     * @param project the project to register for
     */
    public OfficerRegistration(HDBOfficer officer, Project project) {
        this.registrationID = generateRegistrationID(officer, project);
        this.officer = officer;
        this.project = project;
        this.status = RegistrationStatus.PENDING;
        this.registrationDate = new Date(); // Current date
        this.processedDate = null;
        this.processedBy = null;
    }
    
    /**
     * Generate a registration ID based on the officer and project
     * @param officer the officer
     * @param project the project
     * @return a unique registration ID
     */
    private String generateRegistrationID(HDBOfficer officer, Project project) {
        return "REG-" + officer.getOfficerID() + "-" + project.getProjectID();
    }
    
    /**
     * Get the registration ID
     * @return registration ID
     */
    public String getRegistrationID() {
        return registrationID;
    }
    
    /**
     * Get the officer
     * @return the officer
     */
    public HDBOfficer getOfficer() {
        return officer;
    }
    
    /**
     * Get the project
     * @return the project
     */
    public Project getProject() {
        return project;
    }
    
    /**
     * Get the status of the registration
     * @return status
     */
    public RegistrationStatus getStatus() {
        return status;
    }
    
    /**
     * Set the status of the registration
     * @param status the new status
     */
    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }
    
    /**
     * Get the registration date
     * @return registration date
     */
    public Date getRegistrationDate() {
        return registrationDate;
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
     * Get the ID of the manager who processed the registration
     * @return manager ID
     */
    public String getProcessedBy() {
        return processedBy;
    }
    
    /**
     * Set the ID of the manager who processed the registration
     * @param processedBy manager ID
     */
    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }
    
    /**
     * Process the registration
     * @param manager the manager processing the registration
     * @param approve true to approve, false to reject
     * @return true if processing is successful, false otherwise
     */
    public boolean process(HDBManager manager, boolean approve) {
        if (status != RegistrationStatus.PENDING) {
            return false; // Already processed
        }
        
        // Check if manager is in charge of the project
        if (!project.getManagerInCharge().getManagerID().equals(manager.getManagerID())) {
            return false;
        }
        
        // Update status
        status = approve ? RegistrationStatus.APPROVED : RegistrationStatus.REJECTED;
        processedDate = new Date();
        processedBy = manager.getNRIC();
        
        // If approved, update project and officer
        if (approve) {
            // Check if there are available slots
            if (project.getAvailableOfficerSlots() <= 0) {
                return false;
            }
            
            // Add officer to project
            boolean success = project.addOfficer(officer);
            if (success) {
                officer.addHandlingProject(project);
                return true;
            }
            return false;
        }
        
        return true; // Rejection requires no further action
    }
}