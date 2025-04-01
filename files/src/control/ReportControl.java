package control;

import entity.*;
import java.util.*;

/**
 * Controls operations related to Report generation in the BTO system.
 */
public class ReportControl {
    
    /**
     * Generate an application report
     * @param project the project
     * @param applications the applications to include
     * @param filters the filters to apply
     * @return the generated report
     */
    public entity.Report generateApplicationReport(Project project, List<Application> applications, Map<String, Object> filters) {
        // Apply filters to applications
        List<Application> filteredApplications = applyFilters(applications, filters);
        
        // Generate report ID
        String reportID = "RPT-APP-" + project.getProjectID() + "-" + System.currentTimeMillis() % 10000;
        
        // Create report
        entity.Report report = new entity.Report();
        report.setReportID(reportID);
        report.setApplications(filteredApplications);
        report.setGenerationDate(new Date());
        report.setCriteria(new HashMap<>(filters)); // Clone filters
        
        return report;
    }
    
    /**
     * Generate a booking report
     * @param project the project
     * @param applications the booked applications to include
     * @param filters the filters to apply
     * @return the generated report
     */
    public entity.Report generateBookingReport(Project project, List<Application> applications, Map<String, Object> filters) {
        // Ensure only booked applications are included
        List<Application> bookedApplications = new ArrayList<>();
        for (Application app : applications) {
            if (app.getStatus() == ApplicationStatus.BOOKED && app.getBookedFlat() != null) {
                bookedApplications.add(app);
            }
        }
        
        // Apply filters to applications
        List<Application> filteredApplications = applyFilters(bookedApplications, filters);
        
        // Generate report ID
        String reportID = "RPT-BOOK-" + project.getProjectID() + "-" + System.currentTimeMillis() % 10000;
        
        // Create report
        entity.Report report = new entity.Report();
        report.setReportID(reportID);
        report.setApplications(filteredApplications);
        report.setGenerationDate(new Date());
        report.setCriteria(new HashMap<>(filters)); // Clone filters
        
        return report;
    }
    
    /**
     * Apply filters to applications
     * @param applications the applications to filter
     * @param filters the filters to apply
     * @return the filtered list of applications
     */
    private List<Application> applyFilters(List<Application> applications, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>(applications); // No filters, return all
        }
        
        List<Application> filteredList = new ArrayList<>(applications);
        
        // Apply marital status filter
        if (filters.containsKey("maritalStatus")) {
            String maritalStatusStr = (String) filters.get("maritalStatus");
            
            // Make maritalStatus final for use in lambda
            final MaritalStatus maritalStatus;
            try {
                maritalStatus = MaritalStatus.fromString(maritalStatusStr);
                
                if (maritalStatus != null) {
                    filteredList.removeIf(app -> app.getApplicant().getMaritalStatus() != maritalStatus);
                }
            } catch (IllegalArgumentException e) {
                // Invalid value, ignore filter
            }
        }
        
        // Apply min age filter
        if (filters.containsKey("minAge")) {
            int minAge = (int) filters.get("minAge");
            filteredList.removeIf(app -> app.getApplicant().getAge() < minAge);
        }
        
        // Apply max age filter
        if (filters.containsKey("maxAge")) {
            int maxAge = (int) filters.get("maxAge");
            filteredList.removeIf(app -> app.getApplicant().getAge() > maxAge);
        }
        
        // Apply status filter
        if (filters.containsKey("status")) {
            String statusStr = (String) filters.get("status");
            
            // Try to convert to enum
            try {
                final ApplicationStatus status = ApplicationStatus.valueOf(statusStr.toUpperCase());
                filteredList.removeIf(app -> app.getStatus() != status);
            } catch (IllegalArgumentException e) {
                // Invalid value, ignore filter
            }
        }
        
        // Apply flat type filter (for booked applications)
        if (filters.containsKey("flatType")) {
            final String flatTypeStr = (String) filters.get("flatType");
            
            // Determine flat type
            filteredList.removeIf(app -> {
                Flat bookedFlat = app.getBookedFlat();
                if (bookedFlat == null) {
                    return true;
                }
                if (flatTypeStr.equalsIgnoreCase("2-Room")) {
                    return bookedFlat.getType() != FlatType.TWO_ROOM;
                } else if (flatTypeStr.equalsIgnoreCase("3-Room")) {
                    return bookedFlat.getType() != FlatType.THREE_ROOM;
                }
                return true; // If flatTypeStr doesn't match any known type
            });
        }
        
        return filteredList;
    }
    
    /**
     * Create a summary of applications by status
     * @param applications list of applications
     * @return map of status to count
     */
    public Map<ApplicationStatus, Integer> summarizeByStatus(List<Application> applications) {
        Map<ApplicationStatus, Integer> summary = new HashMap<>();
        
        for (Application app : applications) {
            ApplicationStatus status = app.getStatus();
            summary.put(status, summary.getOrDefault(status, 0) + 1);
        }
        
        return summary;
    }
    
    /**
     * Create a summary of applications by marital status
     * @param applications list of applications
     * @return map of marital status to count
     */
    public Map<MaritalStatus, Integer> summarizeByMaritalStatus(List<Application> applications) {
        Map<MaritalStatus, Integer> summary = new HashMap<>();
        
        for (Application app : applications) {
            MaritalStatus status = app.getApplicant().getMaritalStatus();
            summary.put(status, summary.getOrDefault(status, 0) + 1);
        }
        
        return summary;
    }
    
    /**
     * Create a summary of applications by flat type (for booked applications)
     * @param applications list of applications
     * @return map of flat type to count
     */
    public Map<FlatType, Integer> summarizeByFlatType(List<Application> applications) {
        Map<FlatType, Integer> summary = new HashMap<>();
        
        for (Application app : applications) {
            Flat bookedFlat = app.getBookedFlat();
            if (bookedFlat != null) {
                FlatType type = bookedFlat.getType();
                summary.put(type, summary.getOrDefault(type, 0) + 1);
            }
        }
        
        return summary;
    }
}