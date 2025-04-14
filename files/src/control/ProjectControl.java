package control;

import entity.*;
import java.util.*;
import java.util.stream.Collectors;
import utils.DataStorage;
import utils.ProjectDataManager;

/**
 * Control class for managing Project operations in the BTO system.
 * Demonstrates the use of Dependency Inversion Principle by depending on
 * DataStorage interface rather than concrete implementation
 */
public class ProjectControl {
    private final DataStorage<Project> dataStorage;
    
    /**
     * Constructor with default storage
     */
    public ProjectControl() {
        this.dataStorage = ProjectDataManager.getInstance();
    }
    
    /**
     * Constructor with custom storage (for testing or different storage methods)
     * Demonstrates Dependency Inversion Principle
     */
    public ProjectControl(DataStorage<Project> storage) {
        this.dataStorage = storage;
    }
    
    /**
     * Get all projects in the system
     * @return list of all projects
     */
    public List<Project> getAllProjects() {
        return dataStorage.readAll();
    }
    
    /**
     * Get eligible projects for a user
     * @param user the user
     * @return list of projects the user is eligible for
     */
    public List<Project> getEligibleProjects(User user) {
        return getAllProjects().stream()
                .filter(project -> project.isVisible() && project.checkEligibility(user))
                .collect(Collectors.toList());
    }
    
    /**
     * Get visible projects for a specific user with role-based visibility rules
     * @param user the user
     * @return list of visible projects
     */
    public List<Project> getVisibleProjectsForUser(User user) {
        return getVisibleProjectsForUser(user, null);
    }
    
    /**
     * Get visible projects for a user with optional filters
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
            
            // Officers can see projects they are handling regardless of visibility
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
            String flatTypeStr = (String) filters.get("flatType");
            FlatType flatType = null;
            
            if (flatTypeStr.equalsIgnoreCase("2-Room")) {
                flatType = FlatType.TWO_ROOM;
            } else if (flatTypeStr.equalsIgnoreCase("3-Room")) {
                flatType = FlatType.THREE_ROOM;
            }
            
            if (flatType != null) {
                FlatType finalFlatType = flatType;
                filteredProjects.removeIf(p -> !p.hasFlatType(finalFlatType));
            }
        }
        
        // Sort filter
        if (filters.containsKey("sortBy")) {
            String sortOption = (String) filters.get("sortBy");
            filteredProjects = sortProjects(filteredProjects, sortOption);
        }
        
        return filteredProjects;
    }
    
    /**
     * Sort projects by different criteria
     * @param projects the projects to sort
     * @param sortOption the sort option
     * @return sorted list of projects
     */
    public List<Project> sortProjects(List<Project> projects, String sortOption) {
        List<Project> sortedProjects = new ArrayList<>(projects);
        
        switch (sortOption) {
            case "1": // Flat Type
                // Sort by number of flat types
                sortedProjects.sort(Comparator.comparing(p -> p.getFlatTypes().size()));
                break;
            case "2": // Neighborhood
                sortedProjects.sort(Comparator.comparing(Project::getNeighborhood));
                break;
            case "3": // Price Range (placeholder - would need price data)
                // Using project ID as placeholder
                sortedProjects.sort(Comparator.comparing(Project::getProjectID));
                break;
            case "4": // Closing Date
                sortedProjects.sort(Comparator.comparing(Project::getApplicationCloseDate));
                break;
            case "6": // Availability (descending)
                sortedProjects.sort((p1, p2) -> {
                    int p1Available = calculateTotalAvailableUnits(p1);
                    int p2Available = calculateTotalAvailableUnits(p2);
                    return Integer.compare(p2Available, p1Available); // Descending
                });
                break;
            case "5": // Alphabetical (default)
            default:
                sortedProjects.sort(Comparator.comparing(Project::getProjectName));
                break;
        }
        
        return sortedProjects;
    }
    
    /**
     * Calculate total available units across all flat types
     * @param project the project
     * @return total available units
     */
    private int calculateTotalAvailableUnits(Project project) {
        int total = 0;
        for (FlatType type : project.getFlatTypes()) {
            total += project.getAvailableUnitsByType(type);
        }
        return total;
    }
    
    /**
     * Get projects created by a specific manager
     * @param manager the manager
     * @return list of projects created by the manager
     */
    public List<Project> getProjectsByManager(HDBManager manager) {
        return getAllProjects().stream()
                .filter(p -> p.getManagerInCharge().getManagerID().equals(manager.getManagerID()))
                .collect(Collectors.toList());
    }
    
    /**
     * Add a new project to the system
     * @param project the project to add
     * @return true if addition was successful, false otherwise
     */
    public boolean addProject(Project project) {
        return dataStorage.save(project);
    }
    
    /**
     * Update an existing project
     * @param project the updated project
     * @return true if update was successful, false otherwise
     */
    public boolean updateProject(Project project) {
        return dataStorage.update(project);
    }
    
    /**
     * Delete a project
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProject(Project project) {
        return dataStorage.delete(project);
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
    
    /**
     * Create a new project ID based on project name and manager
     * @param projectName the project name
     * @param manager the manager creating the project
     * @return a unique project ID
     */
    public String generateProjectID(String projectName, HDBManager manager) {
        // Take first 3 characters of project name (uppercase)
        String prefix = projectName.substring(0, Math.min(3, projectName.length())).toUpperCase();
        
        // Take last 4 characters of manager ID
        String managerPart = manager.getManagerID().substring(Math.max(0, manager.getManagerID().length() - 4));
        
        // Add timestamp to ensure uniqueness
        long timestamp = System.currentTimeMillis() % 10000;
        
        return prefix + managerPart + timestamp;
    }
}