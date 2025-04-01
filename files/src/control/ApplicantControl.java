package control;

import entity.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controls operations related to Applicants in the BTO system.
 */
public class ApplicantControl {
    private static final String PROJECT_FILE = "files/resources/ProjectList.csv";
    private List<Project> projects;
    
    /**
     * Constructor initializes the projects list from storage
     */
    public ApplicantControl() {
        this.projects = loadProjects();
    }
    
    /**
     * Get projects that an applicant is eligible for
     * @param applicant the applicant
     * @return list of eligible projects
     */
    public List<Project> getEligibleProjects(Applicant applicant) {
        List<Project> eligibleProjects = new ArrayList<>();
        
        for (Project project : projects) {
            // Check visibility
            if (!project.isVisible()) {
                continue; // Skip invisible projects
            }
            
            // Check eligibility based on age and marital status
            if (isEligibleForProject(applicant, project)) {
                eligibleProjects.add(project);
            }
        }
        
        return eligibleProjects;
    }
    
    /**
     * Check if an applicant is eligible for a project
     * @param applicant the applicant
     * @param project the project
     * @return true if eligible, false otherwise
     */
    private boolean isEligibleForProject(Applicant applicant, Project project) {
        // Singles, 35 years old and above, can ONLY apply for 2-Room
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            if (applicant.getAge() < 35) {
                return false; // Singles must be at least 35
            }
            
            // Check if the project has 2-Room flats
            return project.hasFlatType(FlatType.TWO_ROOM);
        } 
        // Married, 21 years old and above, can apply for any flat type
        else if (applicant.getMaritalStatus() == MaritalStatus.MARRIED) {
            return applicant.getAge() >= 21; // Married applicants must be at least 21
        }
        
        return false;
    }
    
    /**
     * Sort projects by different criteria
     * @param projects the list of projects to sort
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
     * Filter projects by neighborhood
     * @param projects the list of projects to filter
     * @param neighborhood the neighborhood to filter by
     * @return filtered list of projects
     */
    public List<Project> filterByNeighborhood(List<Project> projects, String neighborhood) {
        if (neighborhood == null || neighborhood.trim().isEmpty()) {
            return new ArrayList<>(projects);
        }
        
        List<Project> filteredProjects = new ArrayList<>();
        
        for (Project project : projects) {
            if (project.getNeighborhood().equalsIgnoreCase(neighborhood)) {
                filteredProjects.add(project);
            }
        }
        
        return filteredProjects;
    }
    
    /**
     * Filter projects by flat type
     * @param projects the list of projects to filter
     * @param flatTypeStr the flat type to filter by
     * @return filtered list of projects
     */
    public List<Project> filterByFlatType(List<Project> projects, String flatTypeStr) {
        if (flatTypeStr == null || flatTypeStr.trim().isEmpty()) {
            return new ArrayList<>(projects);
        }
        
        FlatType flatType = null;
        if (flatTypeStr.equalsIgnoreCase("2-Room")) {
            flatType = FlatType.TWO_ROOM;
        } else if (flatTypeStr.equalsIgnoreCase("3-Room")) {
            flatType = FlatType.THREE_ROOM;
        } else {
            return new ArrayList<>(projects); // Invalid flat type, return all
        }
        
        List<Project> filteredProjects = new ArrayList<>();
        
        for (Project project : projects) {
            if (project.hasFlatType(flatType)) {
                filteredProjects.add(project);
            }
        }
        
        return filteredProjects;
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
     * Apply for a project
     * @param applicant the applicant
     * @param project the project
     * @return true if application was successful, false otherwise
     */
    public boolean applyForProject(Applicant applicant, Project project) {
        // Delegate to ApplicationControl
        ApplicationControl applicationControl = new ApplicationControl();
        return applicationControl.submitApplication(applicant, project);
    }
    
    /**
     * Load projects from file
     * @return list of projects
     */
    private List<Project> loadProjects() {
        List<Project> loadedProjects = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        try (Scanner fileScanner = new Scanner(new File(PROJECT_FILE))) {
            // Skip header if exists
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] values = line.split(",");
                if (values.length < 11) continue; // Invalid line
                
                try {
                    // Parse project data
                    String projectID = values[0].trim();
                    String projectName = values[1].trim();
                    String neighborhood = values[2].trim();
                    
                    // Parse flat types and units
                    Map<FlatType, Integer> totalUnits = new HashMap<>();
                    int twoRoomUnits = Integer.parseInt(values[3].trim());
                    int threeRoomUnits = Integer.parseInt(values[5].trim());
                    
                    if (twoRoomUnits > 0) totalUnits.put(FlatType.TWO_ROOM, twoRoomUnits);
                    if (threeRoomUnits > 0) totalUnits.put(FlatType.THREE_ROOM, threeRoomUnits);
                    
                    // Parse dates
                    Date openDate = dateFormat.parse(values[7].trim());
                    Date closeDate = dateFormat.parse(values[8].trim());
                    
                    // Parse manager ID
                    String managerID = values[9].trim();
                    
                    // Find manager (placeholder)
                    HDBManager manager = new HDBManager(
                        "Manager " + managerID, // Placeholder name
                        managerID,
                        "password",
                        40, // Placeholder age
                        "Married", // Placeholder marital status
                        "HDBManager"
                    );
                    
                    // Parse officer slots
                    int officerSlots = Integer.parseInt(values[10].trim());
                    
                    // Parse visibility (last column, if available)
                    boolean isVisible = values.length > 11 && Boolean.parseBoolean(values[11].trim());
                    
                    // Create project
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
                    
                    // Set visibility
                    project.setVisible(isVisible);
                    
                    // Add to list
                    loadedProjects.add(project);
                    
                } catch (ParseException | NumberFormatException e) {
                    System.err.println("Error parsing project data: " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Projects file not found. Starting with empty list.");
        }
        
        return loadedProjects;
    }
}