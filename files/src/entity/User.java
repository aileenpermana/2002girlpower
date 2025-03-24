package entity;

/**
 * A class that represents a user
 */
public class User {
    /**
     * The NRIC of the user.
     */
    private String NRIC;
    /**
     * The password of the user.
     */
    private String password;
    /**
     * The age of the user.
     */
    private int age;
    /**
     * The marital status of the user.
     */
    private String maritalStatus;
    /**
     * The role of the user.
     */
    private String role;
    
    /**
     * Gets age of user.
     * 
     * @return the age of the user
     */
    public int getAge() {
        return this.age;
    }
}
