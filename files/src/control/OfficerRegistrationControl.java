package control;

import entity.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Controls operations related to officer registrations in the BTO system.
 * Demonstrates Single Responsibility Principle by focusing only on officer registration operations.
 */
public class OfficerRegistrationControl {
    private static final String REGISTRATIONS_FILE = "files/resources/OfficerRegistrations.csv";
    private List<OfficerRegistration> registrations;
    
    /**
     * Constructor initializes the registrations list from storage
     */
    public OfficerRegistrationControl() {
        this.registrations = loadRegistrations();
    }
    
    /**
     * Submit a new officer registration
     * @param officer the officer
     * @param project the project to register for
     * @return the created registration, or null if submission failed
     */
    public OfficerRegistration submitRegistration(HDBOfficer officer, Project project) {
        // Check if officer is already registered for this project
        for (OfficerRegistration reg : registrations) {
            if (reg.getOfficer().getOfficerID().equals(officer.getOfficerID()) && 
                reg.getProject().getProjectID().equals(project.getProjectID())) {
                return null; // Already registered
            }
        }
        
        // Check if project has available slots
        if (project.getAvailableOfficerSlots() <= 0) {
            return null;
        }
        
        // Check if officer is handling another project in the same period
        if (officer.isHandlingProject(project.getApplicationOpenDate(), project.getApplicationCloseDate())) {
            return null;
        }
        
        // Create new registration
        OfficerRegistration registration = new OfficerRegistration(officer, project);
        
        // Add to list
        registrations.add(registration);
        
        // Save to file
        if (saveRegistrations()) {
            return registration;
        }
        
        return null;
    }
    
    /**
     * Get registrations for a project
     * @param project the project
     * @return list of registrations for the project
     */
    public List<OfficerRegistration> getRegistrationsForProject(Project project) {
        List<OfficerRegistration> projectRegistrations = new ArrayList<>();
        
        for (OfficerRegistration reg : registrations) {
            if (reg.getProject().getProjectID().equals(project.getProjectID())) {
                projectRegistrations.add(reg);
            }
        }
        
        return projectRegistrations;
    }
    
    /**
     * Get registrations by an officer
     * @param officer the officer
     * @return list of registrations by the officer
     */
    public List<OfficerRegistration> getRegistrationsByOfficer(HDBOfficer officer) {
        List<OfficerRegistration> officerRegistrations = new ArrayList<>();
        
        for (OfficerRegistration reg : registrations) {
            if (reg.getOfficer().getOfficerID().equals(officer.getOfficerID())) {
                officerRegistrations.add(reg);
            }
        }
        
        return officerRegistrations;
    }
    
    /**
     * Get pending registrations for a project
     * @param project the project
     * @return list of pending registrations
     */
    public List<OfficerRegistration> getPendingRegistrationsForProject(Project project) {
        List<OfficerRegistration> pendingRegistrations = new ArrayList<>();
        
        for (OfficerRegistration reg : registrations) {
            if (reg.getProject().getProjectID().equals(project.getProjectID()) && 
                reg.getStatus() == RegistrationStatus.PENDING) {
                pendingRegistrations.add(reg);
            }
        }
        
        return pendingRegistrations;
    }
    
    /**
     * Process a registration
     * @param manager the manager processing the registration
     * @param registration the registration to process
     * @param approve true to approve, false to reject
     * @return true if processing is successful, false otherwise
     */
    public boolean processRegistration(HDBManager manager, OfficerRegistration registration, boolean approve) {
        // Process the registration
        boolean success = registration.process(manager, approve);
        
        if (success) {
            // Save changes
            return saveRegistrations();
        }
        
        return false;
    }
    
    /**
     * Cancel a registration
     * @param registration the registration to cancel
     * @return true if cancellation was successful, false otherwise
     */
    public boolean cancelRegistration(OfficerRegistration registration) {
        // Check if registration is still pending
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            return false;
        }
        
        // Remove from list
        registrations.remove(registration);
        
