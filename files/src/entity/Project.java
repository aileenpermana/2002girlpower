package entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a BTO project in the HDB system.
 */
public class Project {
    private String projectID;
    private String projectName;
    private String neighborhood;
    private Map<FlatType, Integer> totalUnits;
    private Map<FlatType, Integer> availableUnits;
    private Date applicationOpenDate;
    private Date applicationCloseDate;
    private HDBManager managerInCharge;
    private List<HDBOfficer> officers;
    private int maxOfficerSlots;
    private int availableOfficerSlots;
    private boolean isVisible;
    
    /**
     * Constructor for Project
     */
    public Project(String projectID, String projectName, String neighborhood, 
                   Map<FlatType, Integer> totalUnits, Date applicationOpenDate, 
                   Date applicationCloseDate, HDBManager managerInCharge, int maxOfficerSlots) {
        this.projectID = projectID;
        this.projectName = projectName;
        this.neighborhood = neighborhood;
        this.totalUnits = new HashMap<>(totalUnits);
        this.availableUnits = new HashMap<>(totalUnits); // Initially available = total
        this.applicationOpenDate = applicationOpenDate;
        this.applicationCloseDate = applicationCloseDate;
        this.managerInCharge = managerInCharge;
        this.officers = new ArrayList<>();
        this.maxOfficerSlots = maxOfficerSlots;
        this.availableOfficerSlots = maxOfficerSlots;
        this.isVisible = false; // Default to not visible
    }
    
    /**
     * Get project ID
     * @return project ID
     */
    public String getProjectID() {
        return projectID;
    }
    
    /**
     * Set project name
     * @param projectName new project name
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    /**
     * Get project name
     * @return project name
     */
    public String getProjectName() {
        return projectName;
    }
    
