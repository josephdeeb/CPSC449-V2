import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class Server {
    public static final String USER_DB_FILE_NAME = "users.csv";
    private static UserDB users;
    private static HashMap<Integer, ChatDB> chats;
    private static HashMap<SocketChannel, Integer> socketToUIDMap;
    private static ConnectionHandler connectionHandler;
    private static int port;
    
    /*
     * Usage: java Server [Listening Port]
     */
    public static void main(String[] args) {
        // Get the port from args
        port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("ERROR: First argument is supposed to be a port number.  Usage: java Server [Listening Port]");
            return;
        }
        
        // Next, load UserDB
        users = UserDB.create(new File("").getAbsolutePath(), Server.USER_DB_FILE_NAME);
        if (users == null) {
            System.out.println("ERROR: Could not open or create " + Server.USER_DB_FILE_NAME);
        }
        
        // If we fail to load the user database, exit
        if (!users.loadAllUsers()) {
            return;
        }
        
        // Next, create the connection handler
        try {
            connectionHandler = ConnectionHandler.create(port);
            if (connectionHandler == null) {
                throw new IOException("ERROR: Failed to establish connection on the given port.");
            }
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
        
        // Now we run the connection handler
        connectionHandler.run();
        Server.saveUsers();
        System.out.println("Exiting server...");
    }
    
    // Tries to load the chat with the given cID and adds it to chats
    // Returns true if the load was successful, false if the load was unsuccessful
    public static boolean loadChat(int cID) {
        ChatDB newChat = ChatDB.loadChat(new File("").getAbsolutePath(), cID);
        if (newChat == null)
            return false;
        
        chats.put(cID, newChat);
        return true;
    }
    
    // un-loads a chat, useful if we start having memory issues
    public static boolean unloadChat(int cID) {
        if (chats.containsKey(cID)) {
            chats.remove(cID);
            return true;
        }
        
        return false;
    }
    
    // Removes the given socket from the socketToUserMap
    // If the given socket does not exist, returns false.  Otherwise, returns true.
    public static boolean disconnectUser(SocketChannel sock) {
        if (socketToUIDMap.containsKey(sock)) {
            socketToUIDMap.remove(sock);
            return true;
        }
        
        return false;
    }
    
    // Adds the given socket + uID to the socketToUserMap
    // Returns false if the given uID already exists in the socketToUserMap, meaning that user is already online
    public static boolean connectUser(SocketChannel sock, int uID) {
        if (socketToUIDMap.containsValue(uID))
            return false;
        
        socketToUIDMap.put(sock, uID);
        return true;
    }
    
    /*
     * This method handles user login.
     * Return values:
     *      1: login info invalid
     *      2: login info is valid but the account is already logged in elsewhere
     *      3: login info is valid and given socket + uID are now linked
     */
    public static int userLogin(String username, String password, SocketChannel sock) {
        int uID = users.validateUser(username, password);
        // If user login info is invalid, return false
        if (uID == -1)
            return 1;
        // Login info is valid, now try to connect their socket and uID
        else if (connectUser(sock, uID))
            return 3;
        // Lo
        else
            return 2;
    }
    
    public static boolean usernameExists(String username) {
    	return users.usernameExists(username);
    }
    
    public static void registerUser(String username, String password) {
    	users.registerUser(username, password);
    }
    
    public static void saveUsers() {
    	users.saveAllUsers();
    }
    
    public static void handleRegister(ByteBuffer message, SocketChannel sock) {
	    // Take the length of the next string as an int
		int len = message.getInt();
		if (len > 129) {
			connectionHandler.sendMessage(sock, (short)3);
		}
		String userInfo = connectionHandler.retrieveString(message, len);
		// If the data in the packet sent isn't properly formatted, then we send an error message and break
		if (userInfo == null) {
			connectionHandler.sendError(sock);
			return;
		}
		// Otherwise, userInfo now contains the necessary data
		// Check how many commas the line contains, should only contain one
		if (connectionHandler.countOccurrences(userInfo, ',') != 1) {
			connectionHandler.sendMessage(sock, (short)3);
			return;
		}
		if (userInfo.split(",").length != 2) {
			connectionHandler.sendMessage(sock, (short)3);
			return;
		}
		String username = userInfo.split(",")[0];
		String password = userInfo.split(",")[1];
		if (Server.usernameExists(username)) {
			connectionHandler.sendMessage(sock, (short)2);
			return;
		}
		// Finally all the checks are done
		else {
			Server.registerUser(username, password);
			connectionHandler.sendMessage(sock, (short)1);
			return;
		}
    }
}
