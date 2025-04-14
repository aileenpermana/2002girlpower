package entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents an HDB Manager in the BTO system.
 * Extends the User class with manager-specific functionality.
 * Demonstrates Single Responsibility Principle by focusing only on manager operations.
 * Implements ProjectManagement interface (Interface Segregation Principle)
 */
public class HDBManager extends User implements ProjectManagement {
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
     * Implements ProjectManagement interface method
     * @param details map containing project details
     * @return the created project or null if creation fails
     */
    @Override
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
        
        // Generate project ID
        String projectID = generateProjectID(projectName);
        
        // Create the project
        Project project = new Project(
            projectID,
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
        // Simple algorithm: first 3 characters of project name + manager ID part + timestamp
        String prefix = projectName.substring(0, Math.min(3, projectName.length())).toUpperCase();
        String managerPart = this.managerID.substring(Math.max(0, this.managerID.length() - 4));
        return prefix + managerPart + System.currentTimeMillis() % 10000;
    }
    
    /**
     * Edit an existing project
     * Implements ProjectManagement interface method
     * @param project the project to edit
     * @param details map containing updated details
     * @return true if edit was successful, false otherwise
     */
    @Override
    public boolean editProject(Project project, Map<String, Object> details) {
        // Check if manager is managing this project
        if (!managingProjects.contains(project)) {
            return false;
        }
        
        // Update project details using Optional for cleaner code
        Optional.ofNullable((String) details.get("projectName"))
            .ifPresent(project::setProjectName);
        
        Optional.ofNullable((String) details.get("neighborhood"))
            .ifPresent(project::setNeighborhood);
        
        Optional.ofNullable((Date) details.get("openDate"))
            .ifPresent(project::setApplicationOpenDate);
        
        Optional.ofNullable((Date) details.get("closeDate"))
            .ifPresent(project::setApplicationCloseDate);
        
        // Update flat units if provided
        if (details.containsKey("twoRoomUnits")) {
            int twoRoomUnits = (int) details.get("twoRoomUnits");
            project.setNumberOfUnitsByType(FlatType.TWO_ROOM, twoRoomUnits);
        }
        
        if (details.containsKey("threeRoomUnits")) {
            int threeRoomUnits = (int) details.get("threeRoomUnits");
            project.setNumberOfUnitsByType(FlatType.THREE_ROOM, threeRoomUnits);
        }
        
        if (details.containsKey("officerSlots")) {
            int officerSlots = (int) details.get("officerSlots");
            project.setOfficerSlots(officerSlots);
        }
        
        return true;
    }
    
    /**
     * Delete a project
     * Implements ProjectManagement interface method
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean deleteProject(Project project) {
        // Check if manager is managing this project
        if (!managingProjects.contains(project)) {
            return false;
        }
        
        // Remove from managing projects
        managingProjects.remove(project);
        
        return true;
    }
    
    /**
     * Toggle the visibility of a project
     * Implements ProjectManagement interface method
     * @param project the project to toggle
     * @param visible whether the project should be visible
     * @return true if successful, false otherwise
     */
    @Override
    public boolean toggleVisibility(Project project, boolean visible) {
        // Check if manager is managing this project
        if (!managingProjects.contains(project)) {
            return false;
        }
        
        project.setVisible(visible);
        
        return true;
    }
    
    /**
     * Get all projects managed by this manager
     * @return list of projects
     */
    public List<Project> getManagedProjects() {
        return new ArrayList<>(managingProjects);
    }
    
    /**
     * Process an officer's registration for a project
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
        
        // Check if there are available slots for approval
        if (approve && project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        if (approve) {
            // Add officer to project
            boolean success = project.addOfficer(officer);
            if (success) {
                officer.addHandlingProject(project);
                return true;
            }
        } else {
            // For rejection, no changes needed to the project
            return true;
        }
        
        return false;
    }
    
    /**
     * Process a withdrawal request
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
                // Determine the flat type from the application
                FlatType flatType = determineRequestedFlatType(application);
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
    
    /**
     * Determine the flat type requested by an applicant
     * @param application the application
     * @return the requested flat type
     */
    private FlatType determineRequestedFlatType(Application application) {
        Applicant applicant = application.getApplicant();
        
        // If the application already has a booked flat, use its type
        if (application.getBookedFlat() != null) {
            return application.getBookedFlat().getType();
        }
        
        // Singles can only apply for 2-Room
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            return FlatType.TWO_ROOM;
        } 
        
        // For married couples, use 3-Room if available in the project
        Project project = application.getProject();
        return project.hasFlatType(FlatType.THREE_ROOM) ? FlatType.THREE_ROOM : FlatType.TWO_ROOM;
    }
    
    /**
     * Add a project to this manager's list
     * Used when loading projects from storage
     * @param project the project to add
     */
    public void addProject(Project project) {
        if (!managingProjects.contains(project)) {
            managingProjects.add(project);
        }
    }
    
    /**
     * Generate a report based on application data and filters
     * @param applications list of applications
     * @param filters filtering criteria
     * @return a Report object
     */
    public Report generateReport(List<Application> applications, Map<String, Object> filters) {
        // Filter applications according to criteria
        List<Application> filteredApps = applyFilters(applications, filters);
        
        // Create and configure the report
        Report report = new Report();
        report.setReportID("RPT-" + getManagerID() + "-" + System.currentTimeMillis() % 10000);
        report.setApplications(filteredApps);
        report.setGenerationDate(new Date());
        report.setCriteria(new HashMap<>(filters));
        
        return report;
    }
    
    /**
     * Apply filters to a list of applications
     * @param applications the applications to filter
     * @param filters the filtering criteria
     * @return filtered list of applications
     */
    private List<Application> applyFilters(List<Application> applications, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>(applications);
        }
        
        List<Application> result = new ArrayList<>(applications);
        
        // Filter by marital status
        if (filters.containsKey("maritalStatus")) {
            String maritalStatusStr = (String) filters.get("maritalStatus");
            MaritalStatus status = MaritalStatus.fromString(maritalStatusStr);
            
            if (status != null) {
                result.removeIf(app -> app.getApplicant().getMaritalStatus() != status);
            }
        }
        
        // Filter by age range
        if (filters.containsKey("minAge")) {
            int minAge = (int) filters.get("minAge");
            result.removeIf(app -> app.getApplicant().getAge() < minAge);
        }
        
        if (filters.containsKey("maxAge")) {
            int maxAge = (int) filters.get("maxAge");
            result.removeIf(app -> app.getApplicant().getAge() > maxAge);
        }
        
        // Filter by application status
        if (filters.containsKey("status")) {
            String statusStr = (String) filters.get("status");
            try {
                ApplicationStatus status = ApplicationStatus.valueOf(statusStr.toUpperCase());
                result.removeIf(app -> app.getStatus() != status);
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore this filter
            }
        }
        
        // Filter by flat type (for booked applications)
        if (filters.containsKey("flatType")) {
            String flatTypeStr = (String) filters.get("flatType");
            FlatType flatType = FlatType.fromString(flatTypeStr);
            
            if (flatType != null) {
                result.removeIf(app -> {
                    Flat bookedFlat = app.getBookedFlat();
                    return bookedFlat == null || bookedFlat.getType() != flatType;
                });
            }
        }
        
        return result;
    }
    
    @Override
    public String toString() {
        return "HDBManager{" +
                "managerID='" + managerID + '\'' +
                ", name='" + getName() + '\'' +
                ", NRIC='" + getNRIC() + '\'' +
                ", managingProjects=" + managingProjects.size() +
                '}';
    }
}