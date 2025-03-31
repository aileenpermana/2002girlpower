package control;
import java.io.*;
import java.util.*;

public class LoginControl {
    public boolean validateInitial(String nric, String password) {
        // Validate NRIC format
        if (nric == null || nric.isEmpty() || nric.length() != 9 || !nric.matches("[ST]\\d{7}[A-Z]")) {
            System.out.println("Invalid NRIC format.");
            return false;
        }
        
        // Validate password length
        if (password == null || password.length() < 8) {
            System.out.println("Password must be at least 8 characters long.");
            return false;
        }
        
        return true;
    }

    public String[] checkUserCredentials(String nric, String password) {
        try {
            String[] userInfo = checkInFile("files/resources/ApplicantList.csv", nric, password);
            if (userInfo != null) {
                return userInfo;
            }
            
            userInfo = checkInFile("files/resources/ManagerList.csv", nric, password);
            if (userInfo != null) {
                return userInfo;
            }
            
            return null;
        } catch (IOException e) {
            System.err.println("Error checking user credentials: " + e.getMessage());
            return null;
        }
    }
    
    private String[] checkInFile(String filePath, String nric, String password) throws IOException {
        try (Scanner fileScanner = new Scanner(new File(filePath))) {
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] values = line.split(",");
                
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim();
                }
                
                // Check if NRIC and password match
                if (values.length > 4 && values[1].equals(nric) && values[4].equals(password)) {
                    return values;
                }
            }
        } catch (FileNotFoundException e) {
            // If file doesn't exist, just return null
            System.out.println("File not found: " + filePath);
        }
        
        return null;
    }
    
    // Additional methods can be added here as needed
}