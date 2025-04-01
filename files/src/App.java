import boundary.ApplicantUI;
import boundary.LoginUI;
import boundary.ManagerUI;
import boundary.OfficerUI;
import entity.Applicant;
import entity.HDBManager;
import entity.HDBOfficer;
import entity.User;

/**
 * Main application class for the BTO Management System.
 */
public class App {
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
     * Main method to start the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Display welcome message
        System.out.println("Starting BTO Management System...");
        
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
        
        // Display exit message
        System.out.println("Thank you for using BTO Management System. Goodbye!");
    }
}