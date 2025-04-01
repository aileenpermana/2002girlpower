package control;

import entity.*;
import java.io.*;
import java.util.*;

/**
 * Controls operations related to HDB Officers in the BTO system.
 */
public class HDBOfficerControl {
    private static final String OFFICER_REGISTRATIONS_FILE = "files/resources/OfficerRegistrations.csv";
    private List<Map<String, Object>> officerRegistrations;
    
    /**
     * Constructor initializes the officer registrations list from storage
     */
    public HDBOfficerControl() {
        this.officerRegistrations = loadOfficerRegistrations();
    }
    
    /**
     * Get officer registrations for an officer
     * @param officer the officer
     * @return list of registration records
     */
    public List<Map<String, Object>> getOfficerRegistrations(HDBOfficer officer) {
        List<Map<String, Object>> registrations = new ArrayList<>();
        
        for (Map<String, Object> reg : officerRegistrations) {
            HDBOfficer regOfficer = (HDBOfficer) reg.get("officer");
            if (regOfficer.getOfficerID().equals(officer.getOfficerID())) {
                registrations.add(reg);
            }
        }
        
        return registrations;
    }
    
    /**
     * Get officer registrations for a project
     * @param project the project
     * @return list of registration records
     */
    public List<Map<String, Object>> getOfficerRegistrationsForProject(Project project) {
        List<Map<String, Object>> registrations = new ArrayList<>();
        
        for (Map<String, Object> reg : officerRegistrations) {
            Project regProject = (Project) reg.get("project");
            if (regProject.getProjectID().equals(project.getProjectID())) {
                registrations.add(reg);
            }
        }
        
        return registrations;
    }
    
    /**
     * Register an officer for a project
     * @param officer the officer
     * @param project the project
     * @return true if registration is successful, false otherwise
     */
    public boolean registerOfficer(HDBOfficer officer, Project project) {
        // Check if officer is already registered for this project
        for (Map<String, Object> reg : officerRegistrations) {
            HDBOfficer regOfficer = (HDBOfficer) reg.get("officer");
            Project regProject = (Project) reg.get("project");
            
            if (regOfficer.getOfficerID().equals(officer.getOfficerID()) && 
                regProject.getProjectID().equals(project.getProjectID())) {
                return false; // Already registered
            }
        }
        
        // Check if project has available slots
        if (project.getAvailableOfficerSlots() <= 0) {
            return false;
        }
        
        // Check if officer is handling another project in the same period
        if (officer.isHandlingProject(project.getApplicationOpenDate(), project.getApplicationCloseDate())) {
            return false;
        }
        
        // Create registration record
        Map<String, Object> registration = new HashMap<>();
        registration.put("officer", officer);
        registration.put("project", project);
        registration.put("status", RegistrationStatus.PENDING);
        registration.put("date", new Date());
        
        // Add to list
        officerRegistrations.add(registration);
        
        // Save to file
        return saveOfficerRegistrations();
    }
    
    /**
     * Update the status of an officer registration
     * @param officer the officer
     * @param project the project
     * @param status the new status
     * @return true if update is successful, false otherwise
     */
    public boolean updateRegistrationStatus(HDBOfficer officer, Project project, RegistrationStatus status) {
        // Find and update registration
        for (Map<String, Object> reg : officerRegistrations) {
            HDBOfficer regOfficer = (HDBOfficer) reg.get("officer");
            Project regProject = (Project) reg.get("project");
            
            if (regOfficer.getOfficerID().equals(officer.getOfficerID()) && 
                regProject.getProjectID().equals(project.getProjectID())) {
                
                reg.put("status", status);
                
                // If approved, add officer to project
                if (status == RegistrationStatus.APPROVED) {
                    // Add officer to project
                    project.addOfficer(officer);
                    
                    // Add project to officer's handling list
                    officer.addHandlingProject(project);
                    
                    // Decrement available slots
                    project.decrementOfficerSlots();
                }
                
                return saveOfficerRegistrations();
            }
        }
        
        return false; // Registration not found
    }
    
