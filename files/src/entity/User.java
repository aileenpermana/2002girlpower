package entity;
import lombok.Getter;
import lombok.Setter;

public class User {
     
    private String NRIC;
    private String password;
    private int age;
    private String maritalStatus;
    private String role;
    
    // login() as constructor so when main method creates new User object, it will automatically register new user)
    User(String NRIC, String password, int age, String maritalStatus, String role) {
        this.NRIC = NRIC;
        this.password = password;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.role = role;
    }

    
    public int getAge() {
        return this.age;
    }
    
    public String getMaritalStatus() {
        return this.maritalStatus;
    }
    
    // yet to add viewProjects method
}
