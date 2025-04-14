package utils;

import entity.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Unified class for managing Project data in CSV files
 * Implements DataStorage interface while providing the Singleton pattern
 * Combines functionality from ProjectCSVStorage and ProjectFileManager
 */
public class ProjectDataManager implements DataStorage<Project> {
    // Singleton instance
    private static ProjectDataManager instance;
    
    // File paths
    private static final String FILE_PATH = "files/resources/ProjectList.csv";
    private static final String MANAGER_FILE_PATH = "files/resources/ManagerList.csv";
    
    // Date format for parsing/formatting dates
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    /**
     * Private constructor (Singleton pattern)
     * Ensures the directory and file structure exists
     */
    private ProjectDataManager() {
        ensureDirectoryAndFileExists();
    }
    
    /**
     * Get the singleton instance
     * @return the ProjectDataManager instance
     */
    public static ProjectDataManager getInstance() {
        if (instance == null) {
            instance = new ProjectDataManager();
        }
        return instance;
    }
    
    /**
     * Ensure the directory and file structure exists
     */
    private void ensureDirectoryAndFileExists() {
        // Make sure the directory exists
        File resourceDir = new File("files/resources");
        if (!resourceDir.exists()) {
            resourceDir.mkdirs();
        }
        
        // Check if the file exists
        File projectFile = new File(FILE_PATH);
        if (!projectFile.exists()) {
            try {
                projectFile.createNewFile();
                // Write header
                try (FileWriter fw = new FileWriter(projectFile)) {
                    fw.write("Project Name,Neighborhood,Type 1,Number of units for Type 1,Selling price for Type 1," +
                             "Type 2,Number of units for Type 2,Selling price for Type 2," +
                             "Application opening date,Application closing date,Manager,Officer Slot,Officer,Visibility\n");
                }
            } catch (IOException e) {
                System.out.println("Error creating project file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Load initial project data from CSV file
     * Should be called when the application starts
     */
    public void loadInitialProjectData() {
        System.out.println("Loading initial project data from: " + FILE_PATH);
        
        // Check if file exists
        File projectFile = new File(FILE_PATH);
        if (!projectFile.exists()) {
            System.out.println("Project file not found. Creating new file at: " + FILE_PATH);
            ensureDirectoryAndFileExists();
            
            // Create the file with header and sample project
            try (FileWriter fw = new FileWriter(projectFile)) {
                fw.write("Project Name,Neighborhood,Type 1,Number of units for Type 1,Selling price for Type 1," +
                         "Type 2,Number of units for Type 2,Selling price for Type 2," +
                         "Application opening date,Application closing date,Manager,Officer Slot,Officer,Visibility\n");
                
                // Add a sample project
                fw.write("Sunrise Heights,Yishun,2-Room,100,350000,3-Room,150,450000,01/05/2024,01/08/2024,S1234567A,5,,true\n");
            } catch (IOException e) {
                System.out.println("Error creating project file: " + e.getMessage());
            }
        }
        
        // Read projects
        List<Project> projects = readAll();
        System.out.println("Loaded " + projects.size() + " projects from file.");
    }
    
    /**
     * Read all projects from CSV file
     * Implements DataStorage.readAll()
     * @return list of all projects
     */
    @Override
    public List<Project> readAll() {
        List<Project> projects = new ArrayList<>();
        File projectFile = new File(FILE_PATH);
        
        if (!projectFile.exists() || projectFile.length() == 0) {
            return projects;
        }
        
        // First load all managers to use real manager objects
        Map<String, HDBManager> managerMap = loadManagers();
        
        try (BufferedReader br = new BufferedReader(new FileReader(projectFile))) {
            String line;
            boolean isHeader = true;
            
            // Read and process each line
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                if (!line.trim().isEmpty()) {
                    Project project = parseProjectFromCSV(line, managerMap);
                    if (project != null) {
                        projects.add(project);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading project file: " + e.getMessage());
        }
        
        return projects;
    }
    
    /**
     * Load managers from the manager CSV file
     * @return map of NRIC to HDBManager objects
     */
    private Map<String, HDBManager> loadManagers() {
        Map<String, HDBManager> managerMap = new HashMap<>();
        File managerFile = new File(MANAGER_FILE_PATH);
        
        if (!managerFile.exists() || managerFile.length() == 0) {
            System.out.println("Warning: Manager file not found or empty. Using placeholder managers.");
            return managerMap;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(managerFile))) {
            String line;
            boolean isHeader = true;
            
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                if (!line.trim().isEmpty()) {
                    try {
                        String[] fields = line.split(",");
                        if (fields.length >= 5) {
                            String name = fields[0].trim();
                            String nric = fields[1].trim();
                            int age = Integer.parseInt(fields[2].trim());
                            String maritalStatus = fields[3].trim();
                            String password = fields[4].trim();
                            
                            HDBManager manager = new HDBManager(name, nric, password, age, maritalStatus, "HDBManager");
                            managerMap.put(nric, manager);
                        }
                    } catch (Exception e) {
                        System.out.println("Error parsing manager data: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading manager file: " + e.getMessage());
        }
        
        return managerMap;
    }
    
    /**
     * Save a project to CSV file
     * Implements DataStorage.save()
     * @param project the project to save
     * @return true if successful, false otherwise
     */
    @Override
    public boolean save(Project project) {
        List<Project> projects = readAll();
        projects.add(project);
        return saveAll(projects);
    }
    
    /**
     * Update a project in CSV file
     * Implements DataStorage.update()
     * @param updatedProject the updated project
     * @return true if successful, false otherwise
     */
    @Override
    public boolean update(Project updatedProject) {
        List<Project> projects = readAll();
        
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getProjectID().equals(updatedProject.getProjectID())) {
                projects.set(i, updatedProject);
                return saveAll(projects);
            }
        }
        
        return false; // Project not found
    }
    
    /**
     * Delete a project from CSV file
     * Implements DataStorage.delete()
     * @param project the project to delete
     * @return true if successful, false otherwise
     */
    @Override
    public boolean delete(Project project) {
        List<Project> projects = readAll();
        
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getProjectID().equals(project.getProjectID())) {
                projects.remove(i);
                return saveAll(projects);
            }
        }
        
        return false; // Project not found
    }
    
    /**
     * Save all projects to CSV file
     * Implements DataStorage.saveAll()
     * @param projects the list of projects to save
     * @return true if successful, false otherwise
     */
    @Override
    public boolean saveAll(List<Project> projects) {
        ensureDirectoryAndFileExists();
        
        try (FileWriter fw = new FileWriter(FILE_PATH)) {
            // Write header
            fw.write("Project Name,Neighborhood,Type 1,Number of units for Type 1,Selling price for Type 1," +
                     "Type 2,Number of units for Type 2,Selling price for Type 2," +
                     "Application opening date,Application closing date,Manager,Officer Slot,Officer,Visibility\n");
            
            // Write projects
            for (Project project : projects) {
                fw.write(formatProjectForCSV(project) + "\n");
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving projects: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Read all projects (alias for readAll to maintain compatibility with ProjectFileManager)
     * @return list of projects
     */
    public List<Project> readAllProjects() {
        return readAll();
    }
    
    /**
     * Save all projects (alias for saveAll to maintain compatibility with ProjectFileManager)
     * @param projects the list of projects to save
     * @return true if successful, false otherwise
     */
    public boolean saveAllProjects(List<Project> projects) {
        return saveAll(projects);
    }
    
    /**
     * Add a project (alias for save to maintain compatibility with ProjectFileManager)
     * @param project the project to add
     * @return true if successful, false otherwise
     */
    public boolean addProject(Project project) {
        return save(project);
    }
    
    /**
     * Update a project (alias for update to maintain compatibility with ProjectFileManager)
     * @param project the project to update
     * @return true if successful, false otherwise
     */
    public boolean updateProject(Project project) {
        return update(project);
    }
    
    /**
     * Delete a project (alias for delete to maintain compatibility with ProjectFileManager)
     * @param project the project to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteProject(Project project) {
        return delete(project);
    }
    
    /**
     * Parse a project from a CSV line using actual manager objects
     * @param line the CSV line
     * @param managerMap map of NRIC to HDBManager objects
     * @return the parsed Project object, or null if parsing failed
     */
    private Project parseProjectFromCSV(String line, Map<String, HDBManager> managerMap) {
        try {
            String[] fields = line.split(",");
            
            if (fields.length < 12) {
                System.out.println("Invalid project record (not enough fields): " + line);
                return null;
            }
            
            // Extract basic project details
            String projectName = fields[0].trim();
            String neighborhood = fields[1].trim();
            
            // Parse flat types and units
            Map<FlatType, Integer> totalUnits = new HashMap<>();
            if (fields[2].equals("2-Room") && !fields[3].isEmpty()) {
                try {
                    totalUnits.put(FlatType.TWO_ROOM, Integer.parseInt(fields[3].trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format for 2-Room units: " + fields[3]);
                    totalUnits.put(FlatType.TWO_ROOM, 0);
                }
            }
            
            if (fields[5].equals("3-Room") && !fields[6].isEmpty()) {
                try {
                    totalUnits.put(FlatType.THREE_ROOM, Integer.parseInt(fields[6].trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format for 3-Room units: " + fields[6]);
                    totalUnits.put(FlatType.THREE_ROOM, 0);
                }
            }
            
            // Parse dates with error handling
            Date openDate;
            Date closeDate;
            try {
                openDate = DATE_FORMAT.parse(fields[8].trim());
                closeDate = DATE_FORMAT.parse(fields[9].trim());
            } catch (ParseException e) {
                System.out.println("Error parsing dates in project record: " + e.getMessage());
                return null;
            }
            
            // Get actual manager from map or create a temporary one if not found
            String managerNRIC = fields[10].trim();
            HDBManager manager = managerMap.get(managerNRIC);
            
            if (manager == null) {
                // If manager not found in map, create a temporary one with the NRIC
                System.out.println("Manager with NRIC " + managerNRIC + " not found in manager list. Using placeholder.");
                manager = new HDBManager(
                    "Manager of " + projectName, 
                    managerNRIC, 
                    "password", 
                    30, 
                    "Married", 
                    "HDBManager"
                );
            }
            
            // Parse officer slots with error handling
            int officerSlots;
            try {
                officerSlots = Integer.parseInt(fields[11].trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format for officer slots: " + fields[11]);
                officerSlots = 5; // Default value
            }
            
            // Create the project
            String projectID = generateProjectID(projectName, managerNRIC);
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
            
            // Set project visibility if available (default true)
            if (fields.length > 13) {
                try {
                    project.setVisible(Boolean.parseBoolean(fields[13].trim()));
                } catch (Exception e) {
                    project.setVisible(true);
                }
            } else {
                project.setVisible(true);
            }
            
            return project;
            
        } catch (Exception e) {
            System.out.println("Error parsing project record: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Format a project as a CSV line
     * @param project the project to format
     * @return formatted CSV line
     */
    private String formatProjectForCSV(Project project) {
        StringBuilder sb = new StringBuilder();
        
        // Project Name
        sb.append(project.getProjectName()).append(",");
        
        // Neighborhood
        sb.append(project.getNeighborhood()).append(",");
        
        // Flat Types
        // Type 1 (2-Room)
        sb.append("2-Room").append(",");
        sb.append(project.getTotalUnitsByType(FlatType.TWO_ROOM)).append(",");
        sb.append("0").append(","); // Placeholder for selling price
        
        // Type 2 (3-Room)
        sb.append("3-Room").append(",");
        sb.append(project.getTotalUnitsByType(FlatType.THREE_ROOM)).append(",");
        sb.append("0").append(","); // Placeholder for selling price
        
        // Dates
        sb.append(DATE_FORMAT.format(project.getApplicationOpenDate())).append(",");
        sb.append(DATE_FORMAT.format(project.getApplicationCloseDate())).append(",");
        
        // Manager
        sb.append(project.getManagerInCharge().getNRIC()).append(",");
        
        // Officer Slots (available + used)
        int totalSlots = project.getAvailableOfficerSlots() + project.getOfficers().size();
        sb.append(totalSlots).append(",");
        
        // Officers - placeholder
        sb.append(",");
        
        // Visibility
        sb.append(project.isVisible());
        
        return sb.toString();
    }
    
    /**
     * Generate a project ID based on the project name and manager NRIC
     * @param projectName the name of the project
     * @param managerNRIC the manager's NRIC
     * @return a project ID
     */
    private String generateProjectID(String projectName, String managerNRIC) {
        String prefix = projectName.substring(0, Math.min(3, projectName.length())).toUpperCase();
        String managerPart = managerNRIC.substring(1, 3);
        return prefix + managerPart + System.currentTimeMillis() % 10000;
    }
}