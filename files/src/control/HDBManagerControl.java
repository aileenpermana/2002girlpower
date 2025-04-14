package control;

import entity.*;
import java.util.*;
import utils.DataStorage;
import utils.ProjectDataManager;

/**
 * Enhanced HDBManagerControl that follows SOLID principles.
 * - Single Responsibility: Focuses only on HDB Manager operations
 * - Open/Closed: Extends functionality through strategy pattern for filtering
 * - Liskov Substitution: Uses interfaces correctly
 * - Interface Segregation: Uses focused interfaces
 * - Dependency Inversion: Depends on abstractions, not concretions
 */
public class HDBManagerControl {
    // Dependencies are defined as interfaces, not concrete classes (Dependency Inversion)
    private final DataStorage<Project> projectStorage;
    private final ApplicationFilter applicationFilter;
    private final ProjectFilter projectFilter;
    
    /**
     * Standard constructor that uses default implementations
     */
    public HDBManagerControl() {
        this.projectStorage = ProjectDataManager.getInstance();
        this.applicationFilter = new DefaultApplicationFilter();
        this.projectFilter = new DefaultProjectFilter();
    }
    
    /**
     * Constructor with dependency injection for easier testing and flexibility
     * Demonstrates Dependency Inversion Principle
     */
    public HDBManagerControl(DataStorage<Project> projectStorage, 
                            ApplicationFilter applicationFilter,
                            ProjectFilter projectFilter) {
        this.projectStorage = projectStorage;
        this.applicationFilter = applicationFilter;
        this.projectFilter = projectFilter;
    }
    
    /**
     * Create a project and save it to storage
     * @param manager the manager creating the project
     * @param details project details
     * @return the created project or null if creation failed
     */
    public Project createProject(HDBManager manager, Map<String, Object> details) {
        // Create the project using the manager (which has project creation logic)
        Project newProject = manager.createProject(details);
        
        // Save to storage if creation was successful
        if (newProject != null) {
            boolean saved = projectStorage.save(newProject);
            if (!saved) {
                // If saving failed, remove from manager's list
                manager.deleteProject(newProject);
                return null;
            }
        }
        
        return newProject;
    }
    
    /**
     * Edit a project and update it in storage
     * @param manager the manager editing the project
     * @param project the project to edit
     * @param details updated project details
     * @return true if edit was successful, false otherwise
     */
    public boolean editProject(HDBManager manager, Project project, Map<String, Object> details) {
        // Edit the project using the manager
        boolean edited = manager.editProject(project, details);
        
        // Update in storage if edit was successful
        if (edited) {
            return projectStorage.update(project);
        }
        
        return false;
    }
    
    /**
     * Delete a project from storage
     * @param manager the manager deleting the project
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProject(HDBManager manager, Project project) {
        // Delete the project using the manager
        boolean deleted = manager.deleteProject(project);
        
        // Remove from storage if deletion was successful
        if (deleted) {
            return projectStorage.delete(project);
        }
        
        return false;
    }
    
    /**
     * Toggle project visibility
     * @param manager the manager toggling visibility
     * @param project the project
     * @param visible the new visibility
     * @return true if toggle was successful, false otherwise
     */
    public boolean toggleProjectVisibility(HDBManager manager, Project project, boolean visible) {
        // Toggle visibility using the manager
        boolean toggled = manager.toggleVisibility(project, visible);
        
        // Update in storage if toggle was successful
        if (toggled) {
            return projectStorage.update(project);
        }
        
        return false;
    }
    
    /**
     * Get all projects in the system
     * @return list of all projects
     */
    public List<Project> getAllProjects() {
        return projectStorage.readAll();
    }
    
    /**
     * Get projects managed by a specific manager
     * @param manager the manager
     * @return list of managed projects
     */
    public List<Project> getProjectsByManager(HDBManager manager) {
        // Get projects from manager object (in-memory)
        return manager.getManagedProjects();
    }
    
    /**
     * Get filtered projects based on criteria
     * @param projects list of projects to filter
     * @param filters filtering criteria
     * @return filtered list of projects
     */
    public List<Project> getFilteredProjects(List<Project> projects, Map<String, Object> filters) {
        return projectFilter.applyFilters(projects, filters);
    }
    
    /**
     * Process an officer registration
     * @param manager the manager processing the registration
     * @param officer the officer
     * @param project the project
     * @param approve true to approve, false to reject
     * @return true if processing was successful, false otherwise
     */
    public boolean processOfficerRegistration(HDBManager manager, HDBOfficer officer, Project project, boolean approve) {
        // Process the registration using the manager
        boolean processed = manager.processOfficerRegistration(officer, project, approve);
        
        // Update project in storage if processing was successful
        if (processed) {
            return projectStorage.update(project);
        }
        
        return false;
    }
    
