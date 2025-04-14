package control;

import entity.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controls operations related to HDB Managers in the BTO system.
 * Demonstrates the Controller pattern in MVC architecture.
 * Applies Single Responsibility Principle by focusing only on manager-related operations.
 */
public class HDBManagerControl {
    private final ProjectControl projectControl;
    private final HDBOfficerControl officerControl;
    private final ApplicationControl applicationControl;
    
    /**
     * Constructor for HDBManagerControl
     */
    public HDBManagerControl() {
        this.projectControl = new ProjectControl();
        this.officerControl = new HDBOfficerControl();
        this.applicationControl = new ApplicationControl();
    }
    
    /**
     * Get officer registrations for a project
     * @param project the project
     * @return list of officer registrations
     */
    public List<Map<String, Object>> getOfficerRegistrations(Project project) {
        return officerControl.getOfficerRegistrationsForProject(project);
    }
    
    /**
     * Process an officer registration
     * @param manager the manager processing the registration
     * @param officer the officer to approve or reject
     * @param project the project
     * @param approve true to approve, false to reject
     * @return true if processing is successful, false otherwise
     */
    public boolean processOfficerRegistration(HDBManager manager, HDBOfficer officer, Project project, boolean approve) {
        // Check if manager is in charge of the project
        if (!isManagerInChargeOfProject(manager, project)) {
            return false;
        }
        
        // Check if there are available slots for approval
        if (approve && project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        // Process the registration
        return manager.processOfficerRegistration(officer, project, approve);
    }
    
    /**
     * Process applications for a project
     * @param manager the manager processing the applications
     * @param applications the applications to process
     * @param approvals map of application IDs to approval status
     * @return number of applications processed successfully
     */
    public int processApplications(HDBManager manager, List<Application> applications, Map<String, Boolean> approvals) {
        int processed = 0;
        
        for (Application app : applications) {
            // Check if manager is in charge of the project
            if (!isManagerInChargeOfProject(manager, app.getProject())) {
                continue;
            }
            
            // Check if application is pending
            if (app.getStatus() != ApplicationStatus.PENDING) {
                continue;
            }
            
            // Get approval status
            Boolean isApproved = approvals.get(app.getApplicationID());
            if (isApproved == null) {
                continue; // No decision for this application
            }
            
            // Process the application
            if (processApplication(app, isApproved)) {
                processed++;
            }
        }
        
        return processed;
    }
    
    /**
     * Process a single application
     * @param application the application to process
     * @param isApproved whether to approve or reject
     * @return true if successfully processed, false otherwise
     */
    private boolean processApplication(Application application, boolean isApproved) {
        if (isApproved) {
            // Approve application
            FlatType requestedType = determineRequestedFlatType(application);
            Project project = application.getProject();
            
            if (project.getAvailableUnitsByType(requestedType) > 0) {
                application.setStatus(ApplicationStatus.SUCCESSFUL);
                project.decrementAvailableUnits(requestedType);
                projectControl.updateProject(project);
            } else {
                // Not enough units, reject instead
                application.setStatus(ApplicationStatus.UNSUCCESSFUL);
            }
        } else {
            // Reject application
            application.setStatus(ApplicationStatus.UNSUCCESSFUL);
        }
        
        // Update application
        return applicationControl.updateApplication(application);
    }
    
    /**
     * Check if a manager is in charge of a project
     * @param manager the manager
     * @param project the project
     * @return true if manager is in charge, false otherwise
     */
    private boolean isManagerInChargeOfProject(HDBManager manager, Project project) {
        return project.getManagerInCharge().getManagerID().equals(manager.getManagerID());
    }
    
    /**
     * Determine which flat type an applicant is applying for
     * @param application the application
     * @return the requested flat type
     */
    private FlatType determineRequestedFlatType(Application application) {
        Applicant applicant = application.getApplicant();
        Project project = application.getProject();
        
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            // Singles can only apply for 2-Room
            return FlatType.TWO_ROOM;
        } else {
            // For married couples, check which type they're applying for
            // For now, assume they're applying for 3-Room if available
            return project.getAvailableUnitsByType(FlatType.THREE_ROOM) > 0 ? 
                   FlatType.THREE_ROOM : FlatType.TWO_ROOM;
        }
    }
    
    /**
     * Process a withdrawal request
     * @param manager the manager processing the request
     * @param application the application to withdraw
     * @param approve true to approve, false to reject
     * @return true if processing is successful, false otherwise
     */
    public boolean processWithdrawalRequest(HDBManager manager, Application application, boolean approve) {
        if (!isManagerInChargeOfProject(manager, application.getProject())) {
            return false;
        }
        
        return manager.processWithdrawalRequest(application, approve);
    }
    
