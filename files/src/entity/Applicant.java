package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Applicant in the BTO system.
 * Extends the User class with additional applicant-specific functionality.
 */
public class Applicant extends User {
    private final List<Application> applications;
    private Flat bookedFlat;
    
    /**
     * Constructor with MaritalStatus enum
     */
    public Applicant(String name, String NRIC, String password, int age, MaritalStatus maritalStatus, String role) {
        super(name, NRIC, password, age, maritalStatus, role);
        this.applications = new ArrayList<>();
        this.bookedFlat = null;
    }
    
    /**
     * Constructor with marital status as String
     */
    public Applicant(String name, String NRIC, String password, int age, String maritalStatus, String role) {
        super(name, NRIC, password, age, maritalStatus, role);
        this.applications = new ArrayList<>();
        this.bookedFlat = null;
    }
    
    /**
     * Apply for a BTO project
     * @param project the project to apply for
     * @return true if application was successful, false otherwise
     */
    public boolean applyForProject(Project project) {
        // Check if applicant is already applying for another project
        if (!applications.isEmpty()) {
            for (Application app : applications) {
                if (app.getStatus() == ApplicationStatus.PENDING || 
                    app.getStatus() == ApplicationStatus.SUCCESSFUL) {
                    return false; // Cannot apply for multiple projects
                }
            }
        }
        
        // Check age and marital status eligibility
        if (!isEligibleForProject(project)) {
            return false;
        }
        
        // Create new application
        Application newApplication = new Application(this, project);
        applications.add(newApplication);
        return true;
    }
    
    /**
     * Check if applicant is eligible for a project based on age and marital status
     * @param project the project to check eligibility for
     * @return true if eligible, false otherwise
     */
    public boolean isEligibleForProject(Project project) {
        // Singles, 35 years old and above, can ONLY apply for 2-Room
        if (getMaritalStatus() == MaritalStatus.SINGLE) {
            if (getAge() < 35) {
                return false; // Singles must be at least 35
            }
            
            // Check if the project has 2-Room flats
            return project.hasFlatType(FlatType.TWO_ROOM);
        } 
        // Married, 21 years old and above, can apply for any flat type
        else if (getMaritalStatus() == MaritalStatus.MARRIED) {
            return getAge() >= 21; // Married applicants must be at least 21
        }
        
        return false;
    }
    
    /**
     * View all applications made by this applicant
     * @return list of applications
     */
    public List<Application> getApplications() {
        return new ArrayList<>(applications);
    }
    
    /**
     * Get current booked flat
     * @return the booked flat or null if none
     */
    public Flat getBookedFlat() {
        return bookedFlat;
    }
    
    /**
     * Set booked flat 
     * @param flat the flat that has been booked
     */
    public void setBookedFlat(Flat flat) {
        this.bookedFlat = flat;
    }
    
    /**
     * Add an application to this applicant's list
     * @param application the application to add
     */
    public void addApplication(Application application) {
        if (!applications.contains(application)) {
            applications.add(application);
        }
    }
    
    /**
     * Check if applicant has any pending or successful applications
     * @return true if has active applications, false otherwise
     */
    public boolean hasActiveApplications() {
        for (Application app : applications) {
            if (app.getStatus() == ApplicationStatus.PENDING || 
                app.getStatus() == ApplicationStatus.SUCCESSFUL) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasSuccessfulApplication() {
        for (Application app : applications) {
            if (app.getStatus() == ApplicationStatus.SUCCESSFUL) {
                return true;
            }
        }
        return false;
    }

    /**
     * Request withdrawal of an application
     * @param application the application to withdraw
     * @return true if request was successful, false otherwise
     */
    public boolean requestWithdrawal(Application application) {
        if (applications.contains(application) && application.canWithdraw()) {
            // Set status to indicate withdrawal request
            // (In a real system, you might want a specific status for this)
            return true;
        }
        return false;
    }
    
}