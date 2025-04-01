package entity;

import java.util.*;

/**
 * Represents an enquiry submitted by an applicant about a project.
 */
public class Enquiry {
    private String enquiryID;
    private Applicant applicant;
    private Project project;
    private String content;
    private Date submissionDate;
    private List<String> replies;
    
    /**
     * Default constructor
     */
    public Enquiry() {
        this.replies = new ArrayList<>();
    }
    
    /**
     * Constructor with parameters
     * @param enquiryID the enquiry ID
     * @param applicant the applicant
     * @param project the project
     * @param content the content
     * @param submissionDate the submission date
     */
    public Enquiry(String enquiryID, Applicant applicant, Project project, String content, Date submissionDate) {
        this.enquiryID = enquiryID;
        this.applicant = applicant;
        this.project = project;
        this.content = content;
        this.submissionDate = submissionDate;
        this.replies = new ArrayList<>();
    }
    
    /**
     * Get the enquiry ID
     * @return enquiry ID
     */
    public String getEnquiryID() {
        return enquiryID;
    }
    
    /**
     * Set the enquiry ID
     * @param enquiryID the enquiry ID
     */
    public void setEnquiryID(String enquiryID) {
        this.enquiryID = enquiryID;
    }
    
    /**
     * Get the applicant
     * @return applicant
     */
    public Applicant getApplicant() {
        return applicant;
    }
    
    /**
     * Set the applicant
     * @param applicant the applicant
     */
    public void setApplicant(Applicant applicant) {
        this.applicant = applicant;
    }
    
    /**
     * Get the project
     * @return project
     */
    public Project getProject() {
        return project;
    }
    
    /**
     * Set the project
     * @param project the project
     */
    public void setProject(Project project) {
        this.project = project;
    }
    
    /**
     * Get the content
     * @return content
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Set the content
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * Get the submission date
     * @return submission date
     */
    public Date getSubmissionDate() {
        return submissionDate;
    }
    
    /**
     * Set the submission date
     * @param submissionDate the submission date
     */
    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }
    
    /**
     * Get the replies
     * @return list of replies
     */
    public List<String> getReplies() {
        return replies;
    }
    
    /**
     * Set the replies
     * @param replies the list of replies
     */
    public void setReplies(List<String> replies) {
        this.replies = replies;
    }
    
    /**
     * Add a reply
     * @param reply the reply to add
     */
    public void addReply(String reply) {
        if (this.replies == null) {
            this.replies = new ArrayList<>();
        }
        this.replies.add(reply);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enquiry enquiry = (Enquiry) o;
        return Objects.equals(enquiryID, enquiry.enquiryID);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(enquiryID);
    }
    
    @Override
    public String toString() {
        return "Enquiry{" +
                "enquiryID='" + enquiryID + '\'' +
                ", applicant=" + applicant.getName() +
                ", project=" + project.getProjectName() +
                ", submissionDate=" + submissionDate +
                '}';
    }
}