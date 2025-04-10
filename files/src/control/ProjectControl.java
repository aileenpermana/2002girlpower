package control;

import entity.*;
import java.util.*;
import utils.ProjectFileManager;

/**
 * Control class for managing Project operations in the BTO system.
 * Demonstrates the use of the Controller pattern in MVC architecture.
 */
public class ProjectControl {
    private ProjectFileManager fileManager;
    
    /**
     * Constructor for ProjectControl
     */
    public ProjectControl() {
        this.fileManager = ProjectFileManager.getInstance();
    }
    
    /**
     * Get all projects in the system
     * @return list of all projects
     */
    public List<Project> getAllProjects() {
        return fileManager.readAllProjects();
    }
    
    /**
     * Get eligible projects for a user
     * @param user the user
     * @return list of projects the user is eligible for
     */
    public List<Project> getEligibleProjects(User user) {
        List<Project> allProjects = getAllProjects();
        List<Project> eligibleProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            if (project.isVisible() && project.checkEligibility(user)) {
                eligibleProjects.add(project);
            }
        }
        
        return eligibleProjects;
    }
    
    /**
     * Get visible projects for a user
     * @param user the user
     * @param filters optional filters
     * @return list of visible projects
     */
    public List<Project> getVisibleProjectsForUser(User user, Map<String, Object> filters) {
        List<Project> allProjects = getAllProjects();
        List<Project> visibleProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            // Managers can see all projects
            if (user instanceof HDBManager) {
                visibleProjects.add(project);
                continue;
            }
            
            // Officers can see projects they are handling, regardless of visibility
            if (user instanceof HDBOfficer) {
                HDBOfficer officer = (HDBOfficer) user;
                if (officer.isHandlingProject(project)) {
                    visibleProjects.add(project);
                    continue;
                }
            }
            
            // For all users, check visibility and eligibility
            if (project.isVisible() && project.checkEligibility(user)) {
                visibleProjects.add(project);
            }
        }
        
        // Apply filters if provided
        if (filters != null && !filters.isEmpty()) {
            visibleProjects = applyFilters(visibleProjects, filters);
        }
        
        return visibleProjects;
    }
    
    /**
     * Apply filters to a list of projects
     * @param projects the projects to filter
     * @param filters the filters to apply
     * @return filtered list of projects
     */
    private List<Project> applyFilters(List<Project> projects, Map<String, Object> filters) {
        List<Project> filteredProjects = new ArrayList<>(projects);
        
        // Neighborhood filter
        if (filters.containsKey("neighborhood")) {
            String neighborhood = (String) filters.get("neighborhood");
            filteredProjects.removeIf(p -> !p.getNeighborhood().equalsIgnoreCase(neighborhood));
        }
        
        // Flat type filter
        if (filters.containsKey("flatType")) {
            FlatType flatType = (FlatType) filters.get("flatType");
            filteredProjects.removeIf(p -> !p.getFlatTypes().contains(flatType));
        }
        
        // Add more filters as needed
        
        return filteredProjects;
    }
    
    /**
     * Get projects created by a specific manager
     * @param manager the manager
     * @return list of projects created by the manager
     */
    public List<Project> getProjectsByManager(HDBManager manager) {
        List<Project> allProjects = getAllProjects();
        List<Project> managerProjects = new ArrayList<>();
        
        for (Project project : allProjects) {
            if (project.getManagerInCharge().getNRIC().equals(manager.getNRIC())) {
                managerProjects.add(project);
            }
        }
        
        return managerProjects;
    }
    
    /**
     * Add a new project to the system
     * @param project the project to add
     * @return true if addition was successful, false otherwise
     */
    public boolean addProject(Project project) {
        return fileManager.addProject(project);
    }
    
    /**
     * Update an existing project
     * @param project the updated project
     * @return true if update was successful, false otherwise
     */
    public boolean updateProject(Project project) {
        return fileManager.updateProject(project);
    }
    
    /**
     * Delete a project
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProject(Project project) {
        return fileManager.deleteProject(project);
    }
    
    /**
     * Toggle the visibility of a project
     * @param project the project
     * @param visible the new visibility
     * @return true if toggle was successful, false otherwise
     */
    public boolean toggleVisibility(Project project, boolean visible) {
        project.setVisible(visible);
        return updateProject(project);
    }
}