import boundary.ApplicantUI;
import boundary.LoginUI;
import boundary.ManagerUI;
import boundary.OfficerUI;
import control.ApplicationControl;
import control.EnquiryControl;
import control.HDBManagerControl;
import control.HDBOfficerControl;
import control.ProjectControl;
import control.ReportControl;
import entity.Applicant;
import entity.HDBManager;
import entity.HDBOfficer;
import entity.User;
import java.util.Scanner;
import utils.ProjectDataManager;

/**
 * Main application class for the BTO Management System.
 * Demonstrates the use of centralized system initialization and routing.
 */
public class App {
    private static Scanner scanner;
    
    /**
     * Clears the console screen.
     */
    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // For Unix-like systems (Linux, macOS)
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            // Fallback if the commands fail
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * Initialize application data and control objects
     */
    public static void initializeData() {
        try {
            System.out.println("Initializing application data...");
            
            // Initialize project data storage
            ProjectDataManager projectDataManager = ProjectDataManager.getInstance();
            
            // Initialize control classes
            ProjectControl projectControl = new ProjectControl(projectDataManager);
            ApplicationControl applicationControl = new ApplicationControl();
            HDBManagerControl managerControl = new HDBManagerControl();
            HDBOfficerControl officerControl = new HDBOfficerControl();
            EnquiryControl enquiryControl = new EnquiryControl();
            ReportControl reportControl = new ReportControl();
            
            // Initialize project data from CSV
            ProjectDataManager projectFileManager = ProjectDataManager.getInstance();
            projectFileManager.loadInitialProjectData();
            
            System.out.println("Data initialization complete.");
        } catch (Exception e) {
            System.out.println("Error initializing data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Display initial header for the application
     */
    private static void displayHeader() {
        clearScreen();
        System.out.println("=====================================================");
        System.out.println("             BTO MANAGEMENT SYSTEM                  ");
        System.out.println("=====================================================");
        System.out.println("      Housing & Development Board of Singapore      ");
        System.out.println("=====================================================");
        System.out.println();
    }
    
    /**
     * Main method to start the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Initialize scanner for system-wide use
        scanner = new Scanner(System.in);
        
        // Display welcome header
        displayHeader();
        System.out.println("Starting BTO Management System...");
        
        // Initialize application data
        initializeData();
        
        // Initialize login UI
        LoginUI loginUI = new LoginUI();
        boolean shouldContinue = true;
        
        // Login loop
        while (shouldContinue) {
            shouldContinue = loginUI.displayLoginMenu();
        }
        
        // Get authenticated user
        User currentUser = loginUI.getCurrentUser();
        if (currentUser != null) {
            // Route to appropriate UI based on user role
            if (currentUser instanceof HDBManager) {
                ManagerUI managerUI = new ManagerUI((HDBManager) currentUser);
                managerUI.displayMenu();
            } else if (currentUser instanceof HDBOfficer) {
                OfficerUI officerUI = new OfficerUI((HDBOfficer) currentUser);
                officerUI.displayMenu();
            } else if (currentUser instanceof Applicant) {
                ApplicantUI applicantUI = new ApplicantUI((Applicant) currentUser);
                applicantUI.displayMenu();
            } else {
                System.out.println("Unknown user type. Please contact system administrator.");
            }
        }
        
        // Close resources
        loginUI.close();
        if (scanner != null) {
            scanner.close();
        }
        
        // Display exit message
        System.out.println("\nThank you for using BTO Management System. Goodbye!");
    }
}