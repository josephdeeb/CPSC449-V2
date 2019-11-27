import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
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
        
        // Next create the socketToUIDMap and chats
        chats = new HashMap<Integer, ChatDB>();
        socketToUIDMap = new HashMap<SocketChannel, Integer>();
        
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
    
    public static void handleLogin(ByteBuffer message, SocketChannel sock) {
    	String username;
    	String password;
    	int len = message.getInt();
    	String userInfo = connectionHandler.retrieveString(message, len);
    	if (userInfo == null) {
    		connectionHandler.sendError(sock);
    		return;
    	}
    	
    	if (connectionHandler.countOccurrences(userInfo, ',') != 1) {
    		connectionHandler.sendMessage(sock, (short)2);
    		return;
    	}
    	
    	if (userInfo.split(",").length != 2) {
    		connectionHandler.sendMessage(sock, (short)2);
    		return;
    	}
    	
    	username = userInfo.split(",")[0];
    	password = userInfo.split(",")[1];
    	int uID = users.validateUser(username, password);
    	if (uID == -1) {
    		connectionHandler.sendMessage(sock,  (short)2);
    		return;
    	}
    	
    	socketToUIDMap.put(sock, uID);
    	
    	connectionHandler.sendMessage(sock, (short)1);
    	return;
    }
    
    public static void handleUpload(ByteBuffer message, SocketChannel sock) {
        // first check if the user is already transferring a file
        User userTemp = users.getUser(socketToUIDMap.get(sock));
        if (userTemp.isTransferringFile()) {
            connectionHandler.sendMessage(sock, (short)-1);
            return;
        }
        // Create the files directory if it doesn't already exist
        String path = new File("").getAbsolutePath() + File.separator + "files";
        new File(path).mkdir();
        
        // Get the length of the file, then the length of the filename, then the filename
        long len = message.getLong();
        int fileNameLength = message.getInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        
        message.get(fileNameBytes);
        
        String fileName = new String(fileNameBytes);
        
        // Next, create the actual file if it doesn't exist already
        try {
            // If it does exist already, send a message of -1 to the client indicating it already exists
            if (new File(path + File.separator + fileName).createNewFile() == false) {
                connectionHandler.sendMessage(sock, (short)-1);
            }
        } catch (Exception e) {
            System.out.println(e);
            connectionHandler.sendMessage(sock, (short)-1);
            return;
        }
        
        
        // Finally, set the file stream
        userTemp.setFileStream(path + File.separator + fileName);
        
        // Set session data for file transfer
        userTemp.fileSize = len;
        userTemp.fileBytesTransferred = 0;
        userTemp.setTransferringFile(true);
        
        // Next, send a response of 1 to indicate that the server is ready to accept bytes from the client
        connectionHandler.sendMessage(sock, (short)1);
        return;
    }
    
    public static void handleUploadInProgress(ByteBuffer message, SocketChannel sock) {
        User userTemp = users.getUser(socketToUIDMap.get(sock));
        // If the user is not currently transferring a file, send a message of -1
        if (!userTemp.isTransferringFile()) {
            connectionHandler.sendMessage(sock, (short)-1);
            return;
        }
        
        // Otherwise, user is currently transferring a file!
        
        // Figure out how many bytes to read from this message
        int bytesToRead = message.getInt();
        
        byte[] bytes = new byte[bytesToRead];
        message.get(bytes);
        
        // Now write the bytes we read
        try {
            userTemp.getFileStream().write(bytes);
        } catch (Exception e) {
            // Could be a source of bugs here
            System.out.println(e);
            return;
        }
        
        return;
    }
    
    public static void handleUploadFinish(ByteBuffer message, SocketChannel sock) {
        // We're done with the upload!  Reset session data for the user and close their file stream
        User userTemp = users.getUser(socketToUIDMap.get(sock));
        
        userTemp.fileSize = 0;
        userTemp.fileBytesTransferred = 0;
        userTemp.setTransferringFile(false);
        userTemp.closeFileStream();
        
        return;
    }

	public static void handleCreateChat(ByteBuffer message, SocketChannel sock) {
        int len = message.getInt();
        String chatName = connectionHandler.retrieveString(message, len);

        String chatPath = "" + File.separator + ChatDB.folderName;
        int CID = new File (chatPath).list().length;

        ChatDB.create("", CID, chatName);
        ChatDB cdb = ChatDB.loadChat(chatPath, CID);

        cdb.addUser(socketToUIDMap.get(sock));
        cdb.saveChat();
        chats.put(CID, cdb);

        //TODO add return message
    }
    //TODO add return messages
	public static void handleAddChatUser(ByteBuffer message, SocketChannel sock) {
        int CID = message.getInt();
        int UID = message.getInt();
        int SUID = socketToUIDMap.get(sock);
        ChatDB cdb = chats.get(CID);
        ArrayList<Integer> chatUsers = cdb.getUsers();
        if (chatUsers.contains(UID)) {
            // Send error message, user already a member
            return;
        }
        if (chatUsers.contains(SUID)) {
            cdb.addUser(UID);
            // Send success message
        }
        else {
            // Send you are not a member message
        }

	}

	public static void handleGetChatList(ByteBuffer message, SocketChannel sock) {
	}
}