    /**
     * Process a withdrawal request
     * @param manager the manager processing the request
     * @param application the application
     * @param approve true to approve, false to reject
     * @return true if processing was successful, false otherwise
     */
    public boolean processWithdrawalRequest(HDBManager manager, Application application, boolean approve) {
        // Process the withdrawal using the manager
        boolean processed = manager.processWithdrawalRequest(application, approve);
        
        // Update project in storage if processing was successful and approved
        if (processed && approve) {
            return projectStorage.update(application.getProject());
        }
        
        return processed;
    }
    
    /**
     * Generate a report
     * @param manager the manager generating the report
     * @param applications list of applications
     * @param filters filtering criteria
     * @return the generated report
     */
    public Report generateReport(HDBManager manager, List<Application> applications, Map<String, Object> filters) {
        // Generate the report using the manager
        return manager.generateReport(applications, filters);
    }
    
    /**
     * Get filtered applications based on criteria
     * @param applications list of applications to filter
     * @param filters filtering criteria
     * @return filtered list of applications
     */
    public List<Application> getFilteredApplications(List<Application> applications, Map<String, Object> filters) {
        return applicationFilter.applyFilters(applications, filters);
    }
    
    /**
     * Interface for filtering applications (Strategy Pattern)
     * Allows for different filtering implementations
     */
    public interface ApplicationFilter {
        List<Application> applyFilters(List<Application> applications, Map<String, Object> filters);
    }
    
    /**
     * Interface for filtering projects (Strategy Pattern)
     * Allows for different filtering implementations
     */
    public interface ProjectFilter {
        List<Project> applyFilters(List<Project> projects, Map<String, Object> filters);
    }
    
    /**
     * Default implementation of ApplicationFilter
     */
    private static class DefaultApplicationFilter implements ApplicationFilter {
        @Override
        public List<Application> applyFilters(List<Application> applications, Map<String, Object> filters) {
            if (filters == null || filters.isEmpty()) {
                return new ArrayList<>(applications);
            }
            
            List<Application> filtered = new ArrayList<>(applications);
            
            // Apply marital status filter
            if (filters.containsKey("maritalStatus")) {
                String maritalStatusStr = (String) filters.get("maritalStatus");
                MaritalStatus status = MaritalStatus.fromString(maritalStatusStr);
                
                if (status != null) {
                    filtered.removeIf(app -> app.getApplicant().getMaritalStatus() != status);
                }
            }
            
            // Apply age filters
            if (filters.containsKey("minAge")) {
                int minAge = (int) filters.get("minAge");
                filtered.removeIf(app -> app.getApplicant().getAge() < minAge);
            }
            
            if (filters.containsKey("maxAge")) {
                int maxAge = (int) filters.get("maxAge");
                filtered.removeIf(app -> app.getApplicant().getAge() > maxAge);
            }
            
            // Apply status filter
            if (filters.containsKey("status")) {
                String statusStr = (String) filters.get("status");
                try {
                    ApplicationStatus status = ApplicationStatus.valueOf(statusStr.toUpperCase());
                    filtered.removeIf(app -> app.getStatus() != status);
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore this filter
                }
            }
            
            return filtered;
        }
    }
    
    /**
     * Default implementation of ProjectFilter
     */
    private static class DefaultProjectFilter implements ProjectFilter {
        @Override
        public List<Project> applyFilters(List<Project> projects, Map<String, Object> filters) {
            if (filters == null || filters.isEmpty()) {
                return new ArrayList<>(projects);
            }
            
            List<Project> filtered = new ArrayList<>(projects);
            
            // Apply neighborhood filter
            if (filters.containsKey("neighborhood")) {
                String neighborhood = (String) filters.get("neighborhood");
                filtered.removeIf(p -> !p.getNeighborhood().equalsIgnoreCase(neighborhood));
            }
            
            // Apply flat type filter
            if (filters.containsKey("flatType")) {
                String flatTypeStr = (String) filters.get("flatType");
                FlatType flatType = FlatType.fromString(flatTypeStr);
                
                if (flatType != null) {
                    filtered.removeIf(p -> !p.hasFlatType(flatType));
                }
            }
            
            // Apply visibility filter
            if (filters.containsKey("visible")) {
                boolean visible = (boolean) filters.get("visible");
                filtered.removeIf(p -> p.isVisible() != visible);
            }
            
            // Apply manager filter
            if (filters.containsKey("managerId")) {
                String managerId = (String) filters.get("managerId");
                filtered.removeIf(p -> !p.getManagerInCharge().getManagerID().equals(managerId));
            }
            
            return filtered;
        }
    }
}