
import java.io.File;
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
        System.out.println("4\t: Save Server Data");
        try {
            selection = Integer.parseInt(input.nextLine());
            if (selection < 1 || selection > 4)
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
                
            case 4:
                return new UIPacket("save");

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
            Client.username = username;
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
	
	public UIPacket acceptFriendRequest(short state){
		if(state == -1){
			System.out.println("Something went wrong!");
		}
		if(state == 0){
			System.out.println("Type in the uID of the user you would like to accept a friend request from:");
			String uID = input.nextLine();
			return new UIPacket("acceptfriendrequest", new String[]{uID});
		}
		if(state == 1){
			System.out.println("You are now friends!!!");
			return new UIPacket("friendslist");
		}
		return new UIPacket("friendslist");
	}
	
    
	public UIPacket friendsList(){
		int selection = -1;
        printTitle("Friends menu\nLogged in as " + Client.username);
        System.out.println("Please choose one of the following options: ");
        System.out.println("1\t: Display Friends");
        System.out.println("2\t: Send a friend request");
        System.out.println("3\t: View friend requests");
		System.out.println("4\t: Accept friend request");
		System.out.println("5\t: Back to main menu");
		
		try {
            selection = Integer.parseInt(input.nextLine());
            if (selection < 1 || selection > 5)
                throw new IOException("ERROR: You did not type a number associated with an available option.");
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to try again");
            input.nextLine();
            return new UIPacket("mainmenu");
        }
        
        switch (selection) {
            case 1:
                return new UIPacket("displayfriends");
            case 2:
                return new UIPacket("sendfriendrequest");
            case 3: 
                return new UIPacket("viewfriendrequests");
            case 4:
                return new UIPacket("acceptfriendrequest");
            case 5:
                return new UIPacket("mainmenu");
            
            default:
                System.out.println("This should've been impossible to reach...");
                return new UIPacket("mainmenu");
        }
    }
	
	
	public UIPacket accountSettings(){
		int selection = -1;
        printTitle("Accout settings\nLogged in as " + Client.username);
        System.out.println("Please choose one of the following options: ");
        System.out.println("1\t: Change username");
        System.out.println("2\t: Change password");
        System.out.println("3\t: Delete your account(No second chances! Once you choose this option - your account is gone forever)");
		System.out.println("4\t: Back to Main menu");
		
		try {
            selection = Integer.parseInt(input.nextLine());
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
                return new UIPacket("changeusername");
            case 2:
                return new UIPacket("changepassword");
            case 3: 
                return new UIPacket("deleteaccout");
            case 4:
                return new UIPacket("mainmenu");
            
            default:
                System.out.println("This should've been impossible to reach...");
                return new UIPacket("mainmenu");
        }
    }
	
	public UIPacket changeUsername(short state){
		if (state == -1) {
    		System.out.println("ERROR: Server tells us we sent a bad message.  Please press enter to continue");
    		input.nextLine();
    		return new UIPacket("accountsettings");
    	}
		else if(state == 0){
			String newName = "";
			System.out.println("Enter new userName");
			newName = input.nextLine();
			return new UIPacket ("changeusername", new String[]{newName});
		}
		else if(state == 1){
			System.out.println("Username changed! Congrats," + Client.username);
			System.out.println("Press Enter to continue...");
			input.nextLine();
			return new UIPacket("accountsettings"); 
		}
		return new UIPacket("accountsettings");
	}
	
	public UIPacket changePassword(short state){
		if (state == -1){
			System.out.println("ERROR: Server tells us we sent a bad message.  Please press enter to continue");
    		input.nextLine();
    		return new UIPacket("accountsettings");
		}
		else if(state == 0){
			String newPassword = "";
			System.out.println("Enter new password(Must be at least 1 character!)");
			newPassword = input.nextLine();
			return new UIPacket ("changepassword", new String[]{newPassword});
		}
		else if (state == 1){
			System.out.println("Your password was succesfully changed!\nPress Enter to continue");
			input.nextLine();
			return new UIPacket("accountsettings");
		}
		return new UIPacket("accountsettings");
	}
	
	public UIPacket sendFriendRequest(){
		String friend;
		System.out.println("Enter the name of the user you would like to send a friend request to:");
		friend = input.nextLine();
		return new UIPacket("sendfriendrequest", new String[] {friend} );
	}
	
	
	
    public UIPacket mainMenu() {
        int selection = -1;
        printTitle("Main Menu\nLogged in as " + Client.username);
        System.out.println("Please choose one of the following options: ");
        System.out.println("1\t: Chats Menu");
        System.out.println("2\t: Friends List");
        System.out.println("3\t: Account Settings");
        System.out.println("4\t: Logout");
        System.out.println("5\t: TEMP upload");
        System.out.println("6\t: TEMP download");
        System.out.println("7\t: Public Chats Menu");
        try {
            selection = Integer.parseInt(input.nextLine());
            if (selection < 1 || selection > 7)
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
            case 5:
                return new UIPacket("uploadfile");
            case 6:
                return new UIPacket("downloadfile");
            case 7:
            	return new UIPacket("publicchatsmenu");
            default:
                System.out.println("This should've been impossible to reach...");
                return new UIPacket("mainmenu");
        }
    }
    
    public UIPacket uploadFile() {
        printTitle("Upload File");
        System.out.println("Please enter the path of the file you wish to upload: ");
        String filePath = input.nextLine();
        File temp;
        try {
            temp = new File(filePath);
            if (!temp.exists()) {
                throw new IOException("ERROR: The chosen file does not exist");
            }
            return new UIPacket("uploadFile", new String[] {filePath});
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to return to the main menu");
            input.nextLine();
            return new UIPacket("mainmenu");
        }
    }

    public UIPacket deleteMessage(short state) {
        if (state == -1) {
            System.out.println("ERROR: Server tells us we sent a bad message.  Please press enter to continue");
            input.nextLine();
            return new UIPacket("startup");
        }
        else if(state == 0) {
            printTitle("Delete message");
            System.out.println("Please enter the contents of the message you would like to delete");
            String messageContents = input.nextLine();
            return new UIPacket("deleteMessage", new String[]{messageContents});
        }
        else if (state == 1) {
            System.out.println("Delete message successful!\nPlease press enter to continue");
            input.nextLine();
            return new UIPacket("chatselected");
        }
        else if (state == 2) {
            System.out.println("Could not delete message.\nPlease press enter to continue");
            input.nextLine();
            return new UIPacket("chatselected");
        }
        System.out.println("Unknown state used in delete message");
        return new UIPacket("startup"); //unknown state
    }

    public UIPacket sendChatMessage(int state) {
        if (state == -1) {
            System.out.println("ERROR: Server tells us we sent a bad message.  Please press enter to continue");
            input.nextLine();
            return new UIPacket("startup");
        }
        else if(state == 0) {
            printTitle("Send chat message");
            System.out.println("Please enter the contents of the message you would like to send");
            String messageContents = input.nextLine();
            return new UIPacket("chatselected", new String[]{messageContents});
        }
        else if (state == 1) {
            System.out.println("Send message successful!\nPlease press enter to continue");
            input.nextLine();
            return new UIPacket("chatselected"); // TODO: reroute to chatview
        }
        else if (state == 2) {
            System.out.println("Could not send message.\nPlease press enter to continue");
            input.nextLine();
            return new UIPacket("chatselected"); // TODO: reroute to chatview
        }
        System.out.println("Unknown state used in send message");
        return new UIPacket("startup"); //unknown state
    }

    public UIPacket downloadFile(int state) {
        printTitle("Download File");
        System.out.println("Please enter the name of the file you would like to download");
        String fileName = input.nextLine();
        
        System.out.println("Please enter the full path of the directory you would like to download the file to (excluding the file you're about to download)");
        String downloadPathName = input.nextLine();
        
        return new UIPacket("downloadfile", new String[] {fileName, downloadPathName});
    }
    
    public void printTitle(String title) {
        System.out.println("\n____________\n\n" + title + "\n____________\n");
    }

    public UIPacket chatsMenu() {
        int selection = -1;
        printTitle("Chats Menu");
        System.out.println("Please choose one of the following options: ");
        System.out.println("0\t: Main Menu");
        System.out.println("1\t: Your Chats");
        System.out.println("2\t: Create New Chat");

        try {
            selection = Integer.parseInt(input.nextLine());
            if (selection < 0 || selection > 2)
                throw new IOException("ERROR: You did not type a number associated with an available option.");
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to try again");
            input.nextLine();
            return new UIPacket("mainmenu");
        }

        switch (selection) {
            case 0 :
                return new UIPacket("mainmenu");
            case 1:
                return new UIPacket("chatslist");
            case 2:
                return new UIPacket("createchat");
            default:
                System.out.println("This should've been impossible to reach...");
                return new UIPacket("mainmenu");
        }
    }

    public UIPacket chatsLists() {
        int selection = -2;
        System.out.println("Select a chat: ");

        try {
            selection = Integer.parseInt(input.nextLine());
        }
        catch (Exception e) {
            System.out.println("Invalid input");
            return new UIPacket("chatsmenu");
        }


        switch (selection) {
            case -1:
                return new UIPacket("chatslist", new String[] {"-1"});
            default:
                return new UIPacket("chatselected", new String[]{Integer.toString(selection)});
        }
    }

    public UIPacket chatSelected() {
        int selection = -1;
        printTitle("CID: " + Client.currentChatID + "\t" + Client.currentChatName);
        System.out.println("Please choose one of the following options: ");
        System.out.println("-1\t: Chats Menu");
        System.out.println("0\t: Add User");
        System.out.println("1\t: Remove User");
        System.out.println("2\t: Placeholder");
        System.out.println("3\t: Send Message");
        System.out.println("4\t: Delete Message");
        System.out.println("5\t: View Chat History");
        System.out.println("6\t: Upload file");
        System.out.println("7\t: Download file");

        try {
            selection = Integer.parseInt(input.nextLine());
            if (selection < -1 || selection > 7)
                throw new IOException("ERROR: You did not type a number associated with an available option.");
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to try again");
            input.nextLine();
            return new UIPacket("mainmenu");
        }

        switch (selection) {
            case -1 :
                return new UIPacket("chatsmenu");
            case 0:
                return new UIPacket("adduser");
            case 1:
                return new UIPacket("removeuser");
            case 2:
                return new UIPacket("");
            case 3:
                return new UIPacket("sendchatmessage");
            case 4:
                return new UIPacket("deletemessage");
            case 5:
                return new UIPacket("getchathistory");
            case 6:
            	return new UIPacket("uploadfile");
            case 7:
            	return new UIPacket("downloadfile");
            default:
                System.out.println("This should've been impossible to reach...");
                return new UIPacket("chatsmenu");
        }
    }

    public UIPacket createChat() {
        String chatName;
        System.out.println("Please enter the name of the chat to be created: ");
        chatName = input.nextLine();


        return new UIPacket("createChat", new String[] {chatName});
    }

    public UIPacket getChatHistory(String history) {
        printTitle("Chat History");
        if (history == null) {
            System.out.println("There is no chat history");
        } else {
            System.out.println(history);
        }
        return new UIPacket("chatselected");
    }

    public UIPacket addUser() {
        System.out.println("Enter the ID of the user to be added");
        int ID = Integer.parseInt(input.nextLine());
        return new UIPacket("adduser", new String[] {String.valueOf(ID)});
    }

    public UIPacket removeUser() {
        System.out.println("Enter the ID of the user to be removed");
        int ID = Integer.parseInt(input.nextLine());
        return new UIPacket("removeuser", new String[] {String.valueOf(ID)});
    }
}
