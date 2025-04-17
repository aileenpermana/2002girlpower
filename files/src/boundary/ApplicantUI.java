package boundary;

import control.ApplicantControl;
import control.ApplicationControl;
import control.ProjectControl;
import entity.*;
import java.text.SimpleDateFormat;
import java.util.*;
import utils.ScreenUtil;


public class ApplicantUI {
    private Applicant currentUser;
    private Scanner sc;
    private final ApplicantControl applicantControl;
    private ProjectControl projectControl;
    private ApplicationControl applicationControl;
    
    // Store user's filter preferences
    private Map<String, String> filterPreferences;
    
    // Project sorting options
    private static final String SORT_BY_FLAT_TYPE = "1";
    private static final String SORT_BY_NEIGHBORHOOD = "2";
    private static final String SORT_BY_PRICE_RANGE = "3";
    private static final String SORT_BY_CLOSING_DATE = "4";
    private static final String SORT_BY_ALPHABETICAL = "5";
    private static final String SORT_BY_AVAILABILITY = "6";
    
    /**
     * Constructor for ApplicantUI
     */
    public ApplicantUI(Applicant user, ApplicantControl applicantControl, ApplicationControl applicationControl) {
        this.currentUser = user;
        this.sc = new Scanner(System.in);
        this.applicantControl = applicantControl;
        this.projectControl = new ProjectControl();
        this.applicationControl = new ApplicationControl();
        this.filterPreferences = new HashMap<>();
        
        // Set default sort preference
        filterPreferences.put("sortBy", SORT_BY_ALPHABETICAL);
    }
    
    
    public void displayMenu() {
        boolean exit = false;
        
        while (!exit) {
            ScreenUtil.clearScreen();
            System.out.println("\n===== Applicant Menu =====");
            System.out.println("Welcome, " + currentUser.getName() + "!");
            System.out.println("1. View Available Projects");
            System.out.println("2. View My Applications");
            System.out.println("3. View My Profile");
            System.out.println("4. Change Password");
            System.out.println("5. Sign Out");
            
            System.out.print("\nEnter your choice: ");
            String choice = sc.nextLine();
            
            switch (choice) {
                case "1":
                    viewAvailableProjects();
                    break;
                case "2":
                    viewMyApplications();
                    break;
                case "3":
                    viewProfile();
                    break;
                case "4":
                    changePassword();
                    break;
                case "5":
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
    * Reads the user's menu choice from input.
    * @return the trimmed user input as a string, or an empty string if an error occurs
    */
    private String readChoice() {
        try {
            return sc.nextLine().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * View all available projects based on eligibility and visibility
     */
    private void viewAvailableProjects() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== Available Projects =====");
        
        // Get projects based on applicant's eligibility
        List<Project> eligibleProjects = projectControl.getEligibleProjects(currentUser);
        
        if (eligibleProjects.isEmpty()) {
            System.out.println("No eligible projects available for you at this time.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Show filtering options
        displayFilterOptions();
        
        // Apply filters and sorting
        eligibleProjects = applyFiltersAndSort(eligibleProjects);
        
        // Display projects
        displayProjects(eligibleProjects);
        
        // Project selection submenu
        System.out.println("\nOptions:");
        System.out.println("1. View Project Details");
        System.out.println("2. Return to Main Menu");
        
        System.out.print("\nEnter your choice: ");
        String choice = readChoice();
        
        if (choice.equals("1")) {
            System.out.print("Enter project number to view details: ");
            try {
                int projectIndex = Integer.parseInt(sc.nextLine()) - 1;
                if (projectIndex >= 0 && projectIndex < eligibleProjects.size()) {
                    viewProjectDetails(eligibleProjects.get(projectIndex));
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
    
    
     // Display filtering and sorting options for projects
     
    private void displayFilterOptions() {
        System.out.println("\n----- Filter and Sort Options -----");
        System.out.println("Sort by:");
        System.out.println("1. Flat Type");
        System.out.println("2. Neighborhood");
        System.out.println("3. Price Range");
        System.out.println("4. Closing Date");
        System.out.println("5. Alphabetical (Default)");
        System.out.println("6. Availability (Descending)");
        
        System.out.print("\nEnter sort option (or press Enter to use previous/default): ");
        String sortOption = sc.nextLine().trim();
        
        if (!sortOption.trim().isEmpty()) {
            filterPreferences.put("sortBy", sortOption);
        }
        
        // Additional filters could be added here
        System.out.print("Filter by Neighborhood (leave blank for all): ");
        String neighborhood = sc.nextLine().trim();
        
        if (!neighborhood.trim().isEmpty()) {
            filterPreferences.put("neighborhood", neighborhood);
        } else {
            filterPreferences.remove("neighborhood");
        }
        
        System.out.print("Filter by Flat Type (2-Room/3-Room, leave blank for all): ");
        String flatType = sc.nextLine().trim();
        
        if (!flatType.trim().isEmpty()) {
            filterPreferences.put("flatType", flatType);
        } else {
            filterPreferences.remove("flatType");
        }
    }
    
    /**
     * Apply filters and sorting to a list of projects
     */
    private List<Project> applyFiltersAndSort(List<Project> projects) {
        List<Project> filteredProjects = new ArrayList<>(projects);
        
        // Apply neighborhood filter if specified
        if (filterPreferences.containsKey("neighborhood")) {
            String neighborhoodFilter = filterPreferences.get("neighborhood");
            filteredProjects.removeIf(p -> !p.getNeighborhood().equalsIgnoreCase(neighborhoodFilter));
        }
        
        // Apply flat type filter if specified
        if (filterPreferences.containsKey("flatType")) {
            String flatTypeFilter = filterPreferences.get("flatType");
            FlatType type = null;
            
            if (flatTypeFilter.equalsIgnoreCase("2-Room")) {
                type = FlatType.TWO_ROOM;
            } else if (flatTypeFilter.equalsIgnoreCase("3-Room")) {
                type = FlatType.THREE_ROOM;
            }
            
            if (type != null) {
                FlatType finalType = type;
                filteredProjects.removeIf(p -> !p.hasFlatType(finalType));
            }
        }
        
        // Apply sorting
        String sortBy = filterPreferences.getOrDefault("sortBy", SORT_BY_ALPHABETICAL);
        
        switch (sortBy) {
            case SORT_BY_FLAT_TYPE:
                // Sort by number of flat types
                filteredProjects.sort(Comparator.comparing(p -> p.getFlatTypes().size()));
                break;
            case SORT_BY_NEIGHBORHOOD:
                filteredProjects.sort(Comparator.comparing(Project::getNeighborhood));
                break;
            case SORT_BY_PRICE_RANGE:
                // This would require price data, using project ID as placeholder
                filteredProjects.sort(Comparator.comparing(Project::getProjectID));
                break;
            case SORT_BY_CLOSING_DATE:
                filteredProjects.sort(Comparator.comparing(Project::getApplicationCloseDate));
                break;
            case SORT_BY_AVAILABILITY:
                // Sort by total available units (descending)
                filteredProjects.sort((p1, p2) -> {
                    int p1Available = calculateTotalAvailableUnits(p1);
                    int p2Available = calculateTotalAvailableUnits(p2);
                    return Integer.compare(p2Available, p1Available); // Descending
                });
                break;
            case SORT_BY_ALPHABETICAL:
            default:
                filteredProjects.sort(Comparator.comparing(Project::getProjectName));
                break;
        }
        
        return filteredProjects;
    }
    
    /**
     * Calculate total available units across all flat types
     */
    private int calculateTotalAvailableUnits(Project project) {
        int total = 0;
        for (FlatType type : project.getFlatTypes()) {
            total += project.getAvailableUnitsByType(type);
        }
        return total;
    }
    
    /**
     * Display a list of projects
     * @param projects the projects to display
     */
    private void displayProjects(List<Project> projects) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.println("\n----- Available Projects -----");
        System.out.printf("%-5s %-20s %-15s %-20s %-15s\n", 
                         "No.", "Project Name", "Neighborhood", "Closing Date", "Available Units");
        System.out.println("--------------------------------------------------------------------------------");
        
        for (int i = 0; i < projects.size(); i++) {
            Project p = projects.get(i);
            int totalAvailable = calculateTotalAvailableUnits(p);
            
            System.out.printf("%-5d %-20s %-15s %-20s %-15d\n", 
                            (i + 1),
                            truncateString(p.getProjectName(), 20),
                            truncateString(p.getNeighborhood(), 15),
                            dateFormat.format(p.getApplicationCloseDate()),
                            totalAvailable);
        }
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
     * View details of a specific project
     * @param project the project to view
     */
    private void viewProjectDetails(Project project) {
        ScreenUtil.clearScreen();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.println("\n===== Project Details =====");
        System.out.println("Project Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Application Period: " + 
                         dateFormat.format(project.getApplicationOpenDate()) + " to " + 
                         dateFormat.format(project.getApplicationCloseDate()));
        
        System.out.println("\nFlat Types Available:");
        for (FlatType type : project.getFlatTypes()) {
            System.out.println("- " + type.getDisplayValue() + ": " + 
                             project.getAvailableUnitsByType(type) + " units available out of " + 
                             project.getTotalUnitsByType(type));
        }
        
        System.out.println("\nOptions:");
        System.out.println("1. Apply for this Project");
        System.out.println("2. Register as HDB Officer for this Project");
        System.out.println("3. Return to Project List");
        
        System.out.print("\nEnter your choice: ");
        String choice = sc.nextLine();
        
        switch (choice) {
            case "1":
                applyForProject(project);
                break;
            case "2":
                registerAsOfficer(project);
                break;
            case "3":
                return;
            default:
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
        }
    }
    
    /**
     * Apply for a project
     * @param project the project to apply for
     */
    private void applyForProject(Project project) {
        // Check if already has an active application
        if (currentUser.hasActiveApplications()) {
            System.out.println("You already have an active application. Cannot apply for multiple projects.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        if (!currentUser.isEligibleForProject(project)) {
            System.out.println("You are not eligible for this project.");
            System.out.println("Eligibility: Singles 35 and below can apply for 2-Room flats; Married 21 and above can apply for any flat type.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }

        // Confirm application
        System.out.print("Confirm application for " + project.getProjectName() + "? (Y/N): ");
        String confirm = sc.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean success = applicationControl.submitApplication(currentUser, project);
            
            if (success) {
                System.out.println("Application submitted successfully! Your application is pending approval.");
            } else {
                System.out.println("Failed to submit application. Please ensure you meet all requirements.");
            }
        } else {
            System.out.println("Application cancelled.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Register as an HDB Officer for a project
     * @param project the project to register for
     */
    private void registerAsOfficer(Project project) {
        // Check if there are available officer slots
        if (project.getAvailableOfficerSlots() <= 0) {
            System.out.println("No available officer slots for this project.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        // Check if already registered or applied for this project
        for (Application app : currentUser.getApplications()) {
            if (app.getProject().equals(project)) {
                System.out.println("You have already applied for this project as an Applicant.");
                System.out.println("You cannot be both an Applicant and an Officer for the same project.");
                System.out.println("Press Enter to continue...");
                sc.nextLine();
                return;
            }
        }}
        
        /*  Confirm registration
        System.out.print("Confirm registration as HDB Officer for " + project.getProjectName() + "? (Y/N): ");
        String confirm = sc.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            // Since the currentUser is an Applicant, we need to create or find an HDBOfficer entity
            // This would typically be handled by a control class
            boolean success = projectControl.registerAsOfficer(currentUser, project);
            
            if (success) {
                System.out.println("Registration submitted successfully! Your registration is pending approval.");
                System.out.println("Check your officer registration status in your profile.");
            } else {
                System.out.println("Failed to register. You may already be an officer for another project in the same period.");
            }
        } else {
            System.out.println("Registration cancelled.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();

    }
        */
    
    /**
     * View all applications made by the applicant
     */
    private void viewMyApplications() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== My Applications =====");
        
        List<Application> applications = currentUser.getApplications();
        
        if (applications.isEmpty()) {
            System.out.println("You have not applied for any projects yet.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        
        System.out.printf("%-5s %-20s %-15s %-15s %-15s\n", 
                         "No.", "Project Name", "Application Date", "Status", "Last Updated");
        System.out.println("--------------------------------------------------------------------------------");
        
        for (int i = 0; i < applications.size(); i++) {
            Application app = applications.get(i);
            
            System.out.printf("%-5d %-20s %-15s %-15s %-15s\n", 
                            (i + 1),
                            truncateString(app.getProject().getProjectName(), 20),
                            dateFormat.format(app.getApplicationDate()),
                            app.getStatus().getDisplayValue(),
                            dateFormat.format(app.getStatusUpdateDate()));
        }
        
        System.out.println("\nOptions:");
        System.out.println("1. View Application Details");
        System.out.println("2. Request Withdrawal");
        System.out.println("3. Return to Main Menu");
        
        System.out.print("\nEnter your choice: ");
        String choice = sc.nextLine();
        
        switch (choice) {
            case "1":
                System.out.print("Enter application number to view details: ");
                try {
                    int appIndex = Integer.parseInt(sc.nextLine()) - 1;
                    if (appIndex >= 0 && appIndex < applications.size()) {
                        viewApplicationDetails(applications.get(appIndex));
                    } else {
                        System.out.println("Invalid application number. Press Enter to continue.");
                        sc.nextLine();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Press Enter to continue.");
                    sc.nextLine();
                }
                break;
            case "2":
                System.out.print("Enter application number to request withdrawal: ");
                try {
                    int appIndex = Integer.parseInt(sc.nextLine()) - 1;
                    if (appIndex >= 0 && appIndex < applications.size()) {
                        requestWithdrawal(applications.get(appIndex));
                    } else {
                        System.out.println("Invalid application number. Press Enter to continue.");
                        sc.nextLine();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Press Enter to continue.");
                    sc.nextLine();
                }
                break;
            case "3":
                return;
            default:
                System.out.println("Invalid choice. Press Enter to continue.");
                sc.nextLine();
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
        
        if (application.getStatus() == ApplicationStatus.BOOKED) {
            Flat bookedFlat = application.getBookedFlat();
            if (bookedFlat != null) {
                System.out.println("\nBooked Flat Details:");
                System.out.println("Flat ID: " + bookedFlat.getFlatID());
                System.out.println("Flat Type: " + bookedFlat.getType().getDisplayValue());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Request withdrawal of an application
     * @param application the application to withdraw
     */
    private void requestWithdrawal(Application application) {
        if (!application.canWithdraw()) {
            System.out.println("This application cannot be withdrawn at this stage.");
            System.out.println("Press Enter to continue...");
            sc.nextLine();
            return;
        }
        
        System.out.print("Confirm withdrawal of application for " + 
                        application.getProject().getProjectName() + "? (Y/N): ");
        String confirm = sc.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean success = applicationControl.withdrawApplication(application);
            
            if (success) {
                System.out.println("Withdrawal request submitted successfully!");
                System.out.println("Your request will be processed by the HDB Manager.");
            } else {
                System.out.println("Failed to submit withdrawal request.");
            }
        } else {
            System.out.println("Withdrawal cancelled.");
        }
        
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * View applicant's profile
     */
    private void viewProfile() {
        ScreenUtil.clearScreen();
        System.out.println("\n===== My Profile =====");
        System.out.println("Name: " + currentUser.getName());
        System.out.println("NRIC: " + currentUser.getNRIC());
        System.out.println("Age: " + currentUser.getAge());
        System.out.println("Marital Status: " + currentUser.getMaritalStatusDisplayValue());
        System.out.println("Role: " + currentUser.getRole());
        
        // Show booked flat if any
        Flat bookedFlat = currentUser.getBookedFlat();
        if (bookedFlat != null) {
            System.out.println("\nBooked Flat Information:");
            System.out.println("Project: " + bookedFlat.getProject().getProjectName());
            System.out.println("Flat Type: " + bookedFlat.getType().getDisplayValue());
            System.out.println("Flat ID: " + bookedFlat.getFlatID());
        } else {
            System.out.println("\nNo flats booked yet.");
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
        try {
            currentUser.changePassword(newPassword);
            System.out.println("Password changed successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
}