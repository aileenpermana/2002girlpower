package boundary;

import control.HDBManagerControl;
import entity.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import utils.ScreenUtil;

/**
 * UI class for HDB Manager operations in the BTO Management System.
 * Demonstrates the Boundary pattern in MVC architecture.
 * Follows Single Responsibility Principle by focusing on UI concerns only.
 */
public class ManagerUI {
    private final HDBManager currentUser;
    private final Scanner sc;
    private final HDBManagerControl managerControl;
    
    /**
     * Constructor for ManagerUI
     * @param user the logged-in HDB Manager
     */
    public ManagerUI(HDBManager user) {
        this.currentUser = user;
        this.sc = new Scanner(System.in);
        this.managerControl = new HDBManagerControl();
    }
    
    /**
     * Display the main menu for HDB Managers
     */
    public void displayMenu() {
        boolean exit = false;
        
        while (!exit) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== HDB Manager Menu =====");
            System.out.println("Welcome, " + currentUser.getName() + "!");
            System.out.println("Manager ID: " + currentUser.getManagerID());
            System.out.println("1. Manage Projects");
            System.out.println("2. View Projects");
            System.out.println("3. Manage Officer Registrations");
            System.out.println("4. Manage Applications");
            System.out.println("5. Manage Withdrawal Requests");
            System.out.println("6. Generate Reports");
            System.out.println("7. View My Profile");
            System.out.println("8. Change Password");
            System.out.println("9. Sign Out");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            switch (choice) {
                case "1":
                    manageProjects();
                    break;
                case "2":
                    viewProjects();
                    break;
                case "3":
                    System.out.println("Manage Officer Registrations - Feature available in full implementation");
                    waitForEnter();
                    break;
                case "4":
                    System.out.println("Manage Applications - Feature available in full implementation");
                    waitForEnter();
                    break;
                case "5":
                    System.out.println("Manage Withdrawal Requests - Feature available in full implementation");
                    waitForEnter();
                    break;
                case "6":
                    System.out.println("Generate Reports - Feature available in full implementation");
                    waitForEnter();
                    break;
                case "7":
                    viewProfile();
                    break;
                case "8":
                    changePassword();
                    break;
                case "9":
                    exit = true;
                    System.out.println("Signing out...");
                    break;
                default:
                    System.out.println("Invalid choice. Press Enter to continue.");
                    waitForEnter();
            }
        }
    }
    
    /**
     * Manage projects (create, edit, delete)
     */
    private void manageProjects() {
        boolean done = false;
        
        while (!done) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Manage Projects =====");
            System.out.println("1. Create New Project");
            System.out.println("2. Edit Existing Project");
            System.out.println("3. Delete Project");
            System.out.println("4. Toggle Project Visibility");
            System.out.println("5. Return to Main Menu");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            switch (choice) {
                case "1":
                    createProject();
                    break;
                case "2":
                    editProject();
                    break;
                case "3":
                    deleteProject();
                    break;
                case "4":
                    toggleProjectVisibility();
                    break;
                case "5":
                    done = true;
                    break;
                default:
                    System.out.println("Invalid choice. Press Enter to continue.");
                    waitForEnter();
            }
        }
    }
    
    /**
     * Create a new project
     */
    private void createProject() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Create New Project =====");
        
        try {
            // Get project details using input validation
            String projectName = getValidInput("Project Name: ", 
                input -> !input.trim().isEmpty(), 
                "Project name cannot be empty.");
            
            String neighborhood = getValidInput("Neighborhood (e.g., Yishun, Boon Lay): ", 
                input -> !input.trim().isEmpty(), 
                "Neighborhood cannot be empty.");
            
            int twoRoomUnits = getValidIntInput("Number of 2-Room units: ", 
                input -> input >= 0, 
                "Number of units cannot be negative.");
            
            int threeRoomUnits = getValidIntInput("Number of 3-Room units: ", 
                input -> input >= 0, 
                "Number of units cannot be negative.");
            
            // Ensure at least one type of unit is available
            if (twoRoomUnits == 0 && threeRoomUnits == 0) {
                System.out.println("Project must have at least one unit. Press Enter to continue.");
                waitForEnter();
                return;
            }
            
            // Get application dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date openDate = getValidDateInput("Application Opening Date (dd/MM/yyyy): ", dateFormat);
            Date closeDate = getValidDateInput("Application Closing Date (dd/MM/yyyy): ", dateFormat);
            
            // Check date validity
            if (closeDate.before(openDate)) {
                System.out.println("Closing date cannot be before opening date. Press Enter to continue.");
                waitForEnter();
                return;
            }
            
            int officerSlots = getValidIntInput("Number of HDB Officer slots (max 10): ", 
                input -> input >= 1 && input <= 10, 
                "Number of slots must be between 1 and 10.");
            
            // Create project details map
            Map<String, Object> projectDetails = new HashMap<>();
            projectDetails.put("projectName", projectName);
            projectDetails.put("neighborhood", neighborhood);
            projectDetails.put("twoRoomUnits", twoRoomUnits);
            projectDetails.put("threeRoomUnits", threeRoomUnits);
            projectDetails.put("openDate", openDate);
            projectDetails.put("closeDate", closeDate);
            projectDetails.put("officerSlots", officerSlots);
            
            // Create the project using the manager control
            Project newProject = managerControl.createProject(currentUser, projectDetails);
            
            if (newProject != null) {
                System.out.println("\nProject created successfully!");
                System.out.println("Project ID: " + newProject.getProjectID());
                System.out.println("Project Name: " + newProject.getProjectName());
            } else {
                System.out.println("\nFailed to create project. You may already be managing another project in the same period.");
            }
            
        } catch (Exception e) {
            System.out.println("\nAn error occurred: " + e.getMessage());
        }
        
        waitForEnter();
    }
    
    /**
     * Edit an existing project
     */
    private void editProject() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Edit Existing Project =====");
        
        // Get projects managed by this manager
        List<Project> managedProjects = managerControl.getProjectsByManager(currentUser);
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            waitForEnter();
            return;
        }
        
        // Display the list of managed projects
        System.out.println("Select a project to edit:");
        for (int i = 0; i < managedProjects.size(); i++) {
            System.out.println((i + 1) + ". " + managedProjects.get(i).getProjectName());
        }
        
        // Get project selection with validation
        int projectChoice = getValidIntInput("\nEnter project number (0 to cancel): ", 
            input -> input >= 0 && input <= managedProjects.size(), 
            "Invalid project number.");
        
        if (projectChoice == 0) {
            return;
        }
        
        Project selectedProject = managedProjects.get(projectChoice - 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        // Display current project details
        ScreenUtil.clearScreen();
        System.out.println("\n===== Edit Project: " + selectedProject.getProjectName() + " =====");
        System.out.println("Current Details:");
        System.out.println("1. Project Name: " + selectedProject.getProjectName());
        System.out.println("2. Neighborhood: " + selectedProject.getNeighborhood());
        System.out.println("3. 2-Room Units: " + selectedProject.getTotalUnitsByType(FlatType.TWO_ROOM));
        System.out.println("4. 3-Room Units: " + selectedProject.getTotalUnitsByType(FlatType.THREE_ROOM));
        System.out.println("5. Opening Date: " + dateFormat.format(selectedProject.getApplicationOpenDate()));
        System.out.println("6. Closing Date: " + dateFormat.format(selectedProject.getApplicationCloseDate()));
        System.out.println("7. Officer Slots: " + (selectedProject.getAvailableOfficerSlots() + selectedProject.getOfficers().size()));
        
        // Get detail selection with validation
        int detailChoice = getValidIntInput("\nEnter the number of the detail to edit (0 to cancel): ", 
            input -> input >= 0 && input <= 7, 
            "Invalid choice.");
        
        if (detailChoice == 0) {
            return;
        }
        
        // Prepare details map for update
        Map<String, Object> updatedDetails = new HashMap<>();
        
        // Get and update the selected detail
        switch (detailChoice) {
            case 1: // Project Name
                String newName = getInputWithPrompt("Enter new Project Name: ");
                if (!newName.trim().isEmpty()) {
                    updatedDetails.put("projectName", newName);
                }
                break;
            case 2: // Neighborhood
                String newNeighborhood = getInputWithPrompt("Enter new Neighborhood: ");
                if (!newNeighborhood.trim().isEmpty()) {
                    updatedDetails.put("neighborhood", newNeighborhood);
                }
                break;
            case 3: // 2-Room Units
                try {
                    int newTwoRoomUnits = getValidIntInput("Enter new number of 2-Room Units: ", 
                        input -> input >= 0, 
                        "Number cannot be negative.");
                    updatedDetails.put("twoRoomUnits", newTwoRoomUnits);
                } catch (Exception e) {
                    System.out.println("Invalid input. Update cancelled.");
                }
                break;
            case 4: // 3-Room Units
                try {
                    int newThreeRoomUnits = getValidIntInput("Enter new number of 3-Room Units: ", 
                        input -> input >= 0, 
                        "Number cannot be negative.");
                    updatedDetails.put("threeRoomUnits", newThreeRoomUnits);
                } catch (Exception e) {
                    System.out.println("Invalid input. Update cancelled.");
                }
                break;
            case 5: // Opening Date
                try {
                    Date newOpenDate = getValidDateInput("Enter new Opening Date (dd/MM/yyyy): ", dateFormat);
                    updatedDetails.put("openDate", newOpenDate);
                } catch (Exception e) {
                    System.out.println("Invalid date format. Update cancelled.");
                }
                break;
            case 6: // Closing Date
                try {
                    Date newCloseDate = getValidDateInput("Enter new Closing Date (dd/MM/yyyy): ", dateFormat);
                    updatedDetails.put("closeDate", newCloseDate);
                } catch (Exception e) {
                    System.out.println("Invalid date format. Update cancelled.");
                }
                break;
            case 7: // Officer Slots
                try {
                    int newOfficerSlots = getValidIntInput("Enter new number of Officer Slots (max 10): ", 
                        input -> input >= 1 && input <= 10, 
                        "Number must be between 1 and 10.");
                    updatedDetails.put("officerSlots", newOfficerSlots);
                } catch (Exception e) {
                    System.out.println("Invalid input. Update cancelled.");
                }
                break;
        }
        
        if (!updatedDetails.isEmpty()) {
            // Apply the updates
            boolean success = managerControl.editProject(currentUser, selectedProject, updatedDetails);
            
            if (success) {
                System.out.println("Project updated successfully!");
            } else {
                System.out.println("Failed to update project. Please check your inputs.");
            }
        } else {
            System.out.println("No changes made to project.");
        }
        
        waitForEnter();
    }
    
    /**
     * Delete a project
     */
    private void deleteProject() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Delete Project =====");
        
        // Get projects managed by this manager
        List<Project> managedProjects = managerControl.getProjectsByManager(currentUser);
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            waitForEnter();
            return;
        }
        
        // Display the list of managed projects
        System.out.println("Select a project to delete:");
        for (int i = 0; i < managedProjects.size(); i++) {
            System.out.println((i + 1) + ". " + managedProjects.get(i).getProjectName());
        }
        
        // Get project selection with validation
        int projectChoice = getValidIntInput("\nEnter project number (0 to cancel): ", 
            input -> input >= 0 && input <= managedProjects.size(), 
            "Invalid project number.");
        
        if (projectChoice == 0) {
            return;
        }
        
        Project selectedProject = managedProjects.get(projectChoice - 1);
        
        // Confirm deletion
        System.out.print("\nAre you sure you want to delete project '" + 
                        selectedProject.getProjectName() + "'? (Y/N): ");
        String confirmation = sc.nextLine();
        
        if (confirmation.equalsIgnoreCase("Y")) {
            boolean success = managerControl.deleteProject(currentUser, selectedProject);
            
            if (success) {
                System.out.println("Project deleted successfully!");
            } else {
                System.out.println("Failed to delete project. There may be active applications or officers assigned.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
        
        waitForEnter();
    }
    
    /**
     * Toggle project visibility
     */
    private void toggleProjectVisibility() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Toggle Project Visibility =====");
        
        // Get all projects managed by this manager
        List<Project> managedProjects = managerControl.getProjectsByManager(currentUser);
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            waitForEnter();
            return;
        }
        
        // Display the list of managed projects with their visibility status
        System.out.println("Current Project Visibility:");
        for (int i = 0; i < managedProjects.size(); i++) {
            Project p = managedProjects.get(i);
            System.out.println((i + 1) + ". " + p.getProjectName() + " - " + 
                             (p.isVisible() ? "Visible" : "Hidden"));
        }
        
        // Get project selection with validation
        int projectChoice = getValidIntInput("\nEnter project number to toggle visibility (0 to cancel): ", 
            input -> input >= 0 && input <= managedProjects.size(), 
            "Invalid project number.");
        
        if (projectChoice == 0) {
            return;
        }
        
        Project selectedProject = managedProjects.get(projectChoice - 1);
        boolean currentVisibility = selectedProject.isVisible();
        
        // Toggle visibility
        boolean success = managerControl.toggleProjectVisibility(currentUser, selectedProject, !currentVisibility);
        
        if (success) {
            System.out.println("\nProject '" + selectedProject.getProjectName() + "' is now " + 
                             (selectedProject.isVisible() ? "visible" : "hidden") + " to applicants.");
        } else {
            System.out.println("\nFailed to toggle project visibility.");
        }
        
        waitForEnter();
    }
    
    /**
     * View projects (all or filtered by manager)
     */
    private void viewProjects() {
        boolean done = false;
        
        while (!done) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== View Projects =====");
            System.out.println("1. View All Projects");
            System.out.println("2. View My Projects");
            System.out.println("3. Return to Main Menu");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            switch (choice) {
                case "1":
                    viewAllProjects();
                    break;
                case "2":
                    viewMyProjects();
                    break;
                case "3":
                    done = true;
                    break;
                default:
                    System.out.println("Invalid choice. Press Enter to continue.");
                    waitForEnter();
            }
        }
    }
    
    /**
     * View all projects
     */
    private void viewAllProjects() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== All Projects =====");
        
        List<Project> allProjects = managerControl.getAllProjects();
        
        if (allProjects.isEmpty()) {
            System.out.println("No projects found in the system.");
            waitForEnter();
            return;
        }
        
        displayProjectList(allProjects, "All");
    }
    
    /**
     * View projects managed by the current manager
     */
    private void viewMyProjects() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== My Projects =====");
        
        List<Project> myProjects = managerControl.getProjectsByManager(currentUser);
        
        if (myProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            waitForEnter();
            return;
        }
        
        displayProjectList(myProjects, "My");
    }
    
    /**
     * Display a list of projects with options to view details
     * @param projects list of projects to display
     * @param listType type of list ("All" or "My")
     */
    private void displayProjectList(List<Project> projects, String listType) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.printf("%-5s %-20s %-15s %-12s %-20s %-15s %-10s\n", 
                         "No.", "Project Name", "Neighborhood", "Visibility", "Closing Date", "Total Units", "Manager ID");
        System.out.println("-------------------------------------------------------------------------------------------");
        
        for (int i = 0; i < projects.size(); i++) {
            Project p = projects.get(i);
            int totalUnits = calculateTotalUnits(p);
            
            System.out.printf("%-5d %-20s %-15s %-12s %-20s %-15d %-10s\n", 
                            (i + 1),
                            truncateString(p.getProjectName(), 20),
                            truncateString(p.getNeighborhood(), 15),
                            p.isVisible() ? "Visible" : "Hidden",
                            dateFormat.format(p.getApplicationCloseDate()),
                            totalUnits,
                            truncateString(p.getManagerInCharge().getManagerID(), 10));
        }
        
        System.out.println("\nOptions:");
        System.out.println("1. View Project Details");
        System.out.println("2. Return to View Projects Menu");
        
        System.out.print("\nEnter your choice: ");
        String choice = sc.nextLine();
        
        if (choice.equals("1")) {
            System.out.print("Enter project number to view details: ");
            try {
                int projectIndex = Integer.parseInt(sc.nextLine()) - 1;
                if (projectIndex >= 0 && projectIndex < projects.size()) {
                    viewProjectDetails(projects.get(projectIndex));
                } else {
                    System.out.println("Invalid project number. Press Enter to continue.");
                    waitForEnter();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Press Enter to continue.");
                waitForEnter();
            }
        }
    }
    
    /**
     * View detailed information about a project
     * @param project the project to view
     */
    private void viewProjectDetails(Project project) {
        ScreenUtil.clearScreen();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.println("\n===== Project Details =====");
        System.out.println("Project ID: " + project.getProjectID());
        System.out.println("Project Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Visibility: " + (project.isVisible() ? "Visible" : "Hidden"));
        System.out.println("Application Period: " + 
                         dateFormat.format(project.getApplicationOpenDate()) + " to " + 
                         dateFormat.format(project.getApplicationCloseDate()));
        System.out.println("Manager ID: " + project.getManagerInCharge().getManagerID());
        
        System.out.println("\nFlat Types:");
        for (FlatType type : project.getFlatTypes()) {
            System.out.println("- " + type.getDisplayValue() + ": " + 
                             project.getAvailableUnitsByType(type) + " units available out of " + 
                             project.getTotalUnitsByType(type));
        }
        
        System.out.println("\nOfficer Slots: " + 
                         project.getAvailableOfficerSlots() + " available out of " + 
                         (project.getAvailableOfficerSlots() + project.getOfficers().size()));
        
        System.out.println("\nAssigned Officers: " + project.getOfficers().size());
        List<HDBOfficer> officers = project.getOfficers();
        if (!officers.isEmpty()) {
            for (int i = 0; i < officers.size(); i++) {
                System.out.println((i + 1) + ". " + officers.get(i).getName() + 
                                 " (" + officers.get(i).getOfficerID() + ")");
            }
        }
        
        waitForEnter();
    }
    
    /**
     * Calculate total units across all flat types
     * @param project the project
     * @return total units
     */
    private int calculateTotalUnits(Project project) {
        int total = 0;
        for (FlatType type : project.getFlatTypes()) {
            total += project.getTotalUnitsByType(type);
        }
        return total;
    }
    
    /**
     * View manager's profile
     */
    private void viewProfile() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== My Profile =====");
        System.out.println("Name: " + currentUser.getName());
        System.out.println("NRIC: " + currentUser.getNRIC());
        System.out.println("Age: " + currentUser.getAge());
        System.out.println("Marital Status: " + currentUser.getMaritalStatusDisplayValue());
        System.out.println("Role: " + currentUser.getRole());
        System.out.println("Manager ID: " + currentUser.getManagerID());
        
        List<Project> managedProjects = currentUser.getManagedProjects();
        if (!managedProjects.isEmpty()) {
            System.out.println("\nProjects I'm Managing:");
            for (Project project : managedProjects) {
                System.out.println("- " + project.getProjectName() + " (ID: " + project.getProjectID() + ")");
            }
        } else {
            System.out.println("\nNot currently managing any projects.");
        }
        
        waitForEnter();
    }
    
    /**
     * Change password
     */
    private void changePassword() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Change Password =====");
        System.out.print("Enter current password: ");
        String currentPassword = sc.nextLine();
        
        if (!currentUser.getPassword().equals(currentPassword)) {
            System.out.println("Incorrect password. Password change cancelled.");
            waitForEnter();
            return;
        }
        
        System.out.print("Enter new password: ");
        String newPassword = sc.nextLine();
        
        System.out.print("Confirm new password: ");
        String confirmPassword = sc.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match. Password change cancelled.");
            waitForEnter();
            return;
        }
        
        // Change password
        currentUser.setPassword(newPassword);
        System.out.println("Password changed successfully!");
        waitForEnter();
    }
    
    /**
     * Wait for user to press Enter to continue
     */
    private void waitForEnter() {
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Truncate a string to a maximum length
     * @param str the string to truncate
     * @param maxLength the maximum length
     * @return truncated string
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Get valid input based on a predicate
     * @param prompt the prompt to display
     * @param validator the validation predicate
     * @param errorMessage the error message to display if validation fails
     * @return the valid input string
     */
    private String getValidInput(String prompt, Predicate<String> validator, String errorMessage) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine();
            
            if (validator.test(input)) {
                return input;
            } else {
                System.out.println(errorMessage);
            }
        }
    }
    
    /**
     * Get valid integer input
     * @param prompt the prompt to display
     * @param validator the validation predicate
     * @param errorMessage the error message to display if validation fails
     * @return the valid integer
     */
    private int getValidIntInput(String prompt, Predicate<Integer> validator, String errorMessage) {
        while (true) {
            System.out.print(prompt);
            try {
                int input = Integer.parseInt(sc.nextLine());
                
                if (validator.test(input)) {
                    return input;
                } else {
                    System.out.println(errorMessage);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Please enter a valid integer.");
            }
        }
    }
    
    /**
     * Get valid date input
     * @param prompt the prompt to display
     * @param dateFormat the date format to use
     * @return the valid date
     */
    private Date getValidDateInput(String prompt, SimpleDateFormat dateFormat) {
        while (true) {
            System.out.print(prompt);
            try {
                return dateFormat.parse(sc.nextLine());
            } catch (ParseException e) {
                System.out.println("Invalid date format. Please use format: " + 
                                  ((SimpleDateFormat) dateFormat.clone()).toPattern());
            }
        }
    }
    
    /**
     * Get input with a prompt
     * @param prompt the prompt to display
     * @return the input string
     */
    private String getInputWithPrompt(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }
}