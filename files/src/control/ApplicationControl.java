package control;

import entity.*;
import java.util.*;
import java.util.stream.Collectors;
import utils.ApplicationCSVStorage;
import utils.DataStorage;

/**
 * Enhanced control class for managing applications in the BTO system.
 * Demonstrates Dependency Inversion Principle and Single Responsibility Principle.
 */
public class ApplicationControl {
    private final DataStorage<Application> applicationStorage;
    private final ProjectControl projectControl;
    
    /**
     * Constructor with default storage
     */
    public ApplicationControl() {
        this.applicationStorage = new ApplicationCSVStorage();
        this.projectControl = new ProjectControl();
    }
    
    /**
     * Constructor with custom storage (for testing or different storage methods)
     * Demonstrates Dependency Inversion Principle
     */
    public ApplicationControl(DataStorage<Application> storage) {
        this.applicationStorage = storage;
        this.projectControl = new ProjectControl();
    }
    
    /**
     * Get all applications in the system
     * @return list of all applications
     */
    public List<Application> getAllApplications() {
        return applicationStorage.readAll();
    }
    
    /**
     * Get all applications for a project
     * @param project the project
     * @return list of all applications for the project
     */
    public List<Application> getAllApplications(Project project) {
        return getAllApplications().stream()
            .filter(app -> app.getProject().getProjectID().equals(project.getProjectID()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get pending applications for a project
     * @param project the project
     * @return list of pending applications
     */
    public List<Application> getPendingApplications(Project project) {
        return getAllApplications(project).stream()
            .filter(app -> app.getStatus() == ApplicationStatus.PENDING)
            .collect(Collectors.toList());
    }
    
    /**
     * Get successful applications for a project
     * @param project the project
     * @return list of successful applications
     */
    public List<Application> getSuccessfulApplications(Project project) {
        return getAllApplications(project).stream()
            .filter(app -> app.getStatus() == ApplicationStatus.SUCCESSFUL)
            .collect(Collectors.toList());
    }
    
    /**
     * Get booked applications for a project
     * @param project the project
     * @return list of booked applications
     */
    public List<Application> getBookedApplications(Project project) {
        return getAllApplications(project).stream()
            .filter(app -> app.getStatus() == ApplicationStatus.BOOKED)
            .collect(Collectors.toList());
    }
    
    /**
     * Get unsuccessful applications for a project
     * @param project the project
     * @return list of unsuccessful applications
     */
    public List<Application> getUnsuccessfulApplications(Project project) {
        return getAllApplications(project).stream()
            .filter(app -> app.getStatus() == ApplicationStatus.UNSUCCESSFUL)
            .collect(Collectors.toList());
    }
    
    /**
     * Get applications for an applicant
     * @param applicant the applicant
     * @return list of applications for the applicant
     */
    public List<Application> getApplicationsForApplicant(Applicant applicant) {
        return getAllApplications().stream()
            .filter(app -> app.getApplicant().getNRIC().equals(applicant.getNRIC()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get application by ID
     * @param applicationID the application ID
     * @return the application, or null if not found
     */
    public Application getApplicationByID(String applicationID) {
        return getAllApplications().stream()
            .filter(app -> app.getApplicationID().equals(applicationID))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Find an application by applicant NRIC and project
     * @param nric the applicant's NRIC
     * @param project the project
     * @return the application, or null if not found
     */
    public Application findApplicationByNRICAndProject(String nric, Project project) {
        return getAllApplications().stream()
            .filter(app -> app.getApplicant().getNRIC().equals(nric) &&
                        app.getProject().getProjectID().equals(project.getProjectID()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Submit a new application
     * @param applicant the applicant
     * @param project the project to apply for
     * @return true if submission is successful, false otherwise
     */
    public boolean submitApplication(Applicant applicant, Project project) {
        // Check if applicant already has an active application
        if (hasActiveApplication(applicant)) {
            return false;
        }
        
        // Check if applicant is eligible for this project
        if (!project.checkEligibility(applicant)) {
            return false;
        }
        
        // Check if project is open for application
        if (!project.isOpenForApplication()) {
            return false;
        }
        
        // Create new application
        Application application = new Application(applicant, project);
        
        // Save to storage
        return applicationStorage.save(application);
    }
    
    /**
     * Check if applicant has an active application
     * @param applicant the applicant
     * @return true if has active application, false otherwise
     */
    private boolean hasActiveApplication(Applicant applicant) {
        return getApplicationsForApplicant(applicant).stream()
            .anyMatch(app -> app.getStatus() == ApplicationStatus.PENDING || 
                           app.getStatus() == ApplicationStatus.SUCCESSFUL);
    }
    
    /**
     * Update an application
     * @param application the application to update
     * @return true if update is successful, false otherwise
     */
    public boolean updateApplication(Application application) {
        return applicationStorage.update(application);
    }
    
    /**
     * Withdraw an application
     * @param application the application to withdraw
     * @return true if withdrawal is successful, false otherwise
     */
    public boolean withdrawApplication(Application application) {
        // Check if application can be withdrawn
        if (!application.canWithdraw()) {
            return false;
        }
        
        // In a real system, you would create a withdrawal request
        // For this implementation, we'll mark it as unsuccessful
        application.setStatus(ApplicationStatus.UNSUCCESSFUL);
        
        // Update application
        return updateApplication(application);
    }
    
    /**
     * Get withdrawal requests for a project
     * This is a placeholder method for demonstration
     * In a real system, this would interact with a WithdrawalControl
     * @param project the project
     * @return list of withdrawal requests (applications with withdrawal requests)
     */
    public List<Application> getWithdrawalRequests(Project project) {
        // This is a placeholder that always returns an empty list
        // In a real implementation, this would interact with the WithdrawalControl
        return new ArrayList<>();
    }
    
    /**
     * Process an application (approve or reject)
     * @param application the application to process
     * @param approve true to approve, false to reject
     * @return true if processing is successful, false otherwise
     */
    public boolean processApplication(Application application, boolean approve) {
        if (approve) {
            // Check flat availability
            FlatType requestedType = determineRequestedFlatType(application);
            Project project = application.getProject();
            
            if (project.getAvailableUnitsByType(requestedType) <= 0) {
                return false; // No available units
            }
            
            // Approve the application
            application.setStatus(ApplicationStatus.SUCCESSFUL);
            
            // Decrement available units
            project.decrementAvailableUnits(requestedType);
            projectControl.updateProject(project);
        } else {
            // Reject the application
            application.setStatus(ApplicationStatus.UNSUCCESSFUL);
        }
        
        // Update application
        return updateApplication(application);
    }
    
    /**
     * Determine which flat type an applicant is requesting
     * @param application the application
     * @return the requested flat type
     */
    private FlatType determineRequestedFlatType(Application application) {
        Applicant applicant = application.getApplicant();
        
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            // Singles can only apply for 2-Room
            return FlatType.TWO_ROOM;
        } else {
            // For married couples, check which type they want
            // This would typically be stored in the application
            // For now, we'll use a placeholder logic
            Project project = application.getProject();
            return project.getAvailableUnitsByType(FlatType.THREE_ROOM) > 0 ?
                   FlatType.THREE_ROOM : FlatType.TWO_ROOM;
        }
    }
    
    /**
     * Book a flat for an application
     * @param application the application
     * @param flatType the type of flat to book
     * @return the booked flat, or null if booking failed
     */
    public Flat bookFlat(Application application, FlatType flatType) {
        // Check if application is successful and can be booked
        if (!application.canBook()) {
            return null;
        }
        
        // Check flat availability
        Project project = application.getProject();
        if (project.getAvailableUnitsByType(flatType) <= 0) {
            return null; // No available units
        }
        
        // Generate a flat ID
        String flatID = generateFlatID(project, flatType);
        
        // Create a new flat
        Flat flat = new Flat(flatID, project, flatType);
        
        // Update application
        application.setStatus(ApplicationStatus.BOOKED);
        application.setBookedFlat(flat);
        
        // Decrement available units
        project.decrementAvailableUnits(flatType);
        projectControl.updateProject(project);
        
        // Update application
        updateApplication(application);
        
        return flat;
    }
    
    /**
     * Generate a flat ID
     * @param project the project
     * @param flatType the flat type
     * @return a unique flat ID
     */
    private String generateFlatID(Project project, FlatType flatType) {
        // Simple algorithm: project ID + flat type prefix + timestamp
        String typePrefix = flatType == FlatType.TWO_ROOM ? "2R" : "3R";
        return "F-" + project.getProjectID() + "-" + typePrefix + "-" + System.currentTimeMillis() % 10000;
    }
}