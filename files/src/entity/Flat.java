package entity;

/**
 * Represents a flat in a BTO project.
 */
public class Flat {
    private String flatID;
    private Project project;
    private FlatType type;
    private boolean isBooked;
    private Application bookedByApplication;
    
    /**
     * Constructor for Flat
     */
    public Flat(String flatID, Project project, FlatType type) {
        this.flatID = flatID;
        this.project = project;
        this.type = type;
        this.isBooked = false;
        this.bookedByApplication = null;
    }
    
    /**
     * Get flat ID
     * @return flat ID
     */
    public String getFlatID() {
        return flatID;
    }
    
    /**
     * Get project
     * @return the project this flat belongs to
     */
    public Project getProject() {
        return project;
    }
    
    /**
     * Get flat type
     * @return flat type
     */
    public FlatType getType() {
        return type;
    }
    
    /**
     * Check if flat is booked
     * @return true if booked, false otherwise
     */
    public boolean isBooked() {
        return isBooked;
    }
    
    /**
     * Set the application that booked this flat
     * @param application the application
     */
    public void setBookedByApplication(Application application) {
        this.bookedByApplication = application;
        this.isBooked = (application != null);
    }
    
    /**
     * Get the application that booked this flat
     * @return the application or null if not booked
     */
    public Application getBookedByApplication() {
        return bookedByApplication;
    }
    
    @Override
    public String toString() {
        return "Flat{" +
                "flatID='" + flatID + '\'' +
                ", project=" + project.getProjectName() +
                ", type=" + type +
                ", isBooked=" + isBooked +
                '}';
    }
}