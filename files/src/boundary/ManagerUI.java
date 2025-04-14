package boundary;

import control.ApplicationControl;
import control.EnquiryControl;
import control.HDBManagerControl;
import control.ReportControl;
import entity.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import utils.ScreenUtil;

/**
 * UI class for HDB Manager operations in the BTO Management System.
 * Demonstrates the Boundary pattern in MVC architecture.
 */
public class ManagerUI {
    private final HDBManager currentUser;
    private final Scanner sc;
    private final HDBManagerControl managerControl;
    private final ApplicationControl applicationControl;
    private final EnquiryControl enquiryControl;
    private final ReportControl reportControl;
    
    /**
     * Constructor for ManagerUI
     * @param user the logged-in HDB Manager
     */
    public ManagerUI(HDBManager user) {
        this.currentUser = user;
        this.sc = new Scanner(System.in);
        this.managerControl = new HDBManagerControl();
        this.applicationControl = new ApplicationControl();
        this.enquiryControl = new EnquiryControl();
        this.reportControl = new ReportControl();
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
            System.out.println("1. Manage Projects");
            System.out.println("2. View Projects");
            System.out.println("3. Manage Officer Registrations");
            System.out.println("4. Manage Applications");
            System.out.println("5. Manage Withdrawal Requests");
            System.out.println("6. View & Reply to Enquiries");
            System.out.println("7. Generate Reports");
            System.out.println("8. View My Profile");
            System.out.println("9. Change Password");
            System.out.println("10. Sign Out");
            
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
                    manageOfficerRegistrations();
                    break;
                case "4":
                    manageApplications();
                    break;
                case "5":
                    manageWithdrawalRequests();
                    break;
                case "6":
                    viewAndReplyToEnquiries();
                    break;
                case "7":
                    generateReports();
                    break;
                case "8":
                    viewProfile();
                    break;
                case "9":
                    changePassword();
                    break;
                case "10":
                    exit = true;
                    System.out.println("Signing out...");
                    break;
                default:
                    System.out.println("Invalid choice. Press Enter to continue.");
                    sc.nextLine();
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
                    sc.nextLine();
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
                sc.nextLine();
                return;
            }
            
            // Get application dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date openDate = getValidDateInput("Application Opening Date (dd/MM/yyyy): ", dateFormat);
            Date closeDate = getValidDateInput("Application Closing Date (dd/MM/yyyy): ", dateFormat);
            
