import java.io.*;
import java.net.IDN;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    public static final int MAX_MESSAGE_SIZE = 4096;
    public static final String ENCODING = "UTF-8";
    private static ClientConnectionHandler clientConnectionHandler;
    private static UI ui;
    private static boolean running;
    private static ByteBuffer buf;
    public static String username = "";
    public static int currentChatID;
    public static String currentChatName;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("ERROR: Incorrect number of arguments.  Proper usage: java Client [server ip] [server port]");
        }

        try {
            clientConnectionHandler = ClientConnectionHandler.create(args[0], Integer.parseInt(args[1]));
            if (clientConnectionHandler == null)
                throw new IOException("Failed to establish connection on the given port.");
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        buf = ByteBuffer.allocate(MAX_MESSAGE_SIZE);

        ui = new UI();
        running = true;
        String next = "startup";
        while (running) {
            next = parseUI(next);
        }
    }

    private static String parseUI(String selection) {
        String nextUI = "";

        switch (selection) {
            case "startup":
                nextUI = parseStartup();
                break;
            
            case "login":
                nextUI = parseLogin();
                break;
                
            case "register":
                nextUI = parseRegister();
                break;
                
            case "exit":
                running = false;
                nextUI = null;
                break;
                
            case "mainmenu":
                nextUI = parseMainMenu();
                break;
                
            case "chatsmenu":
                nextUI = parseChatsMenu();
                break;

            case "createchat":
                nextUI = parseCreateChat();
                break;

            case "chatslist":
                nextUI = parseChatsList();
                break;

            case "chatselected":
                nextUI = parseChatSelected();
                break;

            case "adduser":
                nextUI = parseAddUser();
                break;

            case "removeuser":
                nextUI = parseRemoveUser();
                break;

            case "friendslist":
                nextUI = parseFriendsList();
                break;
			
			case "displayfriends":
				nextUI = parseDisplayFriends();
				break;
				
			case "sendfriendrequest":
				nextUI = parseSendFriendRequest();
				break;

				
			case "viewfriendrequests":
				nextUI = parseViewFriendRequests();
				break;
				
			case "acceptfriendrequest":
				nextUI = parseAcceptFriendRequest();
				break;
                

            case "accountsettings":
                nextUI = parseAccountSettings();
                break;

			case "changeusername":
				nextUI = parseChangeUsername();
				break;

			case "changepassword":
				nextUI = parseChangePassword();
				break;

			case "deleteaccout":
				nextUI = parseDeleteAccout();
				break;
				

            case "uploadfile":
                nextUI = parseUploadFile();
                break;

            case "downloadfile":
                nextUI = parseDownloadFile();
                break;

            case "sendchatmessage":
                nextUI = parseSendChatMessage();
                break;
            case "getchathistory":
                nextUI = parseGetChatHistory();

            case "deletemessage":
                nextUI =  parseDeleteMessage();
                break;

            case "save":
                nextUI = parseSave();
                break;

            default:
                nextUI = "exit";
                System.out.println("ERROR: UI Type \"" + selection + "\" unknown...");
        }

        return nextUI;
    }


	public static String parseViewFriendRequests(){
		buf.clear();
        buf.putShort((short)5);
		
		buf.flip();
        
        clientConnectionHandler.sendMessage(buf);
		buf.clear();
		
		ByteBuffer response = clientConnectionHandler.receiveMessage();
        int stringSize = response.getInt();
		
		byte[] stringBytes = new byte[stringSize];
		
		response.get(stringBytes);
		
		String friends = new String(stringBytes);

        //ui.displayFriends(friends);
		System.out.println(friends);
		return"friendslist";
	}



	public static String parseAcceptFriendRequest(){
		UIPacket temp = ui.acceptFriendRequest((short) 0);
		String msg = temp.args[0];
		 buf.clear();
		 buf.putShort((short)6);
		 try{
			 buf.putInt(msg.length());
			 buf.put(msg.getBytes("UTF-8"));
		 } catch(Exception e){
			 System.out.println("Errpr: could not get bytes from the msg in parseChangeUserName");
			 return "accountsettings";
		 }

		 buf.flip();

        clientConnectionHandler.sendMessage(buf);

        ByteBuffer response = clientConnectionHandler.receiveMessage();

        short type = response.getShort();
		if(type == -1){
			System.out.println("Error");
			return "friendslist";
		}
		if(type == 7){
			System.out.println("");
			return"friendslist";
		}
        if (type == 1) {
            
        }
        return ui.acceptFriendRequest(type).nextUI;
    }
		
		
		

    public static String parseDeleteAccout(){
		buf.clear();
		buf.putShort((short) 666);
		buf.flip();
		clientConnectionHandler.sendMessage(buf);
		return "exit";
		
	}
	
	public static String parseChangeUsername(){
		UIPacket temp = ui.changeUsername((short) 0);
		String msg = temp.args[0];
		 buf.clear();
		 buf.putShort((short)400);
		 try{
			 buf.putInt(msg.length());
			 buf.put(msg.getBytes("UTF-8"));
		 } catch(Exception e){
			 System.out.println("Errpr: could not get bytes from the msg in parseChangeUserName");
			 return "accountsettings";
		 }

		 buf.flip();

        clientConnectionHandler.sendMessage(buf);

        ByteBuffer response = clientConnectionHandler.receiveMessage();

        short type = response.getShort();
		if(type == 6){
			System.out.println("Error");
			return "accountsettings";
		}
		if(type == 7){
			System.out.println("You can't change your username to this because it's already taken :(");
			return"accountsettings";
		}
        if (type == 1) {
            username = temp.args[0];
        }
        return ui.changeUsername(type).nextUI;
    }
	
	public static String parseChangePassword(){
		UIPacket temp = ui.changePassword((short) 0);
		String msg = temp.args[0];
		
		buf.clear();
		 buf.putShort((short)401);
		 try{
			 buf.putInt(msg.length());
			 buf.put(msg.getBytes("UTF-8"));
		 } catch(Exception e){
			 System.out.println("Errpr: could not get bytes from the msg in parseChangePassword");
			 return "accountsettings";
		 }
		 
		 buf.flip();
        
        clientConnectionHandler.sendMessage(buf);
        
        ByteBuffer response = clientConnectionHandler.receiveMessage();
        short type = response.getShort();
		if(type == 6){
			System.out.println("Error");
			return "accountsettings";
		}
		return ui.changePassword(type).nextUI;
		}
		
		
	
		 
		
	

	private static String parseSendFriendRequest() {
		UIPacket temp = ui.sendFriendRequest();
		String msg = temp.args[0];
        buf.clear();
        buf.putShort((short)4);
        try {
        	buf.putInt(msg.length());
        	buf.put(msg.getBytes("UTF-8"));
        } catch (Exception e) {
        	System.out.println("Couldn't get bytes from the msg in parseSendFriendRequest()");
        	return "friendslist";
        }



		buf.flip();

        clientConnectionHandler.sendMessage(buf);
		buf.clear();

		ByteBuffer response = clientConnectionHandler.receiveMessage();
        short type = response.getShort();

		if(type == 1){
			System.out.println("Request has been sent!");
		}

		else{
			System.out.println("Request failed :( Make sure this is a registered user! ");
		}


		return "friendslist";

    }



	private static String parseDisplayFriends() {


        buf.clear();
        buf.putShort((short)3);
		
		buf.flip();
        
        clientConnectionHandler.sendMessage(buf);
		buf.clear();
		
		ByteBuffer response = clientConnectionHandler.receiveMessage();
        int stringSize = response.getInt();
		
		byte[] stringBytes = new byte[stringSize];
		
		response.get(stringBytes);
		
		String friends = new String(stringBytes);

        //ui.displayFriends(friends);
		System.out.println(friends);
		return"friendslist";
        
    }
	
	
	
	
	
	
	
	
	
	
	
	private static String parseAccountSettings(){
		return ui.accountSettings().nextUI;
	}
	
	private static String parseFriendsList(){
		return ui.friendsList().nextUI;
	}
	
	
	
	
	
	
	

    private static String parseStartup() {
        return ui.startup().nextUI;
    }
    
    private static String parseLogin() {
        UIPacket temp = ui.login((short)0);
        String msg = temp.args[0] + "," + temp.args[1];
        buf.clear();
        buf.putShort((short)2);
        try {
        	buf.putInt(msg.length());
        	buf.put(msg.getBytes("UTF-8"));
        } catch (Exception e) {
        	System.out.println("Couldn't get bytes from the msg in parseLogin()");
        	return "startup";
        }
        
        buf.flip();
        clientConnectionHandler.sendMessage(buf);
        
        ByteBuffer response = clientConnectionHandler.receiveMessage();
        short type = response.getShort();
        
        return ui.login(type).nextUI;
    }
    
    private static String parseRegister() {
        UIPacket temp = ui.register((short)0);
        // args[0] = username, args[1] = password
        String msg = temp.args[0] + "," + temp.args[1];
        buf.clear();
        // Put 1 as a short in to signal the message type
        buf.putShort((short)1);
        // Next, put the message length and then try to put username,password into buf
        try {
        	buf.putInt(msg.length());
            buf.put(msg.getBytes("UTF-8"));
        } catch (Exception e) {
            System.out.println("ERROR: Could not get bytes from the msg in parseRegister");
            return "register";
        }
        // Flip the buf before we send
        buf.flip();
        
        // Finally, send the messsage!
        clientConnectionHandler.sendMessage(buf);
        
        // Await our response
        ByteBuffer response = clientConnectionHandler.receiveMessage();
        
        short type = response.getShort();
        // 1 means successful registration
        if (type == 1) {
            username = temp.args[0];
        }
        return ui.register(type).nextUI;
    }
    
	private static String parseSendChatMessage() {
        UIPacket temp = ui.sendChatMessage((short)0);
        String msg = temp.args[0] + "," + temp.args[1];
        // args[0] = chatID, args[1] = messageContents
        buf.clear();
        buf.putShort((short)14);
        try {
            buf.putInt(msg.length());
            buf.put(String.valueOf(Client.currentChatID).getBytes("UTF-8"));
            buf.put(msg.getBytes("UTF-8"));
        } catch (Exception e) {
            System.out.println("ERROR: Could not get bytes from the msg in parseSendChatMessage");
            return "startup";
        }
        buf.flip();
        clientConnectionHandler.sendMessage(buf);

        ByteBuffer response = clientConnectionHandler.receiveMessage();
        short type = response.getShort();

        return ui.sendChatMessage(type).nextUI;
    }
    
    private static String parseGetChatHistory() {
        UIPacket temp = ui.getChatHistory();
        String msg = temp.args[0] + "," + Client.currentChatID;
        // args[0] = messageContents
        buf.clear();
        buf.putShort((short)15);
        try {
            buf.putInt(msg.length());
            buf.put(msg.getBytes("UTF-8"));
        } catch (Exception e) {
            System.out.println("ERROR: Could not get bytes from the msg in parseGetChatHistory");
            return "startup";
        }
        buf.flip();
        clientConnectionHandler.sendMessage(buf);

        ByteBuffer response = clientConnectionHandler.receiveMessage();
        short type = response.getShort();

        //return ui.printChatHistory(response.toString());
		return"";
    }
    
    private static String parseChatsMenu() {
        //UIPacket temp = ui.chatsMenu();
        return ui.chatsMenu().nextUI;
    }


    private static String parseChatsList() {
        ArrayList<Integer> userChats = new ArrayList<Integer>();
        ArrayList<String> userChatNames = new ArrayList<String>();
        buf.clear();
        buf.putShort((short)20);
        buf.flip();
        clientConnectionHandler.sendMessage(buf);

        ByteBuffer response = clientConnectionHandler.receiveMessage();
        int chatsize = response.getInt();
        ui.printTitle("Your Chats");
        if (chatsize == 0) {
            System.out.println("You have no chats");
            return "chatsmenu";
        }
        else {
            System.out.println("-1\t: Go back");
            for (int i = 0; i < chatsize; i++) {
                int CID = response.getInt();
                int size = response.getInt();
                byte[] name = new byte[size];
                response.get(name, 0, size);
                userChats.add(CID);
                userChatNames.add(new String(name));
                System.out.println(CID + "\t: " + new String(name));
            }
        }

        UIPacket temp = ui.chatsLists();
        int selection = Integer.parseInt(temp.args[0]);
        if (selection < 0) {
            return "chatsmenu";
        }
        if (userChats.contains(selection)) {
            Client.currentChatID = selection;
            Client.currentChatName = userChatNames.get(userChats.indexOf(selection));
            System.out.println(selection + "Selected");
            return "chatselected";
        }
        else {
            System.out.println("Invalid selection");
            return "chatsmenu";
        }
    }

    private static String parseChatSelected() {
        UIPacket temp = ui.chatSelected();

        return ui.chatSelected().nextUI;
    }

    private static String parseAddUser() {
        UIPacket temp = ui.addUser();
        int id = Integer.parseInt(temp.args[0]);
        buf.clear();
        buf.putShort((short)18);
        buf.putInt(currentChatID);
        buf.putInt(id);
        buf.flip();
        clientConnectionHandler.sendMessage(buf);

        ByteBuffer response = clientConnectionHandler.receiveMessage();
        short type = response.getShort();
        if (type == -1) {
            System.out.println("Something went wrong!");
        }
        if (type == 1) {
            System.out.println("User " + id + " added successfully!");
        }
        if (type == 2) {
            System.out.println("You can't do that action");
        }
        return "chatselected";
    }

    private static String parseRemoveUser() {
        UIPacket temp = ui.removeUser();
        int id = Integer.parseInt(temp.args[0]);
        buf.clear();
        buf.putShort((short)19);
        buf.putInt(currentChatID);
        buf.putInt(id);
        buf.flip();
        clientConnectionHandler.sendMessage(buf);

        ByteBuffer response = clientConnectionHandler.receiveMessage();
        short type = response.getShort();
        if (type == -1) {
            System.out.println("Something went wrong!");
        }
        if (type == 1) {
            System.out.println("User " + id + " removed successfully!");
        }
        if (type == 2) {
            System.out.println("You can't do that action");
        }
        return "chatselected";
    }

    private static String parseDeleteMessage() {
        UIPacket temp = ui.deleteMessage((short)0);
        String msg = temp.args[0] + "," + temp.args[1];
        // args[0] = chatID, args[1] = messageContents
        buf.clear();
        buf.putShort((short)1);
        try {
            buf.putInt(msg.length());
            buf.put(msg.getBytes("UTF-8"));
        } catch (Exception e) {
            System.out.println("ERROR: Could not get bytes from the msg in parseDeleteMessage");
            return "startup";
        }
        buf.flip();
        clientConnectionHandler.sendMessage(buf);

        ByteBuffer response = clientConnectionHandler.receiveMessage();
        short type = response.getShort();

        return ui.deleteMessage(type).nextUI;
    }

    private static String parseCreateChat() {
        UIPacket temp = ui.createChat();
        String chatName = temp.args[0];
        buf.clear();
        buf.putShort((short)17);
        try {
            buf.putInt(chatName.length());
            buf.put(chatName.getBytes("UTF-8"));
        } catch (Exception e) {
            System.out.println("Something went wrong creating chat.");
        }
        buf.flip();
        clientConnectionHandler.sendMessage(buf);

        ByteBuffer response = clientConnectionHandler.receiveMessage();
        short type = response.getShort();

        if (type == (short) 0) {
            System.out.println("Chat " + chatName + " has been created successfully!");
            System.out.println("Press Enter to continue");
            Scanner input = new Scanner(System.in);
            input.nextLine();
        }
        return "chatsmenu";
    }

    
    private static String parseUploadFile() {
        UIPacket temp = ui.uploadFile();
        // If it returned mainmenu, then go back to the mainmenu
        if (temp.nextUI.equals("mainmenu")) {
            return "mainmenu";
        }
        
        try {
            // First put msg type (300), then the length of the file as a long, then the length of the file name + the file name
            ByteBuffer message = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
            File file = new File(temp.args[0]);
            long bytes = file.length();
            message.putShort((short)300);
            message.putLong(bytes);
            
            String fileName = file.getName();
            byte[] b = fileName.getBytes();
            int fileNameSize = b.length;
            
            message.putInt(fileNameSize);
            message.put(b);
            
            message.flip();
            // Send our message
            clientConnectionHandler.sendMessage(message);
            // Await the response
            message = clientConnectionHandler.receiveMessage();
            short response = message.getShort();
            
            if (response == -2) {
                System.out.println("ERROR: The server thinks you are already transferring a file");
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }
            else if (response == -1) {
                System.out.println("ERROR: A file with that name already exists on the server");
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }
            else if (response == 1) {
                System.out.println("Starting file transfer...");
            }
            else if (response == 2) {
                System.out.println("ERROR: Server cannot accept your file.");
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }
            else {
                System.out.println("ERROR: Unknown error type");
                System.out.println("Please press enter to return to the main menu");
                ui.input.nextLine();
                return "mainmenu";
            }
            
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
        
        try {
            ByteBuffer buf = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
            // b is size - 6 because it the first 6 bytes are the message type and then amount of bytes in the message as an integer
            byte[] b = new byte[MAX_MESSAGE_SIZE-6];
            InputStream is = new FileInputStream(temp.args[0]);
            int readBytes = 0;
            
            while ((readBytes = is.read(b)) != -1) {
                // Put message type as 301 (file transfer in progress)
                buf.putShort((short)301);
                // Put the amount of bytes read as an int (4 bytes)
                buf.putInt(readBytes);
                // Then put the actual bytes from the file
                buf.put(b, 0, readBytes);
                // Flip the buffer to prepare it for read operations
                buf.flip();
                // Finally, send the buffer as a message to the server
                clientConnectionHandler.sendMessage(buf);
                buf.clear();
            }
            
            // Once we've finished sending the file, send the teardown message
            clientConnectionHandler.sendMessage((short)302);
            System.out.println("File upload successful!");
            System.out.println("Please press enter to return to the main menu");
            ui.input.nextLine();
            return "mainmenu";
            
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
    }
    
    private static String parseDownloadFile() {
        UIPacket temp = ui.downloadFile(0);
        String filename = temp.args[0];
        String downloadPathName = temp.args[1];

        // FIRST OFF, make sure the file doesn't already exist and that we have permission to write to it
        File file = new File(downloadPathName + File.separator + filename);
        File folder = new File(downloadPathName);
        try {
            if (folder.exists()) {
                if (folder.canWrite()) {}
                else
                    throw new IOException("ERROR: You do not have permission to write to the given folder");
            }
            else
                throw new IOException("ERROR: The given folder you're downloading to does not exist");

            if (file.exists()) {
                throw new IOException("ERROR: There is already a file in the specified folder with that name");
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }

        // Sending our initial message
        ByteBuffer message = ByteBuffer.allocate(MAX_MESSAGE_SIZE);

        // Put the type at the beginning
        message.putShort((short)303);

        // Next turn filename into a byte array
        byte[] filenameBytes = filename.getBytes();

        // Put filenameBytes length then the bytes into message
        message.putInt(filenameBytes.length);
        message.put(filenameBytes);

        // Don't do the same for downloadPathName, server doesn't need to know it

        // flip our message and send it!
        message.flip();
        clientConnectionHandler.sendMessage(message);

        // Wait for the response
        message = clientConnectionHandler.receiveMessage();

        short response = message.getShort();
        // Already transferring file
        if (response == (short)-1) {
            System.out.println("ERROR: You are already transferring a file, according to the server");
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
        else if (response == (short)2) {
            System.out.println("ERROR: Requested file " + filename + " does not exist on the server.");
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
        // Success!  Start getting ready to read the file bytes
        else if (response == (short)1) {
            System.out.println("Starting file transfer...");
        }
        else {
            System.out.println("ERROR: Unknown response from server");
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
        // If we've gotten here, we're about to start accepting the file transfer

        // First create the file and get the output stream
        OutputStream os = null;
        try {
            file.createNewFile();
            os = new FileOutputStream(file);
        } catch (Exception e) {
            System.out.println("ERROR: Failed to create the local file.  Cancelling file transfer...");
            // send cancel file transfer message to reset server session data
            clientConnectionHandler.sendMessage((short)306);
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }

        // Ask for bytes and receive them until there are none left!
        boolean transferring = true;
        while (transferring) {
            clientConnectionHandler.sendMessage((short)304);
            message = clientConnectionHandler.receiveMessage();

            response = message.getShort();

            if (response == -1) {
                System.out.println("ERROR: The server says you are not transferring a file right now");
                clientConnectionHandler.sendMessage((short)306);
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }
            else if (response == -2) {
                System.out.println("ERROR: The server failed to read the file on the server-side");
                clientConnectionHandler.sendMessage((short)306);
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }
            else if (response == 0) {
                // success!
            }
            else {
                System.out.println("ERROR: Unknown message from server.  Aborting file transfer.");
                clientConnectionHandler.sendMessage((short)306);
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }

            // Next get an int

            int bytesToRead = message.getInt();

            // If its -1, that means file transfer is complete
            if (bytesToRead == -1) {
                System.out.println("Finished transferring file!");
                transferring = false;
                break;
            }
            // Otherwise, we're about to write that many bytes to our file
            else {
                byte[] fileBytes = new byte[bytesToRead];
                message.get(fileBytes);
                try {
                    os.write(fileBytes);
                } catch (Exception e) {
                    System.out.println("ERROR: Failed to write bytes from server to the file.  Aborting file transfer.");
                    clientConnectionHandler.sendMessage((short)306);
                    System.out.println("Please press enter to continue");
                    ui.input.nextLine();
                    return "mainmenu";
                }
            }
        }
        // After while(transferring)
        System.out.println("File successfully transferred!");
        clientConnectionHandler.sendMessage((short)306);
        try {
            os.close();
        } catch (Exception e) {
            System.out.println("ERROR: Failed to close file stream");
        }

        return "mainmenu";
    }
    
    private static String parseMainMenu() {
        return ui.mainMenu().nextUI;
    }
	
	private static String parseFriendslist(){
		return ui.friendsList().nextUI;
	}
    
    private static String parseSave() {
        // No UI needed
        // Just put message type 310 and send
        ByteBuffer message = ByteBuffer.allocate(2);
        message.putShort((short)310);
        message.flip();
        
        clientConnectionHandler.sendMessage(message);
        
        System.out.println("\n\nSave message sent successfully\n\n");
        
        return "startup";
    }
	
    
}
