package utils;

import entity.*;
import control.ProjectControl;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * CSV implementation of DataStorage for Applications
 * Demonstrates Open/Closed Principle and Dependency Inversion Principle
 */
public class ApplicationCSVStorage implements DataStorage<Application> {
    private static final String FILE_PATH = "files/resources/ApplicationList.csv";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    /**
     * Read all applications from CSV file
     * @return list of applications
     */
    @Override
    public List<Application> readAll() {
        List<Application> applications = new ArrayList<>();
        File file = new File(FILE_PATH);
        
        if (!file.exists() || file.length() == 0) {
            return applications;
        }
        
        try (Scanner fileScanner = new Scanner(file)) {
            // Skip header if exists
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            
            ProjectControl projectControl = new ProjectControl();
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] values = line.split(",");
                if (values.length < 7) continue; // Invalid line
                
                // Parse application data
                try {
                    String applicationID = values[0].trim();
                    String applicantNRIC = values[1].trim();
                    String projectID = values[2].trim();
                    String statusStr = values[3].trim();
                    long applicationDate = Long.parseLong(values[4].trim());
                    long statusUpdateDate = Long.parseLong(values[5].trim());
                    String bookedFlatID = values.length > 6 ? values[6].trim() : "";
                    
                    // Convert status string to enum
                    ApplicationStatus status = ApplicationStatus.valueOf(statusStr);
                    
                    // Find applicant (in a real system, this would come from an applicant repository)
                    // For now, create a placeholder
                    Applicant applicant = new Applicant(
                        "Applicant " + applicantNRIC, // Placeholder name
                        applicantNRIC,
                        "password",
                        30, // Placeholder age
                        "Single", // Placeholder marital status
                        "Applicant"
                    );
                    
                    // Find project
                    List<Project> projects = projectControl.getAllProjects();
                    Project project = null;
                    for (Project p : projects) {
                        if (p.getProjectID().equals(projectID)) {
                            project = p;
                            break;
                        }
                    }
                    
                    if (project == null) {
                        // If project not found, create a placeholder
                        project = new Project(
                            projectID,
                            "Project " + projectID, // Placeholder name
                            "Neighborhood", // Placeholder neighborhood
                            new HashMap<>(), // Placeholder units
                            new Date(), // Placeholder open date
                            new Date(), // Placeholder close date
                            null, // Placeholder manager
                            5 // Placeholder officer slots
                        );
                    }
                    
                    // Create application with the applicant and project
                    Application application = new Application(applicant, project);
                    
                    // Use reflection to set the private fields
                    java.lang.reflect.Field idField = Application.class.getDeclaredField("applicationID");
                    idField.setAccessible(true);
                    idField.set(application, applicationID);
                    
                    java.lang.reflect.Field statusField = Application.class.getDeclaredField("status");
                    statusField.setAccessible(true);
                    statusField.set(application, status);
                    
                    java.lang.reflect.Field appDateField = Application.class.getDeclaredField("applicationDate");
                    appDateField.setAccessible(true);
                    appDateField.set(application, new Date(applicationDate));
                    
                    java.lang.reflect.Field statusDateField = Application.class.getDeclaredField("statusUpdateDate");
                    statusDateField.setAccessible(true);
                    statusDateField.set(application, new Date(statusUpdateDate));
                    
                    // Set booked flat if available
                    if (!bookedFlatID.isEmpty()) {
                        // Create a flat with the booked flat ID
                        // In a real system, this would come from a flat repository
                        Flat flat = new Flat(bookedFlatID, project, 
                                           determineDefaultFlatType(applicant));
                        
                        application.setBookedFlat(flat);
                    }
                    
                    applications.add(application);
                    
                } catch (Exception e) {
                    System.err.println("Error parsing application: " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Application file not found: " + FILE_PATH);
        }
        
        return applications;
    }
    
    /**
     * Determine default flat type based on applicant's marital status
     * @param applicant the applicant
     * @return the default flat type
     */
    private FlatType determineDefaultFlatType(Applicant applicant) {
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
            return FlatType.TWO_ROOM;
        } else {
            return FlatType.THREE_ROOM;
        }
    }
    
    /**
     * Save a new application to CSV file
     * @param application the application to save
     * @return true if successful, false otherwise
     */
    @Override
    public boolean save(Application application) {
        List<Application> applications = readAll();
        applications.add(application);
        return saveAll(applications);
    }
    
    /**
     * Update an existing application in CSV file
     * @param application the updated application
     * @return true if successful, false otherwise
     */
    @Override
    public boolean update(Application application) {
        List<Application> applications = readAll();
        
        for (int i = 0; i < applications.size(); i++) {
            if (applications.get(i).getApplicationID().equals(application.getApplicationID())) {
                applications.set(i, application);
                return saveAll(applications);
            }
        }
        
        return false; // Application not found
    }
    
    /**
     * Delete an application from CSV file
     * @param application the application to delete
     * @return true if successful, false otherwise
     */
    @Override
    public boolean delete(Application application) {
        List<Application> applications = readAll();
        
        for (int i = 0; i < applications.size(); i++) {
            if (applications.get(i).getApplicationID().equals(application.getApplicationID())) {
                applications.remove(i);
                return saveAll(applications);
            }
        }
        
        return false; // Application not found
    }
    
    /**
     * Save all applications to CSV file
     * @param applications the list of applications to save
     * @return true if successful, false otherwise
     */
    @Override
    public boolean saveAll(List<Application> applications) {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
                // Write header
                writer.println("ApplicationID,ApplicantNRIC,ProjectID,Status,ApplicationDate,StatusUpdateDate,BookedFlatID");
                
                // Write applications
                for (Application app : applications) {
                    writer.print(
                        app.getApplicationID() + "," +
                        app.getApplicant().getNRIC() + "," +
                        app.getProject().getProjectID() + "," +
                        app.getStatus() + "," +
                        app.getApplicationDate().getTime() + "," +
                        app.getStatusUpdateDate().getTime()
                    );
                    
                    // Add booked flat ID if any
                    if (app.getBookedFlat() != null) {
                        writer.print("," + app.getBookedFlat().getFlatID());
                    } else {
                        writer.print(",");
                    }
                    
                    writer.println();
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving applications: " + e.getMessage());
            return false;
        }
    }
}