            // Check date validity
            if (closeDate.before(openDate)) {
                System.out.println("Closing date cannot be before opening date. Press Enter to continue.");
                sc.nextLine();
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
            
            // Create the project
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
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Edit an existing project
     */
    private void editProject() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Edit Existing Project =====");
        
        // Get projects managed by this manager
        List<Project> managedProjects = currentUser.getManagedProjects();
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
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
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Delete a project
     */
    private void deleteProject() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Delete Project =====");
        
        // Get projects managed by this manager
        List<Project> managedProjects = currentUser.getManagedProjects();
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
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
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Toggle project visibility
     */
    private void toggleProjectVisibility() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Toggle Project Visibility =====");
        
        // Get all projects managed by this manager
        List<Project> managedProjects = currentUser.getManagedProjects();
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
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
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
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
                    sc.nextLine();
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
            System.out.println("Press Enter to continue...");
            sc.nextLine();
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
        
        List<Project> myProjects = currentUser.getManagedProjects();
        
        if (myProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
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
                         "No.", "Project Name", "Neighborhood", "Visibility", "Closing Date", "Total Units", "Manager");
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
                    sc.nextLine();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Press Enter to continue.");
                sc.nextLine();
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
        System.out.println("Manager: " + project.getManagerInCharge().getName());
        
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
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
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
     * Manage officer registrations (view, approve, reject)
     */
    private void manageOfficerRegistrations() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Manage Officer Registrations =====");
        
        // Get all projects managed by this manager
        List<Project> managedProjects = currentUser.getManagedProjects();
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display the list of managed projects
        System.out.println("Select a project to manage officer registrations:");
        for (int i = 0; i < managedProjects.size(); i++) {
            Project p = managedProjects.get(i);
            System.out.println((i + 1) + ". " + p.getProjectName() + 
                             " (Slots: " + p.getAvailableOfficerSlots() + 
                             " available out of " + 
                             (p.getAvailableOfficerSlots() + p.getOfficers().size()) + ")");
        }
        
        // Get project selection with validation
        int projectChoice = getValidIntInput("\nEnter project number (0 to cancel): ", 
            input -> input >= 0 && input <= managedProjects.size(), 
            "Invalid project number.");
        
        if (projectChoice == 0) {
            return;
        }
        
        Project selectedProject = managedProjects.get(projectChoice - 1);
        
        // Get officer registrations for the selected project
        List<Map<String, Object>> registrations = managerControl.getOfficerRegistrations(selectedProject);
        
        if (registrations.isEmpty()) {
            System.out.println("\nNo pending officer registrations for this project.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display pending registrations
        while (true) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Officer Registrations for " + selectedProject.getProjectName() + " =====");
            System.out.println("Available Slots: " + selectedProject.getAvailableOfficerSlots());
            
            List<Map<String, Object>> pendingRegistrations = getPendingRegistrations(registrations);
            
            if (pendingRegistrations.isEmpty()) {
                System.out.println("\nNo pending officer registrations for this project.");
                System.out.println("Press Enter to continue...");
                sc.nextLine();
                return;
            }
            
            System.out.printf("%-5s %-20s %-15s %-15s\n", 
                            "No.", "Officer Name", "Officer ID", "Registration Date");
            System.out.println("----------------------------------------------------------");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            
            for (int i = 0; i < pendingRegistrations.size(); i++) {
                Map<String, Object> reg = pendingRegistrations.get(i);
                HDBOfficer officer = (HDBOfficer) reg.get("officer");
                Date regDate = (Date) reg.get("date");
                
                System.out.printf("%-5d %-20s %-15s %-15s\n", 
                                (i + 1),
                                truncateString(officer.getName(), 20),
                                officer.getOfficerID(),
                                dateFormat.format(regDate));
            }
            
            System.out.println("\nOptions:");
            System.out.println("1. Approve Registration");
            System.out.println("2. Reject Registration");
            System.out.println("3. Return to Main Menu");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            if (choice.equals("1") || choice.equals("2")) {
                int regIndex = getValidIntInput("Enter registration number: ", 
                    input -> input >= 1 && input <= pendingRegistrations.size(), 
                    "Invalid registration number.");
                
                Map<String, Object> selectedReg = pendingRegistrations.get(regIndex - 1);
                HDBOfficer officer = (HDBOfficer) selectedReg.get("officer");
                
                if (choice.equals("1")) {
                    // Check if there are available slots
                    if (selectedProject.getAvailableOfficerSlots() <= 0) {
                        System.out.println("No available officer slots. Cannot approve registration.");
                        System.out.println("Press Enter to continue...");
                        sc.nextLine();
                        continue;
                    }
                    
                    // Approve registration
                    boolean success = managerControl.processOfficerRegistration(
                        currentUser, officer, selectedProject, true);
                    
                    if (success) {
                        System.out.println("Registration approved successfully!");
                        // Update registration status
                        selectedReg.put("status", RegistrationStatus.APPROVED);
                    } else {
                        System.out.println("Failed to approve registration.");
                    }
                } else {
                    // Reject registration
                    boolean success = managerControl.processOfficerRegistration(
                        currentUser, officer, selectedProject, false);
                    
                    if (success) {
                        System.out.println("Registration rejected successfully!");
                        // Update registration status
                        selectedReg.put("status", RegistrationStatus.REJECTED);
                    } else {
                        System.out.println("Failed to reject registration.");
                    }
                }
                
                System.out.println("Press Enter to continue...");
                sc.nextLine();
                
            } else if (choice.equals("3")) {
                break;
            } else {
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
            }
        }
    }
    
    /**
     * Get pending registrations from a list of registrations
     * @param registrations the list of all registrations
     * @return list of pending registrations
     */
    private List<Map<String, Object>> getPendingRegistrations(List<Map<String, Object>> registrations) {
        List<Map<String, Object>> pendingRegistrations = new ArrayList<>();
        
        for (Map<String, Object> reg : registrations) {
            if (reg.get("status") == RegistrationStatus.PENDING) {
                pendingRegistrations.add(reg);
            }
        }
        
        return pendingRegistrations;
    }
    
    /**
     * Manage applications (approve, reject)
     */
    private void manageApplications() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Manage Applications =====");
        
        // Get all projects managed by this manager
        List<Project> managedProjects = currentUser.getManagedProjects();
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display the list of managed projects
        System.out.println("Select a project to manage applications:");
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
        
        // Get pending applications for the selected project
        List<Application> pendingApplications = applicationControl.getPendingApplications(selectedProject);
        
        if (pendingApplications.isEmpty()) {
            System.out.println("\nNo pending applications for this project.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Show available units
        System.out.println("\nAvailable Units in " + selectedProject.getProjectName() + ":");
        for (FlatType type : selectedProject.getFlatTypes()) {
            System.out.println("- " + type.getDisplayValue() + ": " + 
                             selectedProject.getAvailableUnitsByType(type) + " units available");
        }
        
        // Display pending applications
        while (true) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Pending Applications for " + selectedProject.getProjectName() + " =====");
            
            System.out.printf("%-5s %-20s %-10s %-15s %-15s\n", 
                            "No.", "Applicant Name", "Age", "Marital Status", "Application Date");
            System.out.println("------------------------------------------------------------------");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            
            for (int i = 0; i < pendingApplications.size(); i++) {
                Application app = pendingApplications.get(i);
                Applicant applicant = app.getApplicant();
                
                System.out.printf("%-5d %-20s %-10d %-15s %-15s\n", 
                                (i + 1),
                                truncateString(applicant.getName(), 20),
                                applicant.getAge(),
                                applicant.getMaritalStatusDisplayValue(),
                                dateFormat.format(app.getApplicationDate()));
            }
            
            System.out.println("\nOptions:");
            System.out.println("1. Approve Application");
            System.out.println("2. Reject Application");
            System.out.println("3. View Application Details");
            System.out.println("4. Return to Main Menu");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            if (choice.equals("1") || choice.equals("2")) {
                int appIndex = getValidIntInput("Enter application number: ", 
                    input -> input >= 1 && input <= pendingApplications.size(), 
                    "Invalid application number.");
                
                Application selectedApp = pendingApplications.get(appIndex - 1);
                
                if (choice.equals("1")) {
                    // Approve application
                    if (processApplication(selectedApp, true)) {
                        // Remove from pending list
                        pendingApplications.remove(appIndex - 1);
                    }
                } else {
                    // Reject application
                    if (processApplication(selectedApp, false)) {
                        // Remove from pending list
                        pendingApplications.remove(appIndex - 1);
                    }
                }
                
                if (pendingApplications.isEmpty()) {
                    System.out.println("No more pending applications.");
                    System.out.println("Press Enter to continue...");
                    sc.nextLine();
                    break;
                }
                
            } else if (choice.equals("3")) {
                int appIndex = getValidIntInput("Enter application number: ", 
                    input -> input >= 1 && input <= pendingApplications.size(), 
                    "Invalid application number.");
                
                viewApplicationDetails(pendingApplications.get(appIndex - 1));
                
            } else if (choice.equals("4")) {
                break;
            } else {
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
            }
        }
    }
    
    /**
     * Process an application (approve or reject)
     * @param application the application to process
     * @param approve true to approve, false to reject
     * @return true if processed successfully, false otherwise
     */
    private boolean processApplication(Application application, boolean approve) {
        Project project = application.getProject();
        
        if (approve) {
            // Check if there are available units of the appropriate type
            // For a married couple, we need to determine which flat type they want
            FlatType requestedType = determineFlatType(application);
            
            // Check if there are available units
            if (project.getAvailableUnitsByType(requestedType) <= 0) {
                System.out.println("No available " + requestedType.getDisplayValue() + 
                                 " units. Cannot approve application.");
                System.out.println("Press Enter to continue...");
                sc.nextLine();
                return false;
            }
            
            // Approve the application
            application.setStatus(ApplicationStatus.SUCCESSFUL);
            
            // Decrement available units
            project.decrementAvailableUnits(requestedType);
            
            System.out.println("Application approved successfully!");
            System.out.println("Applicant: " + application.getApplicant().getName());
            System.out.println("New status: " + application.getStatus().getDisplayValue());
            
        } else {
            // Reject the application
            application.setStatus(ApplicationStatus.UNSUCCESSFUL);
            
            System.out.println("Application rejected successfully!");
            System.out.println("Applicant: " + application.getApplicant().getName());
            System.out.println("New status: " + application.getStatus().getDisplayValue());
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
        return true;
    }
    
    /**
     * Determine the flat type being requested based on marital status
     * @param application the application
     * @return the requested flat type
     */
    private FlatType determineFlatType(Application application) {
        Applicant applicant = application.getApplicant();
        
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            // Singles can only apply for 2-Room
            return FlatType.TWO_ROOM;
        } else {
            // For married couples, ask which type they want
            System.out.println("\nApplicant is eligible for both 2-Room and 3-Room flats.");
            System.out.println("Determine which flat type they are applying for:");
            System.out.println("1. 2-Room");
            System.out.println("2. 3-Room");
            
            int choice = getValidIntInput("\nEnter choice: ", 
                input -> input == 1 || input == 2, 
                "Invalid choice. Please enter 1 or 2.");
            
            return choice == 1 ? FlatType.TWO_ROOM : FlatType.THREE_ROOM;
        }
    }
    
    /**
     * View details of a specific application
     * @param application the application to view
     */
    private void viewApplicationDetails(Application application) {
        ScreenUtil.clearScreen();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.println("\n===== Application Details =====");
        System.out.println("Application ID: " + application.getApplicationID());
        System.out.println("Project: " + application.getProject().getProjectName());
        System.out.println("Application Date: " + dateFormat.format(application.getApplicationDate()));
        System.out.println("Status: " + application.getStatus().getDisplayValue());
        System.out.println("Last Updated: " + dateFormat.format(application.getStatusUpdateDate()));
        
        System.out.println("\nApplicant Details:");
        Applicant applicant = application.getApplicant();
        System.out.println("Name: " + applicant.getName());
        System.out.println("NRIC: " + applicant.getNRIC());
        System.out.println("Age: " + applicant.getAge());
        System.out.println("Marital Status: " + applicant.getMaritalStatusDisplayValue());
        
        // Eligibility information
        System.out.println("\nEligibility:");
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            if (applicant.getAge() >= 35) {
                System.out.println("Single applicant, 35 years or older: Eligible for 2-Room only");
            } else {
                System.out.println("Single applicant, under 35: Not eligible");
            }
        } else if (applicant.getMaritalStatus() == MaritalStatus.MARRIED) {
            if (applicant.getAge() >= 21) {
                System.out.println("Married applicant, 21 years or older: Eligible for 2-Room and 3-Room");
            } else {
                System.out.println("Married applicant, under 21: Not eligible");
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Manage withdrawal requests
     */
    private void manageWithdrawalRequests() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Manage Withdrawal Requests =====");
        
        // Get all projects managed by this manager
        List<Project> managedProjects = currentUser.getManagedProjects();
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display the list of managed projects
        System.out.println("Select a project to manage withdrawal requests:");
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
        
        // Get withdrawal requests for the selected project
        List<Application> withdrawalRequests = applicationControl.getWithdrawalRequests(selectedProject);
        
        if (withdrawalRequests.isEmpty()) {
            System.out.println("\nNo pending withdrawal requests for this project.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display withdrawal requests
        while (true) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Withdrawal Requests for " + selectedProject.getProjectName() + " =====");
            
            System.out.printf("%-5s %-20s %-15s %-15s %-15s\n", 
                            "No.", "Applicant Name", "Application Date", "Status", "Last Updated");
            System.out.println("-------------------------------------------------------------------------------");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            
            for (int i = 0; i < withdrawalRequests.size(); i++) {
                Application app = withdrawalRequests.get(i);
                
                System.out.printf("%-5d %-20s %-15s %-15s %-15s\n", 
                                (i + 1),
                                truncateString(app.getApplicant().getName(), 20),
                                dateFormat.format(app.getApplicationDate()),
                                app.getStatus().getDisplayValue(),
                                dateFormat.format(app.getStatusUpdateDate()));
            }
            
            System.out.println("\nOptions:");
            System.out.println("1. Approve Withdrawal");
            System.out.println("2. Reject Withdrawal");
            System.out.println("3. View Application Details");
            System.out.println("4. Return to Main Menu");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            if (choice.equals("1") || choice.equals("2")) {
                int reqIndex = getValidIntInput("Enter request number: ", 
                    input -> input >= 1 && input <= withdrawalRequests.size(), 
                    "Invalid request number.");
                
                Application selectedReq = withdrawalRequests.get(reqIndex - 1);
                
                if (choice.equals("1")) {
                    // Approve withdrawal
                    boolean success = managerControl.processWithdrawalRequest(currentUser, selectedReq, true);
                    
                    if (success) {
                        System.out.println("Withdrawal approved successfully!");
                        System.out.println("Applicant: " + selectedReq.getApplicant().getName());
                        System.out.println("New status: " + selectedReq.getStatus().getDisplayValue());
                        
                        // Remove from list
                        withdrawalRequests.remove(reqIndex - 1);
                        
                        if (withdrawalRequests.isEmpty()) {
                            System.out.println("No more pending withdrawal requests.");
                            System.out.println("Press Enter to continue...");
                            sc.nextLine();
                            break;
                        }
                    } else {
                        System.out.println("Failed to approve withdrawal.");
                        System.out.println("Press Enter to continue...");
                        sc.nextLine();
                    }
                } else {
                    // Reject withdrawal
                    boolean success = managerControl.processWithdrawalRequest(currentUser, selectedReq, false);
                    
                    if (success) {
                        System.out.println("Withdrawal rejected successfully!");
                        
                        // Remove from list
                        withdrawalRequests.remove(reqIndex - 1);
                        
                        if (withdrawalRequests.isEmpty()) {
                            System.out.println("No more pending withdrawal requests.");
                            System.out.println("Press Enter to continue...");
                            sc.nextLine();
                            break;
                        }
                    } else {
                        System.out.println("Failed to reject withdrawal.");
                    }
                    
                    System.out.println("Press Enter to continue...");
                    sc.nextLine();
                }
                
            } else if (choice.equals("3")) {
                int reqIndex = getValidIntInput("Enter request number: ", 
                    input -> input >= 1 && input <= withdrawalRequests.size(), 
                    "Invalid request number.");
                
                viewApplicationDetails(withdrawalRequests.get(reqIndex - 1));
                
            } else if (choice.equals("4")) {
                break;
            } else {
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
            }
        }
    }
    
    /**
     * View and reply to enquiries
     */
    private void viewAndReplyToEnquiries() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== View & Reply to Enquiries =====");
        
        // Get all projects managed by this manager
        List<Project> managedProjects = currentUser.getManagedProjects();
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display the list of managed projects
        System.out.println("Select a project to view enquiries:");
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
        
        // Display enquiries for the selected project
        List<Enquiry> enquiries = enquiryControl.getEnquiriesForProject(selectedProject);
        
        if (enquiries.isEmpty()) {
            System.out.println("\nNo enquiries found for " + selectedProject.getProjectName());
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        
        while (true) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Enquiries for " + selectedProject.getProjectName() + " =====");
            System.out.printf("%-5s %-20s %-20s %-30s\n",
                    "No.", "Applicant", "Date", "Content");
            System.out.println("--------------------------------------------------------------------------------");
            
            for (int i = 0; i < enquiries.size(); i++) {
                Enquiry enquiry = enquiries.get(i);
                System.out.printf("%-5d %-20s %-20s %-30s\n",
                        (i + 1),
                        truncateString(enquiry.getApplicant().getName(), 20),
                        dateFormat.format(enquiry.getSubmissionDate()),
                        truncateString(enquiry.getContent(), 30));
            }
            
            System.out.println("\nOptions:");
            System.out.println("1. View Enquiry Details");
            System.out.println("2. Return to Main Menu");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            if (choice.equals("1")) {
                int enquiryIndex = getValidIntInput("Enter enquiry number to view details: ", 
                                                   input -> input >= 1 && input <= enquiries.size(), 
                                                   "Invalid enquiry number.");
                viewAndReplyToEnquiry(enquiries.get(enquiryIndex - 1));
                
                // Instead of reassigning to enquiries, create a new variable
                List<Enquiry> updatedEnquiries = enquiryControl.getEnquiriesForProject(selectedProject);
                enquiries.clear();  // Clear the existing list
                enquiries.addAll(updatedEnquiries);  // Add all new enquiries to the existing list
            
            } else if (choice.equals("2")) {
                break;
            } else {
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
            }
        }
    }
    
    /**
     * View and reply to a specific enquiry
     * @param enquiry the enquiry
     */
    private void viewAndReplyToEnquiry(Enquiry enquiry) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        
        while (true) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Enquiry Details =====");
            System.out.println("Enquiry ID: " + enquiry.getEnquiryID());
            System.out.println("From: " + enquiry.getApplicant().getName() + " (" +
                    enquiry.getApplicant().getNRIC() + ")");
            System.out.println("Project: " + enquiry.getProject().getProjectName());
            System.out.println("Date: " + dateFormat.format(enquiry.getSubmissionDate()));
            System.out.println("\nContent:");
            System.out.println(enquiry.getContent());
            
            List<String> replies = enquiry.getReplies();
            if (!replies.isEmpty()) {
                System.out.println("\nReplies:");
                for (int i = 0; i < replies.size(); i++) {
                    System.out.println((i + 1) + ". " + replies.get(i));
                }
            }
            
            System.out.println("\nOptions:");
            System.out.println("1. Reply to Enquiry");
            System.out.println("2. Return to Enquiry List");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            if (choice.equals("1")) {
                System.out.println("\nEnter your reply:");
                String reply = sc.nextLine();
                
                if (!reply.trim().isEmpty()) {
                    // Add reply to the enquiry
                    enquiryControl.addReply(enquiry, reply, currentUser);
                    System.out.println("Reply submitted successfully!");
                } else {
                    System.out.println("Reply cannot be empty.");
                }
                
                System.out.println("Press Enter to continue...");
                sc.nextLine();
            } else if (choice.equals("2")) {
                break;
            } else {
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
            }
        }
    }
    
