package control;

import entity.*;
import java.io.*;
import java.util.*;

/**
 * Controls operations related to HDB Managers in the BTO system.
 */
public class HDBManagerControl {
    
    /**
     * Get officer registrations for a project
     * @param project the project
     * @return list of officer registrations
     */
    public List<Map<String, Object>> getOfficerRegistrations(Project project) {
        // Delegate to HDBOfficerControl
        HDBOfficerControl officerControl = new HDBOfficerControl();
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
        if (!project.getManagerInCharge().getManagerID().equals(manager.getManagerID())) {
            return false;
        }
        
        // Check if there are available slots for approval
        if (approve && project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        // Delegate to HDBOfficerControl
        HDBOfficerControl officerControl = new HDBOfficerControl();
        RegistrationStatus status = approve ? RegistrationStatus.APPROVED : RegistrationStatus.REJECTED;
        return officerControl.updateRegistrationStatus(officer, project, status);
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
        ApplicationControl applicationControl = new ApplicationControl();
        
        for (Application app : applications) {
            // Check if manager is in charge of the project
            if (!app.getProject().getManagerInCharge().getManagerID().equals(manager.getManagerID())) {
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
            
            if (isApproved) {
                // Approve application
                // Check if there are available units of the requested type
                // In a real system, you would determine the flat type from the application
                FlatType requestedType = FlatType.TWO_ROOM; // Placeholder
                
                if (app.getProject().getAvailableUnitsByType(requestedType) > 0) {
                    app.setStatus(ApplicationStatus.SUCCESSFUL);
                    
                    // Decrement available units
                    Project project = app.getProject();
                    project.decrementAvailableUnits(requestedType);
                } else {
                    // Not enough units, reject instead
                    app.setStatus(ApplicationStatus.UNSUCCESSFUL);
                }
            } else {
                // Reject application
                app.setStatus(ApplicationStatus.UNSUCCESSFUL);
            }
            
            // Update application
            if (applicationControl.updateApplication(app)) {
                processed++;
            }
        }
        
        return processed;
    }
    
    /**
     * Process a withdrawal request
     * @param manager the manager processing the request
     * @param application the application to withdraw
     * @param approve true to approve, false to reject
     * @return true if processing is successful, false otherwise
     */
    public boolean processWithdrawalRequest(HDBManager manager, Application application, boolean approve) {
        // Check if manager is in charge of the project
        if (!application.getProject().getManagerInCharge().getManagerID().equals(manager.getManagerID())) {
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
                
                // If flat was booked, get the actual flat type
                if (currentStatus == ApplicationStatus.BOOKED && application.getBookedFlat() != null) {
                    flatType = application.getBookedFlat().getType();
                    
                    // Free the flat
                    Flat bookedFlat = application.getBookedFlat();
                    bookedFlat.setBookedByApplication(null);
                    application.setBookedFlat(null);
                }
                
                // Increment available units
                application.getProject().incrementAvailableUnits(flatType);
            }
            
            // Update application
            ApplicationControl applicationControl = new ApplicationControl();
            return applicationControl.updateApplication(application);
        }
        
        // If rejected, no changes needed
        return true;
    }
    
    /**
     * Create a new project
     * @param manager the manager creating the project
     * @param details map of project details
     * @return the created project, or null if creation failed
     */
    public Project createProject(HDBManager manager, Map<String, Object> details) {
        // Extract project details
        String projectName = (String) details.get("projectName");
        String neighborhood = (String) details.get("neighborhood");
        Date openDate = (Date) details.get("openDate");
        Date closeDate = (Date) details.get("closeDate");
        int officerSlots = (int) details.get("officerSlots");
        int twoRoomUnits = (int) details.get("twoRoomUnits");
        int threeRoomUnits = (int) details.get("threeRoomUnits");
        
        // Check if manager is already managing a project in the same period
        if (manager.isManagingProject(openDate, closeDate)) {
            return null;
        }
        
        // Create flat type units map
        Map<FlatType, Integer> totalUnits = new HashMap<>();
        if (twoRoomUnits > 0) totalUnits.put(FlatType.TWO_ROOM, twoRoomUnits);
        if (threeRoomUnits > 0) totalUnits.put(FlatType.THREE_ROOM, threeRoomUnits);
        
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
            manager,
            officerSlots
        );
        
        // Add to manager's projects
        manager.getManagedProjects().add(project);
        
        // Save project
        ProjectControl projectControl = new ProjectControl();
        if (projectControl.addProject(project)) {
            return project;
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
        if (!project.getManagerInCharge().getManagerID().equals(manager.getManagerID())) {
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
        
        // Save updated project
        ProjectControl projectControl = new ProjectControl();
        return projectControl.updateProject(project);
    }
    
    /**
     * Delete a project
     * @param manager the manager deleting the project
     * @param project the project to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProject(HDBManager manager, Project project) {
        // Check if manager is in charge of the project
        if (!project.getManagerInCharge().getManagerID().equals(manager.getManagerID())) {
            return false;
        }
        
        // Remove from manager's projects
        manager.getManagedProjects().remove(project);
        
        // Delete project
        ProjectControl projectControl = new ProjectControl();
        return projectControl.deleteProject(project);
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
        
        // Set visibility
        project.setVisible(visible);
        
        // Save updated project
        ProjectControl projectControl = new ProjectControl();
        return projectControl.updateProject(project);
    }
    
    /**
     * Generate a project ID
     * @param projectName the project name
     * @return a unique project ID
     */
    private String generateProjectID(String projectName) {
        // Simple algorithm to generate a project ID
        // In a real system, you would use a more robust method
        String prefix = projectName.substring(0, Math.min(3, projectName.length())).toUpperCase();
        return prefix + System.currentTimeMillis() % 10000;
    }
}