    /**
     * Load officer registrations from file
     * @return list of registration records
     */
    private List<Map<String, Object>> loadOfficerRegistrations() {
        List<Map<String, Object>> loadedRegistrations = new ArrayList<>();
        
        try (Scanner fileScanner = new Scanner(new File(OFFICER_REGISTRATIONS_FILE))) {
            // Skip header if exists
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] values = line.split(",");
                if (values.length < 4) continue; // Invalid line
                
                // Parse registration data
                try {
                    String officerID = values[0].trim();
                    String projectID = values[1].trim();
                    String statusStr = values[2].trim();
                    long registrationDate = Long.parseLong(values[3].trim());
                    
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
                    
                    // Create registration record
                    Map<String, Object> registration = new HashMap<>();
                    registration.put("officer", officer);
                    registration.put("project", project);
                    registration.put("status", status);
                    registration.put("date", new Date(registrationDate));
                    
                    // Add to list
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
     * Save officer registrations to file
     * @return true if successful, false otherwise
     */
    private boolean saveOfficerRegistrations() {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(OFFICER_REGISTRATIONS_FILE))) {
                // Write header
                writer.println("OfficerID,ProjectID,Status,RegistrationDate");
                
                // Write registrations
                for (Map<String, Object> reg : officerRegistrations) {
                    HDBOfficer officer = (HDBOfficer) reg.get("officer");
                    Project project = (Project) reg.get("project");
                    RegistrationStatus status = (RegistrationStatus) reg.get("status");
                    Date date = (Date) reg.get("date");
                    
                    writer.println(
                        officer.getOfficerID() + "," +
                        project.getProjectID() + "," +
                        status + "," +
                        date.getTime()
                    );
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving officer registrations: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Book a flat for an applicant
     * @param officer the HDB officer processing the booking
     * @param application the application
     * @param flat the flat to book
     * @return true if booking is successful, false otherwise
     */
    public boolean bookFlat(HDBOfficer officer, Application application, Flat flat) {
        // Check if officer is handling the project
        if (!officer.isHandlingProject(application.getProject())) {
            return false;
        }
        
        // Check if application is successful and can be booked
        if (!application.canBook()) {
            return false;
        }
        
        // Check if flat is available
        if (flat.isBooked()) {
            return false;
        }
        
        // Book the flat
        flat.setBookedByApplication(application);
        application.setBookedFlat(flat);
        application.setStatus(ApplicationStatus.BOOKED);
        
        // Update the applicant
        Applicant applicant = application.getApplicant();
        applicant.setBookedFlat(flat);
        
        // Update available units count
        Project project = application.getProject();
        FlatType flatType = flat.getType();
        
        // Get current available units and decrement by 1
        int availableUnits = project.getAvailableUnitsByType(flatType);
        project.setAvailableUnitsByType(flatType, availableUnits - 1);
        
        return true;
    }
    
    /**
     * Generate a receipt for a booking
     * @param application the application with a booked flat
     * @return a formatted receipt string
     */
    public String generateReceipt(Application application) {
        if (application.getStatus() != ApplicationStatus.BOOKED || application.getBookedFlat() == null) {
            return null; // Cannot generate receipt for non-booked applications
        }
        
        StringBuilder receipt = new StringBuilder();
        Applicant applicant = application.getApplicant();
        Project project = application.getProject();
        Flat flat = application.getBookedFlat();
        
        receipt.append("====================================================\n");
        receipt.append("                BOOKING RECEIPT                     \n");
        receipt.append("====================================================\n");
        receipt.append("Receipt ID: REC-").append(application.getApplicationID()).append("\n");
        receipt.append("Date: ").append(new Date()).append("\n\n");
        
        receipt.append("APPLICANT DETAILS:\n");
        receipt.append("Name: ").append(applicant.getName()).append("\n");
        receipt.append("NRIC: ").append(applicant.getNRIC()).append("\n");
        receipt.append("Age: ").append(applicant.getAge()).append("\n");
        receipt.append("Marital Status: ").append(applicant.getMaritalStatusDisplayValue()).append("\n\n");
        
        receipt.append("PROJECT DETAILS:\n");
        receipt.append("Project Name: ").append(project.getProjectName()).append("\n");
        receipt.append("Neighborhood: ").append(project.getNeighborhood()).append("\n\n");
        
        receipt.append("FLAT DETAILS:\n");
        receipt.append("Flat ID: ").append(flat.getFlatID()).append("\n");
        receipt.append("Flat Type: ").append(flat.getType().getDisplayValue()).append("\n\n");
        
        receipt.append("This receipt confirms the booking of the flat.\n");
        receipt.append("Please keep this receipt for your records.\n");
        receipt.append("====================================================\n");
        
        return receipt.toString();
    }
    
    /**
     * Find an application by NRIC and project
     * @param nric the applicant's NRIC
     * @param project the project
     * @return the application, or null if not found
     */
    public Application findApplicationByNRIC(String nric, Project project) {
        // Delegate to ApplicationControl to find the application
        ApplicationControl appControl = new ApplicationControl();
        return appControl.findApplicationByNRICAndProject(nric, project);
    }
}