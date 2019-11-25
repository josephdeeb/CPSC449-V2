
import java.io.IOException;
import java.util.Scanner;

public class UI {
    Scanner input;
    
    public UI() {
        this.input = new Scanner(System.in);
    }
    
    
    /*
     * Given a ui string, returns the packet from that UI
     */
    
    public UIPacket startup() {
        int selection = -1;
        printTitle("Welcome to Generic Messaging System #2");
        System.out.println("Please select one of the following options by typing the number associated with it:");
        System.out.println("1\t: Log in");
        System.out.println("2\t: Register");
        System.out.println("3\t: Exit program");
        try {
            selection = input.nextInt();
            if (selection < 1 || selection > 3)
                throw new IOException("");
        } catch (Exception e) {
            System.out.println("ERROR: You did not type a number associated with an available option.  Please press enter to try again");
            input.nextLine();
            return new UIPacket("startup");
        }
        switch (selection) {
            case 1:
                return new UIPacket("login");
                
            case 2:
                return new UIPacket("register");
                
            case 3:
                return new UIPacket("exit");
            
            default:
                System.out.println("ERROR: How the hell did you get here");
                return new UIPacket("startup");
        }
    }
    
    public UIPacket login() {
        
    }
    
    public UIPacket register(short state) {
        // Initial registration
        if (state == 0) {
            String username = "";
            String password = "";
            printTitle("Register a New Account");
            System.out.println("Please enter a username (Cannot contain commas, max 64 characters): ");
            username = input.nextLine();
            System.out.println("\nPlease enter a password (Cannot contain commas, max 64 characters): ");
            password = input.nextLine();
            
            return new UIPacket("register", new String[]{username, password});
        }
        // Successful registration!
        else if (state == 1) {
            System.out.println("Registration successful!\nPlease press enter to continue");
            input.nextLine();
            return new UIPacket("startup");
        }
        // Username is already taken
        else if (state == 2) {
            System.out.println("Unfortunately the username you selected has already been taken.\nPlease press enter to return to the main menu");
            input.nextLine();
            return new UIPacket("startup");
        }
        // Username or password contains invalid characters / too long
        else if (state == 3) {
            System.out.println("The username and/or password you have chosen is invalid.  It may contain invalid characters, or may be too long.\nPlease press enter to return to the main menu");
            input.nextLine();
            return new UIPacket("startup");
        }
    }
    
    public void printTitle(String title) {
        System.out.println("\n____________" + title + "\n____________\n");
    }
}
