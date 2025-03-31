package control;
import entity.User;
import java.io.*;
import java.util.*;

public class RegistrationControl {
    private String name;
    private String nric;
    private String password;
    private int age;
    private String maritalStatus;

    // Constructor
    public RegistrationControl(String name, String nric, String password, int age, String maritalStatus) {
        this.name = name;
        this.nric = nric;
        this.password = password;
        this.age = age;
        this.maritalStatus = maritalStatus;
    }

    // Check if NRIC is already registered in any user file
    public boolean isNricRegistered() {
        // Check in applicant list
        if (checkNricInFile("files/resources/ApplicantList.csv")) {
            return true;
        }
        
        // Check in manager list
        if (checkNricInFile("files/resources/ManagerList.csv")) {
            return true;
        }
        
        // Check in officer list
        if (checkNricInFile("files/resources/OfficerList.csv")) {
            return true;
        }
        
        // NRIC not found in any list
        return false;
    }
    
    private boolean checkNricInFile(String filePath) {
        try (Scanner fileScanner = new Scanner(new File(filePath))) {
            // Skip header if it exists
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            
            // Read data lines
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] values = line.split(",");
                
                // Assuming NRIC is in the second column (index 1)
                if (values.length > 1 && values[1].trim().equals(this.nric)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            // If file doesn't exist, just return false
            System.out.println("File not found: " + filePath);
        }
        
        return false;
    }

    // Validate registration credentials
    public boolean validateCredentials() {
        // Basic validation checks
        if (name == null || name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return false;
        }
        
        
        
        if (password == null || password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }
        
        if (age <= 0) {
            System.out.println("Age must be a positive number.");
            return false;
        }
        
        if (age < 21) {
            System.out.println("You must be at least 21 years old to register.");
            return false;
        }
        
        if (!maritalStatus.equals("Single") && !maritalStatus.equals("Married")) {
            System.out.println("Invalid marital status. Please enter 'Single' or 'Married'.");
            return false;
        }
        
        // Check if NRIC is already registered
        if (isNricRegistered()) {
            System.out.println("User with this NRIC already exists.");
            return false;
        }
        
        // All checks passed
        return true;
    }

    // Register user in the applicant list
    public User registerUser() {
        User user = new User(name, nric, password, age, maritalStatus, "Applicant");
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            // Check if file exists, if not create with header
            File file = new File("files/resources/ApplicantList.csv");
            boolean fileExists = file.exists();
            
            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                
                // Add header if file is new
                if (!fileExists) {
                    out.println("Name,NRIC,Age,MaritalStatus,Password");
                }
                
                // Append new user to CSV
                out.println(name + "," + nric + "," + age + "," + maritalStatus + "," + password );
                System.out.println("User registered successfully as an Applicant!");
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
        return user;
    }
}