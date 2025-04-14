package control;

import entity.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Enhanced control class for managing enquiries in the BTO system.
 * Demonstrates Single Responsibility Principle by focusing only on enquiry operations.
 */
public class EnquiryControl {
    private static final String ENQUIRIES_FILE = "files/resources/EnquiryList.csv";
    private List<Enquiry> enquiries;
    
    /**
     * Constructor initializes the enquiries list from storage
     */
    public EnquiryControl() {
        this.enquiries = loadEnquiries();
    }
    
    /**
     * Get all enquiries in the system
     * @return list of all enquiries
     */
    public List<Enquiry> getAllEnquiries() {
        return new ArrayList<>(enquiries);
    }
    
    /**
     * Get enquiries for a project
     * @param project the project
     * @return list of enquiries for the project
     */
    public List<Enquiry> getEnquiriesForProject(Project project) {
        List<Enquiry> projectEnquiries = new ArrayList<>();
        
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getProject().getProjectID().equals(project.getProjectID())) {
                projectEnquiries.add(enquiry);
            }
        }
        
        return projectEnquiries;
    }
    
    /**
     * Get enquiries submitted by an applicant
     * @param applicant the applicant
     * @return list of enquiries by the applicant
     */
    public List<Enquiry> getEnquiriesByApplicant(Applicant applicant) {
        List<Enquiry> applicantEnquiries = new ArrayList<>();
        
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getApplicant().getNRIC().equals(applicant.getNRIC())) {
                applicantEnquiries.add(enquiry);
            }
        }
        
        return applicantEnquiries;
    }
    
    /**
     * Submit a new enquiry
     * @param applicant the applicant
     * @param project the project
     * @param content the enquiry content
     * @return the created enquiry, or null if submission failed
     */
    public Enquiry submitEnquiry(Applicant applicant, Project project, String content) {
        // Validate input
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        // Generate enquiry ID
        String enquiryID = generateEnquiryID(applicant, project);
        
        // Create new enquiry
        Enquiry enquiry = new Enquiry(enquiryID, applicant, project, content, new Date());
        
        // Add to list
        enquiries.add(enquiry);
        
        // Save to file
        if (saveEnquiries()) {
            return enquiry;
        }
        
        return null;
    }
    
    /**
     * Generate an enquiry ID based on the applicant and project
     * @param applicant the applicant
     * @param project the project
     * @return a unique enquiry ID
     */
    private String generateEnquiryID(Applicant applicant, Project project) {
        // Simple algorithm to generate an enquiry ID
        String prefix = "ENQ";
        String applicantPart = applicant.getNRIC().substring(1, 5);
        String projectPart = project.getProjectID().substring(0, Math.min(3, project.getProjectID().length()));
        String timestamp = Long.toString(System.currentTimeMillis() % 100000);
        
        return prefix + "-" + applicantPart + "-" + projectPart + "-" + timestamp;
    }
    
    /**
     * Edit an existing enquiry
     * @param enquiry the enquiry to edit
     * @param newContent the new content
     * @return true if edit was successful, false otherwise
     */
    public boolean editEnquiry(Enquiry enquiry, String newContent) {
        // Validate input
        if (newContent == null || newContent.trim().isEmpty()) {
            return false;
        }
        
        // Check if enquiry exists
        if (!enquiries.contains(enquiry)) {
            return false;
        }
        
        // Check if enquiry has replies (can't edit if it does)
        if (enquiry.getReplies() != null && !enquiry.getReplies().isEmpty()) {
            return false;
        }
        
        // Update content
        enquiry.setContent(newContent);
        
        // Save to file
        return saveEnquiries();
    }
    
    /**
     * Delete an enquiry
     * @param enquiry the enquiry to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteEnquiry(Enquiry enquiry) {
        // Check if enquiry exists
        if (!enquiries.contains(enquiry)) {
            return false;
        }
        
        // Check if enquiry has replies (can't delete if it does)
        if (enquiry.getReplies() != null && !enquiry.getReplies().isEmpty()) {
            return false;
        }
        
        // Remove from list
        enquiries.remove(enquiry);
        
        // Save to file
        return saveEnquiries();
    }
    
    /**
     * Add a reply to an enquiry
     * @param enquiry the enquiry
     * @param reply the reply content
     * @param responder the user responding
     * @return true if reply was added successfully, false otherwise
     */
    public boolean addReply(Enquiry enquiry, String reply, User responder) {
        // Validate input
        if (reply == null || reply.trim().isEmpty()) {
            return false;
        }
        
        // Check if enquiry exists
        if (!enquiries.contains(enquiry)) {
            return false;
        }
        
        // Format reply with responder info
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String formattedReply = responder.getName() + " (" + responder.getRole() + ") [" + 
                             sdf.format(new Date()) + "]: " + reply;
        
        // Add reply
        enquiry.addReply(formattedReply);
        
        // Save to file
        return saveEnquiries();
    }
    
    /**
     * Load enquiries from file
     * @return list of enquiries
     */
    private List<Enquiry> loadEnquiries() {
        List<Enquiry> loadedEnquiries = new ArrayList<>();
        
        try (Scanner fileScanner = new Scanner(new File(ENQUIRIES_FILE))) {
            // Skip header if exists
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                // Fix for handling commas in content and replies
                List<String> values = parseCSVLine(line);
                if (values.size() < 5) continue; // Invalid line
                
                // Parse enquiry data
                try {
                    String enquiryID = values.get(0).trim();
                    String applicantNRIC = values.get(1).trim();
                    String projectID = values.get(2).trim();
                    long submissionDate = Long.parseLong(values.get(3).trim());
                    String content = values.get(4).trim();
                    
                    // Parse replies (if any)
                    List<String> replies = new ArrayList<>();
                    if (values.size() > 5 && !values.get(5).trim().isEmpty()) {
                        String[] repliesArr = values.get(5).split("\\|");
                        Collections.addAll(replies, repliesArr);
                    }
                    
                    // Find applicant and project (in a real system, these would come from repositories)
                    // For now, create placeholders
                    Applicant applicant = new Applicant(
                        "Applicant " + applicantNRIC, // Placeholder name
                        applicantNRIC,
                        "password",
                        30, // Placeholder age
                        "Single", // Placeholder marital status
                        "Applicant"
                    );
                    
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
                    
                    // Create enquiry
                    Enquiry enquiry = new Enquiry(enquiryID, applicant, project, content, new Date(submissionDate));
                    
                    // Add replies
                    for (String reply : replies) {
                        enquiry.addReply(reply.trim());
                    }
                    
                    // Add to list
                    loadedEnquiries.add(enquiry);
                    
                } catch (Exception e) {
                    System.err.println("Error parsing enquiry data: " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Enquiries file not found. Starting with empty list.");
        }
        
        return loadedEnquiries;
    }
    
    /**
     * Parse a CSV line, handling commas in quoted fields
     * @param line the CSV line
     * @return list of field values
     */
    private List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // Add the last field
        result.add(currentField.toString());
        
        return result;
    }
    
    /**
     * Save enquiries to file
     * @return true if successful, false otherwise
     */
    private boolean saveEnquiries() {
        try {
            // Create directories if they don't exist
            File directory = new File("files/resources");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(ENQUIRIES_FILE))) {
                // Write header
                writer.println("EnquiryID,ApplicantNRIC,ProjectID,SubmissionDate,Content,Replies");
                
                // Write enquiries
                for (Enquiry enquiry : enquiries) {
                    // Handle commas in content by quoting if necessary
                    String content = enquiry.getContent();
                    if (content.contains(",")) {
                        content = "\"" + content + "\"";
                    }
                    
                    writer.print(
                        enquiry.getEnquiryID() + "," +
                        enquiry.getApplicant().getNRIC() + "," +
                        enquiry.getProject().getProjectID() + "," +
                        enquiry.getSubmissionDate().getTime() + "," +
                        content
                    );
                    
                    // Add replies if any
                    List<String> replies = enquiry.getReplies();
                    if (replies != null && !replies.isEmpty()) {
                        writer.print(",");
                        for (int i = 0; i < replies.size(); i++) {
                            if (i > 0) writer.print("|");
                            writer.print(replies.get(i).replace(",", "\\,").replace("|", "\\|"));
                        }
                    }
                    
                    writer.println();
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving enquiries: " + e.getMessage());
            return false;
        }
    }
}