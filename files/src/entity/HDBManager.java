package entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an HDB Manager in the BTO system.
 * Extends the User class with manager-specific functionality.
 */
public class HDBManager extends User {
    private String managerID;
    private List<Project> managingProjects;
    
    /**
     * Constructor with MaritalStatus enum
     */
    public HDBManager(String name, String NRIC, String password, int age, MaritalStatus maritalStatus, String role) {
        super(name, NRIC, password, age, maritalStatus, role);
        this.managerID = "HM" + NRIC.substring(1, 8);
        this.managingProjects = new ArrayList<>();
    }
    
    /**
     * Constructor with marital status as String
     */
    public HDBManager(String name, String NRIC, String password, int age, String maritalStatus, String role) {
        super(name, NRIC, password, age, maritalStatus, role);
        this.managerID = "HM" + NRIC.substring(1, 8);
        this.managingProjects = new ArrayList<>();
    }
    
    /**
     * Get the manager's ID
     * @return manager ID
     */
    public String getManagerID() {
        return managerID;
    }
    
    /**
     * Check if manager is managing any project in the given date range
     * @param start start date
     * @param end end date
     * @return true if managing a project in the date range, false otherwise
     */
    public boolean isManagingProject(Date start, Date end) {
        for (Project project : managingProjects) {
            Date projectStart = project.getApplicationOpenDate();
            Date projectEnd = project.getApplicationCloseDate();
            
            // Check if date ranges overlap
            if (!(end.before(projectStart) || start.after(projectEnd))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Create a new BTO project
     * @param details map containing project details
     * @return the created project, or null if creation failed
     */
    public Project createProject(Map<String, Object> details) {
        // Extract project details
        String projectName = (String) details.get("projectName");
        String neighborhood = (String) details.get("neighborhood");
        Date openDate = (Date) details.get("openDate");
        Date closeDate = (Date) details.get("closeDate");
        int officerSlots = (int) details.get("officerSlots");
        
        // Check if manager is already managing a project in the same period
        if (isManagingProject(openDate, closeDate)) {
            return null;
        }
        
        // Create flat type units map
        Map<FlatType, Integer> totalUnits = new HashMap<>();
        totalUnits.put(FlatType.TWO_ROOM, (int) details.get("twoRoomUnits"));
        totalUnits.put(FlatType.THREE_ROOM, (int) details.get("threeRoomUnits"));
        
        // Create the project
        Project project = new Project(
            generateProjectID(projectName),
            projectName,
            neighborhood,
            totalUnits,
            openDate,
            closeDate,
            this,
            officerSlots
        );
        
        // Add to managing projects
        managingProjects.add(project);
        
        return project;
    }
    
    /**
     * Generate a project ID based on the project name
     * @param projectName the name of the project
     * @return a generated project ID
     */
    private String generateProjectID(String projectName) {
        // Simple algorithm: first 3 characters of project name + timestamp
        String prefix = projectName.substring(0, Math.min(3, projectName.length())).toUpperCase();
        return prefix + System.currentTimeMillis() % 10000;
    }
    
    /**
     * Edit an existing project
     * @param project the project to edit
     * @param details map containing updated details
     * @return true if edit was successful, false otherwise
     */
    public boolean editProject(Project project, Map<String, Object> details) {
        // Check if manager is managing this project
        if (!managingProjects.contains(project)) {
            return false;
        }
        
        // Update project details
        if (details.containsKey("projectName")) {
            project.setProjectName((String) details.get("projectName"));
        }
        
        if (details.containsKey("neighborhood")) {
            project.setNeighborhood((String) details.get("neighborhood"));
        }
        
        if (details.containsKey("openDate")) {
            project.setApplicationOpenDate((Date) details.get("openDate"));
        }
        
        if (details.containsKey("closeDate")) {
            project.setApplicationCloseDate((Date) details.get("closeDate"));
        }
        
        if (details.containsKey("twoRoomUnits")) {
            project.setNumberOfUnitsByType(FlatType.TWO_ROOM, (int) details.get("twoRoomUnits"));
        }
        
        if (details.containsKey("threeRoomUnits")) {
            project.setNumberOfUnitsByType(FlatType.THREE_ROOM, (int) details.get("threeRoomUnits"));
        }
        
        if (details.containsKey("officerSlots")) {
            project.setOfficerSlots((int) details.get("officerSlots"));
        }
        
        return true;
    }
    
    /**
     * Delete a project
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProject(Project project) {
        // Check if manager is managing this project
        if (!managingProjects.contains(project)) {
            return false;
        }
        
        // Remove from managing projects
        managingProjects.remove(project);
        
        // In a real system, the project would be removed from the database
        
        return true;
    }
    
    /**
     * Toggle the visibility of a project
     * @param project the project to toggle
     * @param visible whether the project should be visible
     */
    public void toggleVisibility(Project project, boolean visible) {
        // Check if manager is managing this project
        if (managingProjects.contains(project)) {
            project.setVisible(visible);
        }
    }
    
    /**
     * Get all projects managed by this manager
     * @return list of projects
     */
    public List<Project> getManagedProjects() {
        return new ArrayList<>(managingProjects);
    }
    
    /**
     * Approve or reject an HDB Officer's registration for a project
     * @param officer the officer
     * @param project the project
     * @param approve true to approve, false to reject
     * @return true if operation was successful, false otherwise
     */
    public boolean processOfficerRegistration(HDBOfficer officer, Project project, boolean approve) {
        // Check if manager is managing this project
        if (!managingProjects.contains(project)) {
            return false;
        }
        
        // Check if there are available slots
        if (approve && project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        if (approve) {
            // Add officer to project
            project.addOfficer(officer);
            officer.addHandlingProject(project);
            
            // Update available slots
            project.decrementOfficerSlots();
        }
        
        return true;
    }
    
    /**
     * Process applications for a project
     * @param applications list of applications to process
     * @return number of applications processed
     */
    public int processApplications(List<Application> applications) {
        int processed = 0;
        
        for (Application app : applications) {
            Project project = app.getProject();
            
            // Check if manager is managing this project
            if (!managingProjects.contains(project)) {
                continue;
            }
            
            // Check if application is pending
            if (app.getStatus() != ApplicationStatus.PENDING) {
                continue;
            }
            
            // Check if there are available units of the requested type
            // In a real system, you would determine the flat type requested in the application
            FlatType requestedType = FlatType.TWO_ROOM; // Placeholder
            
            if (project.getAvailableUnitsByType(requestedType) > 0) {
                // Approve the application
                app.setStatus(ApplicationStatus.SUCCESSFUL);
                
                // Decrement available units
                project.decrementAvailableUnits(requestedType);
            } else {
                // Reject the application
                app.setStatus(ApplicationStatus.UNSUCCESSFUL);
            }
            
            processed++;
        }
        
        return processed;
    }
    
    /**
     * Approve or reject a withdrawal request
     * @param application the application to withdraw
     * @param approve true to approve, false to reject
     * @return true if operation was successful, false otherwise
     */
    public boolean processWithdrawalRequest(Application application, boolean approve) {
        Project project = application.getProject();
        
        // Check if manager is managing this project
        if (!managingProjects.contains(project)) {
            return false;
        }
        
        if (approve) {
            // Check current status
            ApplicationStatus currentStatus = application.getStatus();
            
            // Reset the application status to allow new applications
            application.setStatus(ApplicationStatus.UNSUCCESSFUL);
            
            // If the application was successful or booked, increment available units
            if (currentStatus == ApplicationStatus.SUCCESSFUL || currentStatus == ApplicationStatus.BOOKED) {
                // In a real system, you would determine the flat type from the application
                FlatType flatType = FlatType.TWO_ROOM; // Placeholder
                project.incrementAvailableUnits(flatType);
                
                // If flat was booked, free it
                if (currentStatus == ApplicationStatus.BOOKED) {
                    Flat bookedFlat = application.getBookedFlat();
                    if (bookedFlat != null) {
                        bookedFlat.setBookedByApplication(null);
                        application.setBookedFlat(null);
                    }
                }
            }
        }
        
        return true;
    }
}