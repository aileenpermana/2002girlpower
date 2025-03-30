import java.util.Scanner;

public class LoginUI(){
    Scanner sc = new Scanner(System.in);

    System.out.println("Welcome to the BTO system! Please login to continue.");
    System.out.print("Enter your NRIC: ");
    String NRIC = sc.nextLine();
    System.out.print("Enter your password(default is : 'password'): ");
    String password = sc.nextLine();
    System.out.print("Enter your age: ");
    int age = sc.nextInt();
    sc.nextLine();
    System.out.print("Enter your marital status: ");
    String maritalStatus = sc.nextLine();
    RegistrationControl registControl = new RegistrationControl(NRIC, password, age, maritalStatus);
    if (registControl.validateCredentials()) {
        registControl.registerUser();
        System.out.println("Setting Up Profile...");
    }

        sc.close();
}