    /**
     * Create a new project
     * @param manager the manager creating the project
     * @param details map of project details
     * @return the created project, or null if creation failed
     */
    public Project createProject(HDBManager manager, Map<String, Object> details) {
        // Check if manager is already managing a project in the same period
        Date openDate = (Date) details.get("openDate");
        Date closeDate = (Date) details.get("closeDate");
        
        if (manager.isManagingProject(openDate, closeDate)) {
            return null;
        }
        
        // Create the project
        boolean success = manager.createProject(details);
        if (success) {
            // Get the last project added to the manager's list
            List<Project> managedProjects = manager.getManagedProjects();
            if (!managedProjects.isEmpty()) {
                return managedProjects.get(managedProjects.size() - 1);
            }
        }
        
        return null;
    }
    
    /**
     * Edit an existing project
     * @param manager the manager editing the project
     * @param project the project to edit
     * @param details map of updated details
     * @return true if edit was successful, false otherwise
     */
    public boolean editProject(HDBManager manager, Project project, Map<String, Object> details) {
        // Check if manager is in charge of the project
        if (!isManagerInChargeOfProject(manager, project)) {
            return false;
        }
        
        // Edit the project
        return manager.editProject(project, details);
    }
    
    /**
     * Delete a project
     * @param manager the manager deleting the project
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProject(HDBManager manager, Project project) {
        // Check if manager is in charge of the project
        if (!isManagerInChargeOfProject(manager, project)) {
            return false;
        }
        
        // Delete the project
        return manager.deleteProject(project);
    }
    
    /**
     * Toggle project visibility
     * @param manager the manager toggling visibility
     * @param project the project
     * @param visible the new visibility
     * @return true if toggle was successful, false otherwise
     */
    public boolean toggleProjectVisibility(HDBManager manager, Project project, boolean visible) {
        // Check if manager is in charge of the project
        if (!project.getManagerInCharge().getManagerID().equals(manager.getManagerID())) {
            return false;
        }
        
        // Toggle visibility
        return manager.toggleVisibility(project, visible);
    }
    
    /**
     * Get projects created by this manager
     * @param manager the manager
     * @return list of projects
     */
    public List<Project> getProjectsByManager(HDBManager manager) {
        List<Project> allProjects = projectControl.getAllProjects();
        
        // Use streams for cleaner filtering
        return allProjects.stream()
            .filter(p -> p.getManagerInCharge().getNRIC().equals(manager.getNRIC()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get all projects in the system
     * @return list of projects
     */
    public List<Project> getAllProjects() {
        return projectControl.getAllProjects();
    }
    
    /**
     * Generate a report based on applications
     * @param applications the applications to include in the report
     * @param filters the filters to apply
     * @return the generated report
     */
    public Report generateReport(List<Application> applications, Map<String, Object> filters) {
        // Apply filters if provided
        List<Application> filteredApps = applications;
        if (filters != null && !filters.isEmpty()) {
            filteredApps = applyFilters(applications, filters);
        }
        
        // Create a report
        Report report = new Report();
        report.setReportID("RPT" + System.currentTimeMillis() % 10000);
        report.setApplications(filteredApps);
        report.setGenerationDate(new Date());
        report.setCriteria(filters != null ? new HashMap<>(filters) : new HashMap<>());
        
        return report;
    }
    
    /**
     * Apply filters to applications
     * @param applications the applications to filter
     * @param filters the filters to apply
     * @return filtered list of applications
     */
    private List<Application> applyFilters(List<Application> applications, Map<String, Object> filters) {
        List<Application> result = new ArrayList<>(applications);
        
        // Apply marital status filter
        if (filters.containsKey("maritalStatus")) {
            String maritalStatusStr = (String) filters.get("maritalStatus");
            MaritalStatus status = MaritalStatus.fromString(maritalStatusStr);
            
            if (status != null) {
                result.removeIf(app -> app.getApplicant().getMaritalStatus() != status);
            }
        }
        
        // Apply age range filter
        if (filters.containsKey("minAge")) {
            int minAge = (int) filters.get("minAge");
            result.removeIf(app -> app.getApplicant().getAge() < minAge);
        }
        
        if (filters.containsKey("maxAge")) {
            int maxAge = (int) filters.get("maxAge");
            result.removeIf(app -> app.getApplicant().getAge() > maxAge);
        }
        
        // Apply flat type filter
        if (filters.containsKey("flatType")) {
            String flatTypeStr = (String) filters.get("flatType");
            FlatType flatType = null;
            
            if (flatTypeStr.equalsIgnoreCase("2-Room")) {
                flatType = FlatType.TWO_ROOM;
            } else if (flatTypeStr.equalsIgnoreCase("3-Room")) {
                flatType = FlatType.THREE_ROOM;
            }
            
            if (flatType != null) {
                FlatType finalFlatType = flatType;
                result.removeIf(app -> {
                    Flat bookedFlat = app.getBookedFlat();
                    return bookedFlat == null || bookedFlat.getType() != finalFlatType;
                });
            }
        }
        
        return result;
    }
}