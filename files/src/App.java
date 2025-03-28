import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        //first time users -- later put in loginUI
        System.out.println("Welcome to the BTO system! Please login to continue.");
        System.out.print("Enter your NRIC: ");
        String NRIC = sc.nextLine();
        System.out.print("Enter your password(default is : 'password'): ");
        String password = sc.nextLine();
        if (password == "password") {
            System.out.println("Welcome! " + NRIC);
        } else {
            System.out.println("Invalid password. Please try again.");
        }
        System.out.print("Enter your age: ");
        int age = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter your marital status: ");
        String maritalStatus = sc.nextLine();
        if (maritalStatus != "Single" || maritalStatus != "Married") {
            System.out.println("Invalid marital status. Please try again.");
        } else {
            User newUser = new User(NRIC, password, age, maritalStatus, "Applicant");
            System.out.println("User created successfully!");
            System.out.println("Setting Up Profile...");
        }

        

        sc.close();
