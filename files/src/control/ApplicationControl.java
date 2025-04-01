package control;

import entity.*;
import java.io.*;
import java.util.*;

/**
 * Controls operations related to Applications in the BTO system.
 */
public class ApplicationControl {
    private static final String APPLICATIONS_FILE = "files/resources/ApplicationList.csv";
    private List<Application> applications;
    
    /**
     * Constructor initializes the applications list from storage
     */
    public ApplicationControl() {
        this.applications = loadApplications();
    }
    
    /**
     * Submit a new application
     * @param applicant the applicant
     * @param project the project to apply for
     * @return true if submission is successful, false otherwise
     */
    public boolean submitApplication(Applicant applicant, Project project) {
        // Check if applicant already has an active application
        if (applicant.hasActiveApplications()) {
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
        
        // Add to list
        applications.add(application);
        
        // Save to file
        return saveApplications();
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
        // For this implementation, we'll mark it as a special status
        // This would be tracked elsewhere
        
        return true;
    }
    
    /**
     * Get all applications in the system
     * @return list of all applications
     */
    public List<Application> getAllApplications() {
        return new ArrayList<>(applications);
    }
    
    /**
     * Get all applications for a project
     * @param project the project
     * @return list of all applications for the project
     */
    public List<Application> getAllApplications(Project project) {
        List<Application> projectApplications = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getProject().getProjectID().equals(project.getProjectID())) {
                projectApplications.add(app);
            }
        }
        
