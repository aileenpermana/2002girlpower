package entity;


public class User {
    private String name;
    private String NRIC;
    private String password;
    private int age;
    private String maritalStatus;
    private String role;
    
    // login() as constructor so when main method creates new User object, it will automatically register new user)
    public User(String name, String NRIC, String password, int age, String maritalStatus, String role) {
        this.name = name;
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

    public String getName() {
        return this.name;
    }
    
    public String getNRIC() {
        return this.NRIC;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public String getRole() {
        return this.role;
    }
    
    // yet to add viewProjects method
}
