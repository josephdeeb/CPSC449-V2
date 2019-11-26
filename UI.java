
import java.io.IOException;
import java.util.Scanner;

public class UI {
    Scanner input;
    
    public UI() {
        this.input = new Scanner(System.in);
        input.nextLine();
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
                throw new IOException("ERROR: You did not type a number associated with an available option.");
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to try again");
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
    
    public UIPacket login(short state) {
    	String username;
    	String password;
    	if (state == -1) {
    		System.out.println("ERROR: Server tells us we sent a bad message.  Please press enter to continue");
    		input.nextLine();
    		return new UIPacket("startup");
    	}
    	if (state == 0) {
	        printTitle("Login");
	        System.out.println("Please enter your username:");
	        username = input.nextLine();
	        username = input.nextLine();
	        System.out.println("Please enter your password:");
	        password = input.nextLine();
	        return new UIPacket("login", new String[] {username, password});
    	}
    	else if (state == 1) {
    		System.out.println("Login successful!  Please press enter to continue");
    		input.nextLine();
    		return new UIPacket("mainmenu");
    	}
    	
    	else if (state == 2) {
    		System.out.println("ERROR: Invalid username and password combination.  Please press enter to continue");
    		input.nextLine();
    		return new UIPacket("startup");
    	}
    	
    	return new UIPacket("startup");
    }
    
    public UIPacket register(short state) {
    	if (state == -1) {
    		System.out.println("ERROR: Server tells us we sent a bad message.  Please press enter to continue");
    		input.nextLine();
    		return new UIPacket("startup");
    	}
        // Initial registration
        if (state == 0) {
            String username = "";
            String password = "";
            printTitle("Register a New Account");
            System.out.println("Please enter a username (Cannot contain commas, max 64 characters): ");
            username = input.nextLine();
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
        else {
        	return new UIPacket("register");
        }
    }
    
    public UIPacket mainMenu() {
        int selection = -1;
        printTitle("Main Menu\nLogged in as " + Client.username);
        System.out.println("Please choose one of the following options: ");
        System.out.println("1\t: Chats Menu");
        System.out.println("2\t: Friends List");
        System.out.println("3\t: Account Settings");
        System.out.println("4\t: Logout");
        try {
            selection = input.nextInt();
            if (selection < 1 || selection > 4)
                throw new IOException("ERROR: You did not type a number associated with an available option.");
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to try again");
            input.nextLine();
            return new UIPacket("mainmenu");
        }
        
        switch (selection) {
            case 1:
                return new UIPacket("chatsmenu");
            case 2:
                return new UIPacket("friendslist");
            case 3:
                return new UIPacket("accountsettings");
            case 4:
                return new UIPacket("logout");
            default:
                System.out.println("This should've been impossible to reach...");
                return new UIPacket("mainmenu");
        }
    }
    
    public void printTitle(String title) {
        System.out.println("\n____________\n\n" + title + "\n____________\n");
    }
}