        return projectApplications;
    }
    
    /**
     * Get pending applications for a project
     * @param project the project
     * @return list of pending applications
     */
    public List<Application> getPendingApplications(Project project) {
        List<Application> pendingApplications = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getProject().getProjectID().equals(project.getProjectID()) && 
                app.getStatus() == ApplicationStatus.PENDING) {
                pendingApplications.add(app);
            }
        }
        
        return pendingApplications;
    }
    
    /**
     * Get booked applications for a project
     * @param project the project
     * @return list of booked applications
     */
    public List<Application> getBookedApplications(Project project) {
        List<Application> bookedApplications = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getProject().getProjectID().equals(project.getProjectID()) && 
                app.getStatus() == ApplicationStatus.BOOKED) {
                bookedApplications.add(app);
            }
        }
        
        return bookedApplications;
    }
    
    /**
     * Get withdrawal requests for a project
     * @param project the project
     * @return list of withdrawal requests
     */
    public List<Application> getWithdrawalRequests(Project project) {
        // In a real system, withdrawal requests would be tracked separately
        // For this implementation, we'll return an empty list
        return new ArrayList<>();
    }
    
    /**
     * Update an application
     * @param application the application to update
     * @return true if update is successful, false otherwise
     */
    public boolean updateApplication(Application application) {
        // Find and update application
        for (int i = 0; i < applications.size(); i++) {
            if (applications.get(i).getApplicationID().equals(application.getApplicationID())) {
                applications.set(i, application);
                return saveApplications();
            }
        }
        
        return false; // Application not found
    }
    
    /**
     * Get applications for an applicant
     * @param applicant the applicant
     * @return list of applications for the applicant
     */
    public List<Application> getApplicationsForApplicant(Applicant applicant) {
        List<Application> applicantApplications = new ArrayList<>();
        
        for (Application app : applications) {
            if (app.getApplicant().getNRIC().equals(applicant.getNRIC())) {
                applicantApplications.add(app);
            }
        }
        
        return applicantApplications;
    }
    
    /**
     * Get application by ID
     * @param applicationID the application ID
     * @return the application, or null if not found
     */
    public Application getApplicationByID(String applicationID) {
        for (Application app : applications) {
            if (app.getApplicationID().equals(applicationID)) {
                return app;
            }
        }
        
        return null;
    }
    
    /**
     * Find an application by applicant NRIC and project
     * @param nric the applicant's NRIC
     * @param project the project
     * @return the application, or null if not found
     */
    public Application findApplicationByNRICAndProject(String nric, Project project) {
        for (Application app : applications) {
            if (app.getApplicant().getNRIC().equals(nric) && 
                app.getProject().getProjectID().equals(project.getProjectID())) {
                return app;
            }
        }
        
        return null;
    }
    
    /**
     * Load applications from file
     * @return list of applications
     */
    private List<Application> loadApplications() {
        List<Application> loadedApplications = new ArrayList<>();
        
        try (Scanner fileScanner = new Scanner(new File(APPLICATIONS_FILE))) {
            // Skip header if exists
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] values = line.split(",");
                if (values.length < 7) continue; // Invalid line
                
                // Parse application data
                try {
                    String applicationID = values[0].trim();
                    String applicantNRIC = values[1].trim();
                    String projectID = values[2].trim();
                    String statusStr = values[3].trim();
                    long applicationDate = Long.parseLong(values[4].trim());
                    long statusUpdateDate = Long.parseLong(values[5].trim());
                    String bookedFlatID = values.length > 6 ? values[6].trim() : "";
                    
                    // Convert status string to enum
                    ApplicationStatus status = ApplicationStatus.valueOf(statusStr);
                    
                    // Find applicant and project (in a real system, these would come from repositories)
                    // For now, create placeholders
                    Applicant applicant = new Applicant(
                        "Applicant " + applicantNRIC, // Placeholder name
                        applicantNRIC,
                        "password",
                        30, // Placeholder age
                        "Single", // Placeholder marital status
                        "Applicant"
                    );
                    
                    Project project = new Project(
                        projectID,
                        "Project " + projectID, // Placeholder name
                        "Neighborhood", // Placeholder neighborhood
                        new HashMap<>(), // Placeholder units
                        new Date(), // Placeholder open date
                        new Date(), // Placeholder close date
                        null, // Placeholder manager
                        5 // Placeholder officer slots
                    );
                    
                    // Create application with parsed data
                    Application application = new Application(applicant, project);
                    
                    // Use reflection to set private fields (for demo purposes)
                    // In a real application, you would have proper constructors or methods
                    java.lang.reflect.Field idField = Application.class.getDeclaredField("applicationID");
                    idField.setAccessible(true);
                    idField.set(application, applicationID);
                    
                    java.lang.reflect.Field statusField = Application.class.getDeclaredField("status");
                    statusField.setAccessible(true);
                    statusField.set(application, status);
                    
                    java.lang.reflect.Field appDateField = Application.class.getDeclaredField("applicationDate");
                    appDateField.setAccessible(true);
                    appDateField.set(application, new Date(applicationDate));
                    
                    java.lang.reflect.Field updateDateField = Application.class.getDeclaredField("statusUpdateDate");
                    updateDateField.setAccessible(true);
                    updateDateField.set(application, new Date(statusUpdateDate));
                    
                    // Set booked flat if any
                    if (!bookedFlatID.isEmpty()) {
                        Flat flat = new Flat(bookedFlatID, project, FlatType.TWO_ROOM); // Placeholder flat type
                        application.setBookedFlat(flat);
                    }
                    
                    // Add to list
                    loadedApplications.add(application);
                    
                } catch (Exception e) {
                    System.err.println("Error parsing application data: " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Applications file not found. Starting with empty list.");
        }
        
        return loadedApplications;
    }
    
    /**
     * Save applications to file
     * @return true if successful, false otherwise
     */
    private boolean saveApplications() {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(APPLICATIONS_FILE))) {
                // Write header
                writer.println("ApplicationID,ApplicantNRIC,ProjectID,Status,ApplicationDate,StatusUpdateDate,BookedFlatID");
                
                // Write applications
                for (Application app : applications) {
                    writer.print(
                        app.getApplicationID() + "," +
                        app.getApplicant().getNRIC() + "," +
                        app.getProject().getProjectID() + "," +
                        app.getStatus() + "," +
                        app.getApplicationDate().getTime() + "," +
                        app.getStatusUpdateDate().getTime()
                    );
                    
                    // Add booked flat ID if any
                    if (app.getBookedFlat() != null) {
                        writer.print("," + app.getBookedFlat().getFlatID());
                    }
                    
                    writer.println();
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving applications: " + e.getMessage());
            return false;
        }
    }
}