        // Save changes
        return saveRegistrations();
    }
    
    /**
     * Load registrations from file
     * @return list of registrations
     */
    private List<OfficerRegistration> loadRegistrations() {
        List<OfficerRegistration> loadedRegistrations = new ArrayList<>();
        
        try (Scanner fileScanner = new Scanner(new File(REGISTRATIONS_FILE))) {
            // Skip header if exists
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] values = line.split(",");
                if (values.length < 5) continue; // Invalid line
                
                // Parse registration data
                try {
                    String registrationID = values[0].trim();
                    String officerID = values[1].trim();
                    String projectID = values[2].trim();
                    String statusStr = values[3].trim();
                    long registrationDate = Long.parseLong(values[4].trim());
                    
                    // Parse remaining fields
                    String processedDateStr = values.length > 5 ? values[5].trim() : "";
                    long processedDate = processedDateStr.isEmpty() ? 0 : Long.parseLong(processedDateStr);
                    String processedBy = values.length > 6 ? values[6].trim() : "";
                    
                    // Convert status string to enum
                    RegistrationStatus status = RegistrationStatus.valueOf(statusStr);
                    
                    // Find officer and project (in a real system, these would come from repositories)
                    // For now, create placeholders
                    HDBOfficer officer = new HDBOfficer(
                        "Officer " + officerID, // Placeholder name
                        "S1234567A", // Placeholder NRIC
                        "password",
                        40, // Placeholder age
                        "Married", // Placeholder marital status
                        "HDBOfficer"
                    );
                    
                    // Use reflection to set officerID (for demo purposes)
                    java.lang.reflect.Field idField = HDBOfficer.class.getDeclaredField("officerID");
                    idField.setAccessible(true);
                    idField.set(officer, officerID);
                    
                    Project project = new Project(
                        projectID,
                        "Project " + projectID, // Placeholder name
                        "Neighborhood", // Placeholder neighborhood
                        new HashMap<>(), // Placeholder units
                        new Date(), // Placeholder open date
                        new Date(), // Placeholder close date
                        null, // Placeholder manager
                        5 // Placeholder officer slots
                    );
                    
                    // Create registration
                    OfficerRegistration registration = new OfficerRegistration(officer, project);
                    
                    // Use reflection to set private fields
                    java.lang.reflect.Field regIdField = OfficerRegistration.class.getDeclaredField("registrationID");
                    regIdField.setAccessible(true);
                    regIdField.set(registration, registrationID);
                    
                    java.lang.reflect.Field statusField = OfficerRegistration.class.getDeclaredField("status");
                    statusField.setAccessible(true);
                    statusField.set(registration, status);
                    
                    java.lang.reflect.Field regDateField = OfficerRegistration.class.getDeclaredField("registrationDate");
                    regDateField.setAccessible(true);
                    regDateField.set(registration, new Date(registrationDate));
                    
                    // Set processed fields if available
                    if (processedDate > 0) {
                        java.lang.reflect.Field procDateField = OfficerRegistration.class.getDeclaredField("processedDate");
                        procDateField.setAccessible(true);
                        procDateField.set(registration, new Date(processedDate));
                        
                        if (!processedBy.isEmpty()) {
                            java.lang.reflect.Field procByField = OfficerRegistration.class.getDeclaredField("processedBy");
                            procByField.setAccessible(true);
                            procByField.set(registration, processedBy);
                        }
                    }
                    
                    loadedRegistrations.add(registration);
                    
                } catch (Exception e) {
                    System.err.println("Error parsing officer registration data: " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Officer registrations file not found. Starting with empty list.");
        }
        
        return loadedRegistrations;
    }
    
    /**
     * Save registrations to file
     * @return true if successful, false otherwise
     */
    private boolean saveRegistrations() {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(REGISTRATIONS_FILE))) {
                // Write header
                writer.println("RegistrationID,OfficerID,ProjectID,Status,RegistrationDate,ProcessedDate,ProcessedBy");
                
                // Write registrations
                for (OfficerRegistration reg : registrations) {
                    writer.println(
                        reg.getRegistrationID() + "," +
                        reg.getOfficer().getOfficerID() + "," +
                        reg.getProject().getProjectID() + "," +
                        reg.getStatus() + "," +
                        reg.getRegistrationDate().getTime() + "," +
                        (reg.getProcessedDate() != null ? reg.getProcessedDate().getTime() : "") + "," +
                        (reg.getProcessedBy() != null ? reg.getProcessedBy() : "")
                    );
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving officer registrations: " + e.getMessage());
            return false;
        }
    }
}