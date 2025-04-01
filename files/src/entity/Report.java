// In a new file: entity/Report.java
package entity;

import java.util.*;

/**
 * Represents a report generated in the BTO system.
 */
public class Report {
    private String reportID;
    private List<Application> applications;
    private Date generationDate;
    private Map<String, Object> criteria;
    
    /**
     * Default constructor
     */
    public Report() {
        this.criteria = new HashMap<>();
    }
    
    /**
     * Get the report ID
     * @return report ID
     */
    public String getReportID() {
        return reportID;
    }
    
    /**
     * Set the report ID
     * @param reportID the report ID
     */
    public void setReportID(String reportID) {
        this.reportID = reportID;
    }
    
    /**
     * Get the list of applications
     * @return list of applications
     */
    public List<Application> getApplications() {
        return applications;
    }
    
    /**
     * Set the list of applications
     * @param applications the applications
     */
    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }
    
    /**
     * Get the generation date
     * @return generation date
     */
    public Date getGenerationDate() {
        return generationDate;
    }
    
    /**
     * Set the generation date
     * @param generationDate the generation date
     */
    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }
    
    /**
     * Get the filter criteria
     * @return map of filter criteria
     */
    public Map<String, Object> getCriteria() {
        return criteria;
    }
    
    /**
     * Set the filter criteria
     * @param criteria the filter criteria
     */
    public void setCriteria(Map<String, Object> criteria) {
        this.criteria = criteria;
    }
}