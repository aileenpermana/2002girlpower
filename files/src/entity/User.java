package entity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * Enum for user roles
 */
enum Role {
    APPLICANT,
    HDB_OFFICER,
    HDB_MANAGER;

    public static Role fromString(String roleStr) {
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleStr);
        }
    }
}
/**
 * Base User class for the BTO system.
 * Represents common attributes and behavior for all user types.
 */
public class User {
    private String name;
    private String NRIC;
    private String password;
    private int age;
    private MaritalStatus maritalStatus;
    private Role role;
    private static final Pattern NRIC_PATTERN = Pattern.compile("^[ST]\\d{7}[A-Z]$");

    /**
     * Constructor for User object with MaritalStatus enum.
     */
    public User(String name, String NRIC, String password, int age, MaritalStatus maritalStatus, String role) {
        this.name = name;
        this.NRIC = NRIC;
        this.password = password;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.role = Role.fromString(role);
    }
    
    /**
     * Overloaded constructor that accepts marital status as String
     */
    public User(String name, String NRIC, String password, int age, String maritalStatusStr, String role) {
        this.name = name;
        this.NRIC = NRIC;
        this.password = password;
        this.age = age;
        this.maritalStatus = MaritalStatus.fromString(maritalStatusStr);
        this.role = Role.fromString(role);
    }
    
    /**
     * Validates the NRIC format.
     * @param nric the NRIC to validate
     * @throws IllegalArgumentException if the NRIC format is invalid
     */
    private void validateNRIC(String nric) {
        if (nric == null || !NRIC_PATTERN.matcher(nric).matches()) {
            throw new IllegalArgumentException("Invalid NRIC format. Must be S/T followed by 7 digits and a letter.");
        }
    }
    
    /**
     * Validates the user's age.
     * @param age the age to validate
     * @return the validated age
     * @throws IllegalArgumentException if the age is invalid (less than 0 or greater than 120)
     */
    private int validateAge(int age) {
        if (age < 0 || age > 120) {
            throw new IllegalArgumentException("Invalid age: " + age);
        }
        return age;
    }

    /**
     * Hashes the user's password using MD5.
     * @param password the password to hash
     * @return the hashed password as a hexadecimal string
     * @throws RuntimeException if the MD5 algorithm is not available
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Get user's age
     * @return age of the user
     */
    public int getAge() {
        return this.age;
    }
    
    /**
     * Get user's marital status
     * @return marital status of the user
     */
    public MaritalStatus getMaritalStatus() {
        return this.maritalStatus;
    }
    
    /**
     * Get marital status display value
     * @return string representation of marital status
     */
    public String getMaritalStatusDisplayValue() {
        return this.maritalStatus.getDisplayValue();
    }
    
    /**
     * Get user's name
     * @return name of the user
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Get user's NRIC
     * @return NRIC of the user
     */
    public String getNRIC() {
        return this.NRIC;
    }
    
    /**
     * Gets the user's hashed password.
     * @return the hashed password of the user
     */
    public String getPassword() {
        return this.password;
    }
    
    /**
     * Sets the user's password after hashing.
     * @param password the new password to set
     */
    public void setPassword(String password) {
        this.password = password;
        
    }
    
    /**
     * Get user's role
     * @return role of the user
     */
    public String getRole() {
        return this.role.name();
    }
    
    /**
     * Verifies login by comparing the input password with the stored hashed password.
     * @param inputPassword the password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean login(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    
    /**
     * Changes the user's password after validation and hashing.
     * @param newPassword the new password to set
     * @throws IllegalArgumentException if the new password is null or less than 8 characters
     */
    public void changePassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        this.password = hashPassword(newPassword);
    }
    
    /**
     * Returns a string representation of the User object.
     * @return a string containing the user's details
     */
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", NRIC='" + NRIC + '\'' +
                ", age=" + age +
                ", maritalStatus='" + maritalStatus + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}