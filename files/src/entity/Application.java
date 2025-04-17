package entity;

import java.util.Date;

/**
 * Represents a BTO application in the system.
 */
public class Application {
    private final String applicationID;
    private final Applicant applicant;
    private final Project project;
    private ApplicationStatus status;
    private final Date applicationDate;
    private Date statusUpdateDate;
    private Flat bookedFlat;
    
    /**
     * Constructor for Application
     */
    public Application(Applicant applicant, Project project) {
        this.applicationID = generateApplicationID(applicant, project);
        this.applicant = applicant;
        this.project = project;
        this.status = ApplicationStatus.PENDING; // Start with pending status
        this.applicationDate = new Date(); // Current date
        this.statusUpdateDate = new Date(); // Same as application date initially
        this.bookedFlat = null;
        
        // Add this application to the applicant's list
        applicant.addApplication(this);
    }
    
    /**
     * Generate an application ID
     * @param applicant the applicant
     * @param project the project
     * @return generated application ID
     */
    private String generateApplicationID(Applicant applicant, Project project) {
        // Simple algorithm: first 3 chars of NRIC + first 3 chars of project ID + timestamp
        String nricPart = applicant.getNRIC().substring(0, 3);
        String projectPart = project.getProjectID().substring(0, 3);
        return nricPart + projectPart + System.currentTimeMillis() % 10000;
    }
    
    /**
     * Get application ID
     * @return application ID
     */
    public String getApplicationID() {
        return applicationID;
    }
    
    /**
     * Get applicant
     * @return the applicant
     */
    public Applicant getApplicant() {
        return applicant;
    }
    
    /**
     * Get project
     * @return the project
     */
    public Project getProject() {
        return project;
    }
    
    /**
     * Set application status
     * @param status new status
     */
    public void setStatus(ApplicationStatus status) {
        // Validate status transitions
        if (this.status == ApplicationStatus.PENDING && 
            (status == ApplicationStatus.SUCCESSFUL || status == ApplicationStatus.UNSUCCESSFUL)) {
            this.status = status;
        } else if (this.status == ApplicationStatus.SUCCESSFUL && status == ApplicationStatus.BOOKED) {
            this.status = status;
        } else {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to " + status);
        }
        this.statusUpdateDate = new Date();
    }
    
    /**
     * Get application status
     * @return current status
     */
    public ApplicationStatus getStatus() {
        return status;
    }
    
    /**
     * Get application date
     * @return application date
     */
    public Date getApplicationDate() {
        return applicationDate;
    }
    
    /**
     * Get status update date
     * @return status update date
     */
    public Date getStatusUpdateDate() {
        return statusUpdateDate;
    }
    
    /**
     * Set booked flat
     * @param flat the flat that has been booked
     */
    public void setBookedFlat(Flat flat) {
        if (!canBook()) {
            throw new IllegalStateException("Cannot book flat in current status: " + status);
        }
        this.bookedFlat = flat;
        this.applicant.setBookedFlat(flat);
    }
    
    /**
     * Get booked flat
     * @return the booked flat or null if none
     */
    public Flat getBookedFlat() {
        return bookedFlat;
    }
    
    /**
     * Check if this application can proceed to booking
     * @return true if can book, false otherwise
     */
    public boolean canBook() {
        return status == ApplicationStatus.SUCCESSFUL && bookedFlat == null;
    }
    
    /**
     * Check if this application can be withdrawn
     * @return true if can withdraw, false otherwise
     */
    public boolean canWithdraw() {
        // Can withdraw if pending, successful, or even booked (though might have penalties)
        return status == ApplicationStatus.PENDING || 
               status == ApplicationStatus.SUCCESSFUL || 
               status == ApplicationStatus.BOOKED;
    }
    
    @Override
    public String toString() {
        return "Application{" +
                "applicationID='" + applicationID + '\'' +
                ", applicant=" + applicant.getName() +
                ", project=" + project.getProjectName() +
                ", status=" + status +
                ", applicationDate=" + applicationDate +
                '}';
    }
}