package control;

import entity.Applicant;
import entity.User;
import java.io.*;
import java.util.Scanner;

public class RegistrationControl {
    private final String name;
    private final String nric;
    private final String password;
    private final int age;
    private String maritalStatus;
    
    // Constructor
    public RegistrationControl(String name, String nric, String password, int age, String maritalStatus) {
        this.name = name;
        this.nric = nric;
        this.password = password;
        this.age = age;
        this.maritalStatus = maritalStatus;
    }
    
    // Validate registration credentials
    public boolean validateCredentials() {
        // Basic validation checks
        if (name == null || name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return false;
        }
        
        if (nric == null || nric.isEmpty()) {
            System.out.println("NRIC cannot be empty.");
            return false;
        }
        
        if (nric.length() != 9 || !nric.matches("[STst]\\d{7}[A-Za-z]")) {
            System.out.println("Invalid NRIC format.");
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
        
        if (!maritalStatus.equalsIgnoreCase("Single") && !maritalStatus.equalsIgnoreCase("Married")) {
            System.out.println("Invalid marital status. Please enter 'Single' or 'Married'.");
            return false;
        }
        
        // Standardize marital status format (first letter uppercase, rest lowercase)
        this.maritalStatus = maritalStatus.substring(0, 1).toUpperCase() + maritalStatus.substring(1).toLowerCase();
        
        // Check if NRIC is already registered
        if (isNricRegistered()) {
            System.out.println("User with this NRIC already exists.");
            return false;
        }
        
        // All checks passed
        return true;
    }
    
    // Check if NRIC is already registered
    private boolean isNricRegistered() {
        try {
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
            
            return false;
        } catch (IOException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            // If we can't check, assume the user doesn't exist
            return false;
        }
    }
    
    private boolean checkNricInFile(String filePath) throws IOException {
        try (Scanner fileScanner = new Scanner(new File(filePath))) {
            // Skip header
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if (line.trim().isEmpty()) continue;
                
                String[] values = line.split(",");
                
                // Trim values
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim();
                }
                
                // Make sure we have at least 2 fields (to check NRIC)
                if (values.length < 2) {
                    continue;
                }
                
                // Check if NRIC matches (case-insensitive)
                if (values[1].equalsIgnoreCase(this.nric)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            // If file doesn't exist, just continue checking other files
        }
        return false;
    }
    
    // Register user in the applicant list
    public User registerUser() {
        User user = new Applicant(name, nric, password, age, maritalStatus, "Applicant");
        
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
                out.println(name + "," + nric + "," + age + "," + maritalStatus + "," + password);
                System.out.println("User registered successfully as an Applicant!");
            }
            
            return user;
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            return null;
        }
    }
}