    /**
     * Set neighborhood
     * @param neighborhood new neighborhood
     */
    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }
    
    /**
     * Get neighborhood
     * @return neighborhood
     */
    public String getNeighborhood() {
        return neighborhood;
    }
    
    /**
     * Get list of flat types available in this project
     * @return list of flat types
     */
    public List<FlatType> getFlatTypes() {
        return new ArrayList<>(totalUnits.keySet());
    }
    
    /**
     * Set the number of units for a specific flat type
     * @param flatType the flat type
     * @param count the new count
     */
    public void setNumberOfUnitsByType(FlatType flatType, int count) {
        totalUnits.put(flatType, count);
        
        // Update available units as well
        // This assumes no units have been booked yet
        availableUnits.put(flatType, count);
    }
    
    /**
     * Get the number of available units by flat type
     * @param flatType the flat type
     * @return number of available units
     */
    public int getAvailableUnitsByType(FlatType flatType) {
        return availableUnits.getOrDefault(flatType, 0);
    }
    
    /**
     * Get the total number of units by flat type
     * @param flatType the flat type
     * @return total number of units
     */
    public int getTotalUnitsByType(FlatType flatType) {
        return totalUnits.getOrDefault(flatType, 0);
    }
    
    /**
     * Decrement available units for a flat type
     * @param flatType the flat type
     * @return true if successful, false if no units available
     */
    public boolean decrementAvailableUnits(FlatType flatType) {
        int current = availableUnits.getOrDefault(flatType, 0);
        if (current > 0) {
            availableUnits.put(flatType, current - 1);
            return true;
        }
        return false;
    }
    
    /**
     * Increment available units for a flat type
     * @param flatType the flat type
     */
    public void incrementAvailableUnits(FlatType flatType) {
        int current = availableUnits.getOrDefault(flatType, 0);
        int max = totalUnits.getOrDefault(flatType, 0);
        if (current < max) {
            availableUnits.put(flatType, current + 1);
        }
    }
    
    /**
     * Set available units by flat type
     * @param flatType the flat type
     * @param count the new count
     */
    public void setAvailableUnitsByType(FlatType flatType, int count) {
        int max = totalUnits.getOrDefault(flatType, 0);
        availableUnits.put(flatType, Math.min(count, max));
    }
    
    /**
     * Set application open date
     * @param applicationOpenDate new application open date
     */
    public void setApplicationOpenDate(Date applicationOpenDate) {
        this.applicationOpenDate = applicationOpenDate;
    }
    
    /**
     * Get application open date
     * @return application open date
     */
    public Date getApplicationOpenDate() {
        return applicationOpenDate;
    }
    
    /**
     * Set application close date
     * @param applicationCloseDate new application close date
     */
    public void setApplicationCloseDate(Date applicationCloseDate) {
        this.applicationCloseDate = applicationCloseDate;
    }
    
    /**
     * Get application close date
     * @return application close date
     */
    public Date getApplicationCloseDate() {
        return applicationCloseDate;
    }
    
    /**
     * Get visibility status
     * @return true if visible, false otherwise
     */
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * Set visibility status
     * @param visible new visibility status
     */
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
    
    /**
     * Get manager in charge
     * @return manager in charge
     */
    public HDBManager getManagerInCharge() {
        return managerInCharge;
    }
    
    /**
     * Set officer slots
     * @param maxOfficerSlots new max officer slots
     */
    public void setOfficerSlots(int maxOfficerSlots) {
        // Set new max slots and adjust available slots accordingly
        int diff = maxOfficerSlots - this.maxOfficerSlots;
        this.maxOfficerSlots = maxOfficerSlots;
        this.availableOfficerSlots += diff;
        
        // Ensure available slots is never negative
        if (this.availableOfficerSlots < 0) {
            this.availableOfficerSlots = 0;
        }
    }
    
    /**
     * Get available officer slots
     * @return available officer slots
     */
    public int getAvailableOfficerSlots() {
        return availableOfficerSlots;
    }
    
    /**
     * Decrement available officer slots
     * @return true if successful, false if no slots available
     */
    public boolean decrementOfficerSlots() {
        if (availableOfficerSlots > 0) {
            availableOfficerSlots--;
            return true;
        }
        return false;
    }
    
    /**
     * Add an officer to this project
     * @param officer the officer to add
     */
    public void addOfficer(HDBOfficer officer) {
        if (!officers.contains(officer)) {
            officers.add(officer);
        }
    }
    
    /**
     * Get all officers assigned to this project
     * @return list of officers
     */
    public List<HDBOfficer> getOfficers() {
        return new ArrayList<>(officers);
    }
    
    /**
     * Check if project is open for applications
     * @return true if open, false otherwise
     */
    public boolean isOpenForApplication() {
        Date now = new Date();
        return now.after(applicationOpenDate) && now.before(applicationCloseDate) && isVisible;
    }
    
    /**
     * Check if a user is eligible to apply for this project
     * @param user the user to check
     * @return true if eligible, false otherwise
     */
    public boolean checkEligibility(User user) {
        // Singles, 35 years old and above, can ONLY apply for 2-Room
        if (user.getMaritalStatus() == MaritalStatus.SINGLE) {
            if (user.getAge() < 35) {
                return false; // Singles must be at least 35
            }
            
            // Check if the project has 2-Room flats
            return hasFlatType(FlatType.TWO_ROOM);
        } 
        // Married, 21 years old and above, can apply for any flat type
        else if (user.getMaritalStatus() == MaritalStatus.MARRIED) {
            return user.getAge() >= 21; // Married applicants must be at least 21
        }
        
        return false;
    }
    
    /**
     * Check if project has a specific flat type
     * @param flatType the flat type to check
     * @return true if available, false otherwise
     */
    public boolean hasFlatType(FlatType flatType) {
        return totalUnits.containsKey(flatType) && totalUnits.get(flatType) > 0;
    }
    
    @Override
    public String toString() {
        return "Project{" +
                "projectID='" + projectID + '\'' +
                ", projectName='" + projectName + '\'' +
                ", neighborhood='" + neighborhood + '\'' +
                ", applicationOpenDate=" + applicationOpenDate +
                ", applicationCloseDate=" + applicationCloseDate +
                '}';
    }
}