    /**
     * Generate reports
     */
    private void generateReports() {
        boolean done = false;
        
        while (!done) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Generate Reports =====");
            System.out.println("1. Generate Project Application Report");
            System.out.println("2. Generate Flat Booking Report");
            System.out.println("3. Return to Main Menu");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            switch (choice) {
                case "1":
                    generateApplicationReport();
                    break;
                case "2":
                    generateBookingReport();
                    break;
                case "3":
                    done = true;
                    break;
                default:
                    System.out.println("Invalid choice. Press Enter to continue.");
                    sc.nextLine();
            }
        }
    }
    
    /**
     * Generate an application report
     */
    private void generateApplicationReport() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Generate Application Report =====");
        
        // Get all projects managed by this manager
        List<Project> managedProjects = currentUser.getManagedProjects();
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display the list of managed projects
        System.out.println("Select a project for the report:");
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
        
        // Get applications for the selected project
        List<Application> applications = applicationControl.getAllApplications(selectedProject);
        
        if (applications.isEmpty()) {
            System.out.println("\nNo applications found for " + selectedProject.getProjectName());
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Apply filters
        Map<String, Object> filters = new HashMap<>();
        
        System.out.println("\nApply filters (leave blank to skip):");
        
        // Marital status filter
        System.out.print("Filter by Marital Status (Single/Married): ");
        String maritalStatus = sc.nextLine().trim();
        if (!maritalStatus.isEmpty()) {
            filters.put("maritalStatus", maritalStatus);
        }
        
        // Age range filter
        System.out.print("Filter by Minimum Age: ");
        String minAgeStr = sc.nextLine().trim();
        if (!minAgeStr.isEmpty()) {
            try {
                int minAge = Integer.parseInt(minAgeStr);
                filters.put("minAge", minAge);
            } catch (NumberFormatException e) {
                System.out.println("Invalid age input. Filter not applied.");
            }
        }
        
        System.out.print("Filter by Maximum Age: ");
        String maxAgeStr = sc.nextLine().trim();
        if (!maxAgeStr.isEmpty()) {
            try {
                int maxAge = Integer.parseInt(maxAgeStr);
                filters.put("maxAge", maxAge);
            } catch (NumberFormatException e) {
                System.out.println("Invalid age input. Filter not applied.");
            }
        }
        
        // Status filter
        System.out.print("Filter by Application Status (Pending/Successful/Unsuccessful/Booked): ");
        String status = sc.nextLine().trim();
        if (!status.isEmpty()) {
            filters.put("status", status);
        }
        
        // Generate the report
        Report report = reportControl.generateApplicationReport(selectedProject, applications, filters);
        
        if (report != null) {
            // Display the report
            displayApplicationReport(report, selectedProject);
        } else {
            System.out.println("\nFailed to generate report.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
        }
    }
    
    /**
     * Display an application report
     * @param report the report to display
     * @param project the project for the report
     */
    private void displayApplicationReport(Report report, Project project) {
        ScreenUtil.clearScreen();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.println("\n============================================================");
        System.out.println("                APPLICATION REPORT                         ");
        System.out.println("============================================================");
        System.out.println("Project: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Report Date: " + dateFormat.format(report.getGenerationDate()));
        System.out.println("Report ID: " + report.getReportID());
        
        // Display filter criteria
        System.out.println("\nFilter Criteria:");
        Map<String, Object> criteria = report.getCriteria();
        if (criteria.isEmpty()) {
            System.out.println("- No filters applied");
        } else {
            for (Map.Entry<String, Object> entry : criteria.entrySet()) {
                System.out.println("- " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        // Get the filtered applications
        List<Application> filteredApplications = report.getApplications();
        
        // Display summary statistics
        System.out.println("\nSummary:");
        System.out.println("Total Applications: " + filteredApplications.size());
        
        // Count by status
        Map<ApplicationStatus, Integer> statusCounts = countByStatus(filteredApplications);
        
        System.out.println("\nApplications by Status:");
        for (Map.Entry<ApplicationStatus, Integer> entry : statusCounts.entrySet()) {
            System.out.println("- " + entry.getKey().getDisplayValue() + ": " + entry.getValue());
        }
        
        // Count by marital status
        Map<MaritalStatus, Integer> maritalCounts = countByMaritalStatus(filteredApplications);
        
        System.out.println("\nApplicants by Marital Status:");
        for (Map.Entry<MaritalStatus, Integer> entry : maritalCounts.entrySet()) {
            System.out.println("- " + entry.getKey().getDisplayValue() + ": " + entry.getValue());
        }
        
        // Display detailed list
        System.out.println("\nDetailed Application List:");
        System.out.printf("%-5s %-20s %-10s %-15s %-15s %-15s\n", 
                         "No.", "Applicant Name", "Age", "Marital Status", "Status", "Application Date");
        System.out.println("-------------------------------------------------------------------------------");
        
        for (int i = 0; i < filteredApplications.size(); i++) {
            Application app = filteredApplications.get(i);
            Applicant applicant = app.getApplicant();
            
            System.out.printf("%-5d %-20s %-10d %-15s %-15s %-15s\n", 
                            (i + 1),
                            truncateString(applicant.getName(), 20),
                            applicant.getAge(),
                            applicant.getMaritalStatusDisplayValue(),
                            app.getStatus().getDisplayValue(),
                            dateFormat.format(app.getApplicationDate()));
        }
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Count applications by status
     * @param applications the applications to count
     * @return map of status to count
     */
    private Map<ApplicationStatus, Integer> countByStatus(List<Application> applications) {
        Map<ApplicationStatus, Integer> counts = new HashMap<>();
        
        for (Application app : applications) {
            ApplicationStatus status = app.getStatus();
            counts.put(status, counts.getOrDefault(status, 0) + 1);
        }
        
        return counts;
    }
    
    /**
     * Count applications by marital status
     * @param applications the applications to count
     * @return map of marital status to count
     */
    private Map<MaritalStatus, Integer> countByMaritalStatus(List<Application> applications) {
        Map<MaritalStatus, Integer> counts = new HashMap<>();
        
        for (Application app : applications) {
            MaritalStatus status = app.getApplicant().getMaritalStatus();
            counts.put(status, counts.getOrDefault(status, 0) + 1);
        }
        
        return counts;
    }
    /**
     * Generate a flat booking report
     */
    private void generateBookingReport() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Generate Flat Booking Report =====");
        
        // Get all projects managed by this manager
        List<Project> managedProjects = currentUser.getManagedProjects();
        
        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Display the list of managed projects
        System.out.println("Select a project for the report:");
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
        
        // Get booked applications for the selected project
        List<Application> bookedApplications = applicationControl.getBookedApplications(selectedProject);
        
        if (bookedApplications.isEmpty()) {
            System.out.println("\nNo bookings found for " + selectedProject.getProjectName());
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Apply filters
        Map<String, Object> filters = new HashMap<>();
        
        System.out.println("\nApply filters (leave blank to skip):");
        
        // Marital status filter
        System.out.print("Filter by Marital Status (Single/Married): ");
        String maritalStatus = sc.nextLine().trim();
        if (!maritalStatus.isEmpty()) {
            filters.put("maritalStatus", maritalStatus);
        }
        
        // Flat type filter
        System.out.print("Filter by Flat Type (2-Room/3-Room): ");
        String flatType = sc.nextLine().trim();
        if (!flatType.isEmpty()) {
            filters.put("flatType", flatType);
        }
        
        // Generate the report
        Report report = reportControl.generateBookingReport(selectedProject, bookedApplications, filters);
        
        if (report != null) {
            // Display the report
            displayBookingReport(report, selectedProject);
        } else {
            System.out.println("\nFailed to generate report.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
        }
    }
    
    /**
     * Display a booking report
     * @param report the report to display
     * @param project the project for the report
     */
    private void displayBookingReport(Report report, Project project) {
        ScreenUtil.clearScreen();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.println("\n============================================================");
        System.out.println("                FLAT BOOKING REPORT                         ");
        System.out.println("============================================================");
        System.out.println("Project: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Report Date: " + dateFormat.format(report.getGenerationDate()));
        System.out.println("Report ID: " + report.getReportID());
        
        // Display filter criteria
        System.out.println("\nFilter Criteria:");
        Map<String, Object> criteria = report.getCriteria();
        if (criteria.isEmpty()) {
            System.out.println("- No filters applied");
        } else {
            for (Map.Entry<String, Object> entry : criteria.entrySet()) {
                System.out.println("- " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        // Get the filtered applications
        List<Application> filteredApplications = report.getApplications();
        
        // Display summary statistics
        System.out.println("\nSummary:");
        System.out.println("Total Bookings: " + filteredApplications.size());
        
        // Count by flat type
        Map<FlatType, Integer> flatTypeCounts = countByFlatType(filteredApplications);
        
        System.out.println("\nBookings by Flat Type:");
        for (Map.Entry<FlatType, Integer> entry : flatTypeCounts.entrySet()) {
            System.out.println("- " + entry.getKey().getDisplayValue() + ": " + entry.getValue());
        }
        
        // Count by marital status
        Map<MaritalStatus, Integer> maritalCounts = countByMaritalStatus(filteredApplications);
        
        System.out.println("\nApplicants by Marital Status:");
        for (Map.Entry<MaritalStatus, Integer> entry : maritalCounts.entrySet()) {
            System.out.println("- " + entry.getKey().getDisplayValue() + ": " + entry.getValue());
        }
        
        // Display detailed list
        System.out.println("\nDetailed Booking List:");
        System.out.printf("%-5s %-20s %-10s %-15s %-15s %-15s\n", 
                         "No.", "Applicant Name", "Age", "Marital Status", "Flat Type", "Flat ID");
        System.out.println("-------------------------------------------------------------------------------");
        
        for (int i = 0; i < filteredApplications.size(); i++) {
            Application app = filteredApplications.get(i);
            Applicant applicant = app.getApplicant();
            Flat flat = app.getBookedFlat();
            
            if (flat != null) {
                System.out.printf("%-5d %-20s %-10d %-15s %-15s %-15s\n", 
                                (i + 1),
                                truncateString(applicant.getName(), 20),
                                applicant.getAge(),
                                applicant.getMaritalStatusDisplayValue(),
                                flat.getType().getDisplayValue(),
                                flat.getFlatID());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Count applications by flat type
     * @param applications the applications to count
     * @return map of flat type to count
     */
    private Map<FlatType, Integer> countByFlatType(List<Application> applications) {
        Map<FlatType, Integer> counts = new HashMap<>();
        
        for (Application app : applications) {
            Flat flat = app.getBookedFlat();
            if (flat != null) {
                FlatType type = flat.getType();
                counts.put(type, counts.getOrDefault(type, 0) + 1);
            }
        }
        
        return counts;
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
                System.out.println("- " + project.getProjectName());
            }
        } else {
            System.out.println("\nNot currently managing any projects.");
        }
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
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
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        System.out.print("Enter new password: ");
        String newPassword = sc.nextLine();
        
        System.out.print("Confirm new password: ");
        String confirmPassword = sc.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match. Password change cancelled.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Change password
        currentUser.setPassword(newPassword);
        System.out.println("Password changed successfully!");
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