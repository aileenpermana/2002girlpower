package entity;

import java.util.Map;

/**
 * Defines operations for project management
 * Demonstrates Interface Segregation Principle by providing
 * focused operations related only to project management
 */
public interface ProjectManagement {
    /**
     * Create a new project with the given details
     * @param details map containing project details
     * @return the created project, or null if creation failed
     */
    Project createProject(Map<String, Object> details);
    
    /**
     * Edit an existing project
     * @param project the project to edit
     * @param details map containing updated details
     * @return true if edit was successful, false otherwise
     */
    boolean editProject(Project project, Map<String, Object> details);
    
    /**
     * Delete a project
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteProject(Project project);
    
    /**
     * Toggle the visibility of a project
     * @param project the project to toggle
     * @param visible whether the project should be visible
     * @return true if successful, false otherwise
     */
    boolean toggleVisibility(Project project, boolean visible);
}