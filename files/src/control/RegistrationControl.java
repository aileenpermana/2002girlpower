public class RegistrationControl(){
    // Constructor
    public RegistrationControl(String NRIC, String password, int age, String maritalStatus) {
        this.NRIC = NRIC;
        this.password = password;
        this.age = age;
        this.maritalStatus = maritalStatus;
    }

    // Method to validate user credentials
    public boolean validateCredentials() {
        // checkNRIC(NRIC);
        if (NRIC == null || NRIC.isEmpty()) {
            System.out.println("NRIC cannot be empty.");
            return false;
        }
        if (NRIC.length() != 9 || !NRIC.matches("[ST]\\d{7}[A-Z]")) {
            System.out.println("Invalid NRIC format.");
            return false;
        }
        // checkPassword(password);
        if (password == null || password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }
        if (password != 'password'){
            System.out.println("Wrong default password. Please try again.");
            return false;
        }
        // checkAge(age);
        if (age <= 0) {
            System.out.println("Age must be a positive number.");
            return false;
        }
        if (age < 21) {
            System.out.println("You must be at least 21 years old to register.");
            return false;
        }
        // checkMaritalStatus(maritalStatus);
        if (!maritalStatus.equals("Single") && !maritalStatus.equals("Married")) {
            System.out.println("Invalid marital status. Please enter 'Single' or 'Married'.");
            return false;
        }
        return true; 
    }


    public void registerUser() {
        User newUser = new User(this.NRIC, this.password, this.age, this.maritalStatus, 'Applicant');
        // Save the new user to the database or perform any other necessary actions
        System.out.println("User registered successfully!");
    }
}