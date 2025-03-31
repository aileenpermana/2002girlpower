import boundary.LoginUI;

public class App {
    
    public static void main(String[] args) {
        LoginUI loginUI = new LoginUI();
        boolean shouldContinue = true;
        while (shouldContinue) {
            shouldContinue = loginUI.displayLoginMenu();
        }
        loginUI.close();
    }
}
