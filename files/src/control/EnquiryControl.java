package control;

import entity.*;
import java.io.*;
import java.util.*;

/**
 * Controls operations related to Enquiries in the BTO system.
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
        // Create new enquiry
        Enquiry enquiry = new Enquiry();
        
        // Set basic properties
        enquiry.setEnquiryID(generateEnquiryID(applicant, project));
        enquiry.setApplicant(applicant);
        enquiry.setProject(project);
        enquiry.setContent(content);
        enquiry.setSubmissionDate(new Date());
        enquiry.setReplies(new ArrayList<>());
        
        // Add to list
        enquiries.add(enquiry);
        
        // Save to file
        if (saveEnquiries()) {
            return enquiry;
        }
        
        return null;
    }
    
    /**
     * Edit an existing enquiry
     * @param enquiry the enquiry to edit
     * @param newContent the new content
     * @return true if edit was successful, false otherwise
     */
    public boolean editEnquiry(Enquiry enquiry, String newContent) {
        // Check if enquiry exists
        if (!enquiries.contains(enquiry)) {
            return false;
        }
        
        // Check if enquiry has replies (can't edit if it does)
        if (!enquiry.getReplies().isEmpty()) {
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
        if (!enquiry.getReplies().isEmpty()) {
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
        // Check if enquiry exists
        if (!enquiries.contains(enquiry)) {
            return false;
        }
        
        // Format reply with responder info
        String formattedReply = responder.getName() + " (" + responder.getRole() + "): " + reply;
        
        // Add reply
        enquiry.getReplies().add(formattedReply);
        
        // Save to file
        return saveEnquiries();
    }
    
    /**
     * Generate an enquiry ID
     * @param applicant the applicant
     * @param project the project
     * @return a unique enquiry ID
     */
    private String generateEnquiryID(Applicant applicant, Project project) {
        // Simple algorithm to generate an enquiry ID
        // In a real system, you would use a more robust method
        return "ENQ-" + applicant.getNRIC().substring(1, 5) + "-" + project.getProjectID().substring(0, 3) + "-" + System.currentTimeMillis() % 1000;
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
                
                String[] values = line.split(",", 6); // Limit to 6 to handle commas in content and replies
                if (values.length < 5) continue; // Invalid line
                
                // Parse enquiry data
                try {
                    String enquiryID = values[0].trim();
                    String applicantNRIC = values[1].trim();
                    String projectID = values[2].trim();
                    long submissionDate = Long.parseLong(values[3].trim());
                    String content = values[4].trim();
                    
                    // Parse replies (if any)
                    List<String> replies = new ArrayList<>();
                    if (values.length > 5 && !values[5].trim().isEmpty()) {
                        String[] repliesArr = values[5].split("\\|");
                        for (String reply : repliesArr) {
                            replies.add(reply.trim());
                        }
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
                    Enquiry enquiry = new Enquiry();
                    enquiry.setEnquiryID(enquiryID);
                    enquiry.setApplicant(applicant);
                    enquiry.setProject(project);
                    enquiry.setContent(content);
                    enquiry.setSubmissionDate(new Date(submissionDate));
                    enquiry.setReplies(replies);
                    
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
                    writer.print(
                        enquiry.getEnquiryID() + "," +
                        enquiry.getApplicant().getNRIC() + "," +
                        enquiry.getProject().getProjectID() + "," +
                        enquiry.getSubmissionDate().getTime() + "," +
                        enquiry.getContent()
                    );
                    
                    // Add replies if any
                    if (!enquiry.getReplies().isEmpty()) {
                        writer.print(",");
                        for (int i = 0; i < enquiry.getReplies().size(); i++) {
                            if (i > 0) writer.print("|");
                            writer.print(enquiry.getReplies().get(i));
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