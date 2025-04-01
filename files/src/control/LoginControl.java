package control;

import entity.*;
import java.io.*;
import java.util.*;

public class LoginControl {
    public boolean validateInitial(String nric, String password) {
        // Validate NRIC format
        if (nric == null || nric.isEmpty() || nric.length() != 9 || !nric.matches("[STst]\\d{7}[A-Za-z]")) {
            System.out.println("Invalid NRIC format.");
            return false;
        }
        
        // For password, consider allowing the default password even if shorter than 8 characters
        if (password == null || (password.length() < 8 && !password.equals("password"))) {
            System.out.println("Password must be at least 8 characters long.");
            return false;
        }
        return true;
    }

    public User checkUserCredentials(String nric, String password) {
        // Try all user files
        try {
            // Check in Manager list first (highest priority)
            User user = checkInFile("files/resources/ManagerList.csv", nric, password);
            if (user != null) {
                return user;
            }
            
            // Check in Officer list
            user = checkInFile("files/resources/OfficerList.csv", nric, password);
            if (user != null) {
                return user;
            }
            
            // Check in Applicant list
            user = checkInFile("files/resources/ApplicantList.csv", nric, password);
            if (user != null) {
                return user;
            }
            
            return null;
        } catch (IOException e) {
            System.err.println("Error checking user credentials: " + e.getMessage());
            return null;
        }
    }
    
    private User checkInFile(String filePath, String nric, String password) throws IOException {
        try (Scanner fileScanner = new Scanner(new File(filePath))) {
            // Get the filename to determine user role
            String fileName = new File(filePath).getName();
            
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
                
                // Make sure we have minimum fields needed
                if (values.length < 5) {
                    continue;
                }
                
                // Check if NRIC and password match (case-insensitive for NRIC)
                if (values[1].equalsIgnoreCase(nric) && values[4].equals(password)) {
                    try {
                        // Create the appropriate user object based on file
                        int age = Integer.parseInt(values[2]);
                        String name = values[0];
                        String maritalStatus = values[3];
                        
                        // Create the correct type of User based on file
                        if (fileName.contains("Manager")) {
                            return new HDBManager(name, nric, password, age, maritalStatus, "HDBManager");
                        } else if (fileName.contains("Officer")) {
                            return new HDBOfficer(name, nric, password, age, maritalStatus, "HDBOfficer");
                        } else {
                            return new Applicant(name, nric, password, age, maritalStatus, "Applicant");
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing age in file " + filePath + ": " + e.getMessage());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
        }
        
        return null;
    }
}