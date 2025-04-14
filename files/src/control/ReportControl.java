package control;

import entity.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controls operations related to Report generation in the BTO system.
 * Demonstrates Single Responsibility Principle by focusing only on report-related operations.
 */
public class ReportControl {
    
    /**
     * Generate an application report
     * @param project the project
     * @param applications the applications to include
     * @param filters the filters to apply
     * @return the generated report
     */
    public Report generateApplicationReport(Project project, List<Application> applications, Map<String, Object> filters) {
        // Apply filters to applications
        List<Application> filteredApplications = applyFilters(applications, filters);
        
        // Generate report ID
        String reportID = "RPT-APP-" + project.getProjectID() + "-" + System.currentTimeMillis() % 10000;
        
        // Create report
        Report report = new Report();
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
    public Report generateBookingReport(Project project, List<Application> applications, Map<String, Object> filters) {
        // Ensure only booked applications are included
        List<Application> bookedApplications = applications.stream()
            .filter(app -> app.getStatus() == ApplicationStatus.BOOKED && app.getBookedFlat() != null)
            .collect(Collectors.toList());
        
        // Apply filters to applications
        List<Application> filteredApplications = applyFilters(bookedApplications, filters);
        
        // Generate report ID
        String reportID = "RPT-BOOK-" + project.getProjectID() + "-" + System.currentTimeMillis() % 10000;
        
        // Create report
        Report report = new Report();
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
        
        // Using modern Java streams for filtering
        return applications.stream()
            .filter(app -> filterByMaritalStatus(app, filters))
            .filter(app -> filterByAge(app, filters))
            .filter(app -> filterByStatus(app, filters))
            .filter(app -> filterByFlatType(app, filters))
            .collect(Collectors.toList());
    }
    
    /**
     * Filter application by marital status
     * @param app the application
     * @param filters the filters
     * @return true if application passes the filter, false otherwise
     */
    private boolean filterByMaritalStatus(Application app, Map<String, Object> filters) {
        if (!filters.containsKey("maritalStatus")) {
            return true; // No filter applied
        }
        
        String maritalStatusStr = (String) filters.get("maritalStatus");
        MaritalStatus status = MaritalStatus.fromString(maritalStatusStr);
        
        return status == null || app.getApplicant().getMaritalStatus() == status;
    }
    
    /**
     * Filter application by age
     * @param app the application
     * @param filters the filters
     * @return true if application passes the filter, false otherwise
     */
    private boolean filterByAge(Application app, Map<String, Object> filters) {
        int age = app.getApplicant().getAge();
        
        // Check min age
        if (filters.containsKey("minAge")) {
            int minAge = (int) filters.get("minAge");
            if (age < minAge) {
                return false;
            }
        }
        
        // Check max age
        if (filters.containsKey("maxAge")) {
            int maxAge = (int) filters.get("maxAge");
            if (age > maxAge) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Filter application by status
     * @param app the application
     * @param filters the filters
     * @return true if application passes the filter, false otherwise
     */
    private boolean filterByStatus(Application app, Map<String, Object> filters) {
        if (!filters.containsKey("status")) {
            return true; // No filter applied
        }
        
        String statusStr = (String) filters.get("status");
        
        try {
            ApplicationStatus status = ApplicationStatus.valueOf(statusStr.toUpperCase());
            return app.getStatus() == status;
        } catch (IllegalArgumentException e) {
            // Invalid status value, don't filter
            return true;
        }
    }
    
    /**
     * Filter application by flat type
     * @param app the application
     * @param filters the filters
     * @return true if application passes the filter, false otherwise
     */
    private boolean filterByFlatType(Application app, Map<String, Object> filters) {
        if (!filters.containsKey("flatType")) {
            return true; // No filter applied
        }
        
        String flatTypeStr = (String) filters.get("flatType");
        FlatType type = null;
        
        if (flatTypeStr.equalsIgnoreCase("2-Room")) {
            type = FlatType.TWO_ROOM;
        } else if (flatTypeStr.equalsIgnoreCase("3-Room")) {
            type = FlatType.THREE_ROOM;
        }
        
        if (type == null) {
            return true; // Invalid flat type, don't filter
        }
        
        Flat bookedFlat = app.getBookedFlat();
        return bookedFlat == null || bookedFlat.getType() == type;
    }
    
    /**
     * Create a summary of applications by status
     * @param applications list of applications
     * @return map of status to count
     */
    public Map<ApplicationStatus, Integer> summarizeByStatus(List<Application> applications) {
        // Use modern Java groupingBy collector to create summary
        return applications.stream()
            .collect(Collectors.groupingBy(
                Application::getStatus,
                Collectors.summingInt(app -> 1)
            ));
    }
    
    /**
     * Create a summary of applications by marital status
     * @param applications list of applications
     * @return map of marital status to count
     */
    public Map<MaritalStatus, Integer> summarizeByMaritalStatus(List<Application> applications) {
        // Use modern Java groupingBy collector to create summary
        return applications.stream()
            .collect(Collectors.groupingBy(
                app -> app.getApplicant().getMaritalStatus(),
                Collectors.summingInt(app -> 1)
            ));
    }
    
    /**
     * Create a summary of applications by flat type (for booked applications)
     * @param applications list of applications
     * @return map of flat type to count
     */
    public Map<FlatType, Integer> summarizeByFlatType(List<Application> applications) {
        // Use modern Java streams and collectors
        return applications.stream()
            .filter(app -> app.getBookedFlat() != null)
            .collect(Collectors.groupingBy(
                app -> app.getBookedFlat().getType(),
                Collectors.summingInt(app -> 1)
            ));
    }
}