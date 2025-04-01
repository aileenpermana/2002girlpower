package control;

import entity.*;
import java.io.*;
import java.util.*;

/**
 * Controls operations related to Projects in the BTO system.
 */
public class ProjectControl {
    private static final String PROJECTS_FILE = "files/resources/ProjectList.csv";
    private List<Project> projects;
    
    /**
     * Constructor initializes the projects list from storage
     */
    public ProjectControl() {
        this.projects = loadProjects();
    }
    
    /**
     * Get projects that a user is eligible for
     * @param user the user to check eligibility for
     * @return list of eligible projects
     */
    public List<Project> getEligibleProjects(User user) {
        List<Project> eligibleProjects = new ArrayList<>();
        
        for (Project project : projects) {
            // Check visibility
            if (!project.isVisible() && !(user instanceof HDBOfficer && ((HDBOfficer)user).isHandlingProject(project))) {
                continue; // Skip invisible projects unless user is handling officer
            }
            
            // Skip if user is a manager
            if (user instanceof HDBManager) {
                continue; // Managers cannot apply for projects
            }
            
            // Check if officer is already handling this project
            if (user instanceof HDBOfficer && ((HDBOfficer)user).isHandlingProject(project)) {
                continue; // Officers can't apply for projects they're handling
            }
            
            // Check eligibility based on age and marital status
            if (project.checkEligibility(user)) {
                eligibleProjects.add(project);
            }
        }
        
        return eligibleProjects;
    }
    
    /**
     * Get all projects in the system
     * @return list of all projects
     */
    public List<Project> getAllProjects() {
        return new ArrayList<>(projects);
    }
    
    /**
     * Register a user as an officer for a project
     * @param user the user to register
     * @param project the project to register for
     * @return true if registration is successful, false otherwise
     */
    public boolean registerAsOfficer(User user, Project project) {
        // Check if project still has available officer slots
        if (project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        // Check if user is an applicant with an active application for this project
        if (user instanceof Applicant) {
            Applicant applicant = (Applicant) user;
            for (Application app : applicant.getApplications()) {
                if (app.getProject().equals(project) && 
                    (app.getStatus() == ApplicationStatus.PENDING || 
                     app.getStatus() == ApplicationStatus.SUCCESSFUL || 
                     app.getStatus() == ApplicationStatus.BOOKED)) {
                    return false; // Already applied as an applicant
                }
            }
        }
        
        // Check if already a HDB Officer for another project in the same period
        Date startDate = project.getApplicationOpenDate();
        Date endDate = project.getApplicationCloseDate();
        
        if (user instanceof HDBOfficer) {
            HDBOfficer officer = (HDBOfficer) user;
            if (officer.isHandlingProject(startDate, endDate)) {
                return false; // Already handling another project in the same period
            }
        }
        
        // Create registration request (in a real system, this would be saved)
        // For this implementation, we'll assume registration requests are tracked elsewhere
        return true;
    }
    
    /**
     * Add a new project to the system
     * @param project the project to add
     * @return true if successful, false otherwise
     */
    public boolean addProject(Project project) {
        // Verify project doesn't already exist (by ID)
        for (Project p : projects) {
            if (p.getProjectID().equals(project.getProjectID())) {
                return false; // Project already exists
            }
        }
        
        // Add to list
        projects.add(project);
        
        // Save to file
        return saveProjects();
    }
    
    /**
     * Update an existing project
     * @param project the project to update
     * @return true if successful, false otherwise
     */
    public boolean updateProject(Project project) {
        // Find and replace the project
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getProjectID().equals(project.getProjectID())) {
                projects.set(i, project);
                return saveProjects();
            }
        }
        
        return false; // Project not found
    }
    
    /**
     * Delete a project from the system
     * @param project the project to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteProject(Project project) {
        // Remove project
        boolean removed = projects.removeIf(p -> p.getProjectID().equals(project.getProjectID()));
        
        if (removed) {
            return saveProjects();
        }
        
        return false; // Project not found
    }
    
    /**
     * Load projects from file
     * @return list of projects
     */
    private List<Project> loadProjects() {
        List<Project> loadedProjects = new ArrayList<>();
        
        try (Scanner fileScanner = new Scanner(new File(PROJECTS_FILE))) {
            // Skip header if exists
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] values = line.split(",");
                if (values.length < 10) continue; // Invalid line
                
                // Parse project data
                try {
                    String projectID = values[0].trim();
                    String projectName = values[1].trim();
                    String neighborhood = values[2].trim();
                    
                    // Parse flat types and units
                    Map<FlatType, Integer> totalUnits = new HashMap<>();
                    int twoRoomUnits = Integer.parseInt(values[3].trim());
                    int threeRoomUnits = Integer.parseInt(values[4].trim());
                    
                    if (twoRoomUnits > 0) totalUnits.put(FlatType.TWO_ROOM, twoRoomUnits);
                    if (threeRoomUnits > 0) totalUnits.put(FlatType.THREE_ROOM, threeRoomUnits);
                    
                    // Parse dates
                    Date openDate = new Date(Long.parseLong(values[5].trim()));
                    Date closeDate = new Date(Long.parseLong(values[6].trim()));
                    
                    // Parse manager ID
                    String managerID = values[7].trim();
                    
                    // Find manager (in a real system, this would be from a manager repository)
                    // For now, create a placeholder manager
                    HDBManager manager = new HDBManager(
                        "Manager " + managerID, // Placeholder name
                        managerID,
                        "password",
                        40, // Placeholder age
                        "Married", // Placeholder marital status
                        "HDBManager"
                    );
                    
                    // Parse officer slots
                    int maxOfficerSlots = Integer.parseInt(values[8].trim());
                    
                    // Parse visibility
                    boolean isVisible = Boolean.parseBoolean(values[9].trim());
                    
                    // Create project
                    Project project = new Project(
                        projectID,
                        projectName,
                        neighborhood,
                        totalUnits,
                        openDate,
                        closeDate,
                        manager,
                        maxOfficerSlots
                    );
                    
                    // Set visibility
                    project.setVisible(isVisible);
                    
                    // Add to list
                    loadedProjects.add(project);
                    
                } catch (Exception e) {
                    System.err.println("Error parsing project data: " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Projects file not found. Starting with empty list.");
        }
        
        return loadedProjects;
    }
    
    /**
     * Save projects to file
     * @return true if successful, false otherwise
     */
    private boolean saveProjects() {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(PROJECTS_FILE))) {
                // Write header
                writer.println("ProjectID,ProjectName,Neighborhood,TwoRoomUnits,ThreeRoomUnits,OpenDate,CloseDate,ManagerID,OfficerSlots,Visible");
                
                // Write projects
                for (Project project : projects) {
                    writer.println(
                        project.getProjectID() + "," +
                        project.getProjectName() + "," +
                        project.getNeighborhood() + "," +
                        project.getTotalUnitsByType(FlatType.TWO_ROOM) + "," +
                        project.getTotalUnitsByType(FlatType.THREE_ROOM) + "," +
                        project.getApplicationOpenDate().getTime() + "," +
                        project.getApplicationCloseDate().getTime() + "," +
                        project.getManagerInCharge().getManagerID() + "," +
                        (project.getAvailableOfficerSlots() + project.getOfficers().size()) + "," +
                        project.isVisible()
                    );
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving projects: " + e.getMessage());
            return false;
        }
    }
}