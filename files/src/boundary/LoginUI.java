
package boundary;

import control.LoginControl;
import control.RegistrationControl;
import java.util.Scanner;

public class LoginUI {
    private Scanner sc;
    private LoginControl loginControl;
    private RegistrationControl registrationControl;

    public LoginUI() {
        sc = new Scanner(System.in);
        loginControl = new LoginControl();
    }

    public boolean displayLoginMenu() {
        System.out.println("\n===== BTO Management System =====");
        System.out.println("Welcome! Please login to continue.");
        
        // Ask for NRIC and password
        System.out.print("Enter your NRIC: ");
        String nric = sc.nextLine();
        
        System.out.print("Enter your password (if you are a new user, use default password 'password'):");
        String password = sc.nextLine();
        
        // Check if user exists
        if (!loginControl.validateInitial(nric, password)) {
            System.out.println("Invalid NRIC or password format. Please try again.");
            return true;
        } else {
            String[] userInfo = loginControl.checkUserCredentials(nric, password);
        
            if (userInfo != null) {
                // Successful login
                System.out.println("\nLogin successful!");
                System.out.println("Welcome, " + userInfo[0] + "! ");
            } else {
                // User not found, ask if they want to register
                System.out.println("\nUser not found.");
                System.out.print("Do you want to register as a new user? (Y/N): ");
                String response = sc.nextLine();
                
                if (response.equalsIgnoreCase("Y")) {
                    return registerNewUser(nric);
                } else {
                    System.out.println("Login cancelled. Goodbye!");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean registerNewUser(String nric) {
        // Validate NRIC format
        if (nric == null || nric.isEmpty() || nric.length() != 9 || !nric.matches("[ST]\\d{7}[A-Z]")) {
            System.out.println("Invalid NRIC format. Registration cancelled.");
            return true;
        }
        
        // Collect user information
        System.out.print("Enter your name: ");
        String name = sc.nextLine();
        
        System.out.print("Enter your age: ");
        int age = 0;
        try {
            age = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid age format. Registration cancelled.");
            return true;
        }
        
        System.out.print("Enter your marital status (Single/Married): ");
        String maritalStatus = sc.nextLine();
        
        // Use default password
        String password = "password";
        
        // Create registration control and register
        registrationControl = new RegistrationControl(name, nric, password, age, maritalStatus);
        
        if (registrationControl.validateCredentials()) {
            registrationControl.registerUser();
            System.out.println("\nUser registered successfully! Please change default password upon logging in.");
            System.out.println("Welcome, " + name + "! ");
            return false;

        } else {
            System.out.println("\nRegistration failed. Please try again later.");
            return true;
        }
    }

    // Method to close scanner
    public void close() {
        if (sc != null) {
            sc.close();
        }
    }

    
}