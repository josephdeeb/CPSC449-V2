import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

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
        
        // Create the files directory if it doesn't already exist
        String path = new File("").getAbsolutePath() + File.separator + "files";
        new File(path).mkdir();
        
        // Now we run the connection handler
        connectionHandler.run();
        Server.saveUsers();
        System.out.println("Exiting server...");
    }
    
    // Tries to load the chat with the given cID and adds it to chats
    // Returns true if the load was successful, false if the load was unsuccessful
    public static boolean loadChat(int cID) {
        ChatDB newChat = ChatDB.loadChat(cID);
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

    public static boolean deleteMessage(Message message, int cID) {
        return chats.get(cID).deleteMessage(message);
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
	
	public static boolean handleChangeUsername(ByteBuffer message, SocketChannel sock){
		Integer uID = socketToUIDMap.get(sock);
		
		int len = message.getInt();
        if (len > 129) {
            connectionHandler.sendMessage(sock, (short) 6);
        }

        String userInfo = connectionHandler.retrieveString(message, len);

        if (userInfo == null) {
            connectionHandler.sendError(sock);
            return false;
        }
		if(Server.usernameExists(userInfo)){
			connectionHandler.sendMessage(sock, (short) 7);
			return false;
		}
		else{
			Integer uIDofTheUser= users.nameToUIDMap.remove(uID);
			users.nameToUIDMap.put(userInfo, uIDofTheUser);
			return true;
		}
		
	}
	
	
	public static boolean handleSendFriendRequest(ByteBuffer message, SocketChannel sock){
		Integer uID = socketToUIDMap.get(sock);
		String friendlist = "";
		
		ArrayList<Integer> friends = users.getUser(uID).getFriends();
		
		//WORK ON LATER
		ByteBuffer buf = ByteBuffer.allocate(ConnectionHandler.MAX_MESSAGE_SIZE);
		byte[] bytes = new byte[friendlist.length()];
		bytes = friendlist.getBytes();
		buf.putInt(bytes.length);
		buf.put(bytes);
		buf.flip();
		connectionHandler.sendMessage(sock, buf);
		

        
		
		return true;	
	}
	
	
	
	public static boolean handleDisplayFriends(ByteBuffer message, SocketChannel sock){
		Integer uID = socketToUIDMap.get(sock);
		String friendlist = "";
		
		ArrayList<Integer> friends = users.getUser(uID).getFriends();
		
		for(int i = 0; i<friends.size(); i++){
			friendlist = friendlist + " " + (users.getUser(friends.get(i)).getusername());
		}
		 System.out.println(friendlist);
		ByteBuffer buf = ByteBuffer.allocate(ConnectionHandler.MAX_MESSAGE_SIZE);
		byte[] bytes = new byte[friendlist.length()];
		bytes = friendlist.getBytes();
		buf.putInt(bytes.length);
		buf.put(bytes);
		buf.flip();
		connectionHandler.sendMessage(sock, buf);
		

        
		
		return true;	
	}

    public static boolean handleSendChatMesssage(ByteBuffer message, SocketChannel sock) {
        int len = message.getInt();
        if (len > 129) {
            connectionHandler.sendMessage(sock, (short) 2);
        }

        String userInfo = connectionHandler.retrieveString(message, len);

        if (userInfo == null) {
            connectionHandler.sendError(sock);
            return false;
        }

        if (userInfo.split(",").length != 2) {
            connectionHandler.sendMessage(sock, (short) 1);
            return false;
        }

        int cID = Integer.parseInt(userInfo.split(",")[0]);
        Message msg = new Message(userInfo.split(",")[1], socketToUIDMap.get(sock));
        LinkedList<SocketChannel> socksToSendMessageTo = chats.get(cID).sendChatMessage(msg);
        for (SocketChannel socket : socksToSendMessageTo) {
            connectionHandler.sendMessage(socket, message); //TODO send chat message to users currently in chat (What function to call on client side?)
        }
        connectionHandler.sendMessage(sock, (short)1);  //send confirmation to sender
        return true;
    }

    public static boolean handleDeleteMessage(ByteBuffer message, SocketChannel sock) {
        int len = message.getInt();
        if (len > 129) {
            connectionHandler.sendMessage(sock, (short) 2);
        }

        String userInfo = connectionHandler.retrieveString(message, len);

        if (userInfo == null) {
            connectionHandler.sendError(sock);
            return false;
        }

        if (userInfo.split(",").length != 2) {
            connectionHandler.sendMessage(sock, (short) 1);
            return false;
        }

        Message msg = new Message(userInfo.split(",")[0], socketToUIDMap.get(sock));
        int cID = Integer.parseInt(userInfo.split(",")[1]);

        connectionHandler.sendMessage(sock, (short)1);

        return chats.get(cID).deleteMessage(msg);
    }


    public static boolean handleGetChatHistory(ByteBuffer message, SocketChannel sock) {
        int len = message.getInt();
        if (len > 129) {
            connectionHandler.sendMessage(sock, (short) 2);
        }

        String userInfo = connectionHandler.retrieveString(message, len);

        if (userInfo == null) {
            connectionHandler.sendError(sock);
            return false;
        }

        if (userInfo.split(",").length != 2) {
            connectionHandler.sendMessage(sock, (short) 1);
            return false;
        }

        Message msg = new Message(userInfo.split(",")[0], socketToUIDMap.get(sock));
        int cID = Integer.parseInt(userInfo.split(",")[1]);

        //connectionHandler.sendMessage(sock, ((short)chats.get(cID).getChatHistory()).getBytes());

        return true;

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
            connectionHandler.sendMessage(sock, (short)-2);
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
        System.out.println("resetting user session data for upload...");
        
        userTemp.fileSize = 0;
        userTemp.fileBytesTransferred = 0;
        userTemp.setTransferringFile(false);
        userTemp.closeFileStream();
        
        
        
        return;
    }
    
    public static void handleSave(ByteBuffer message, SocketChannel sock) {
        // Message is irrelevant, just need to save all user and chat data
        users.saveAllUsers();
        for (ChatDB chat : chats.values()) {
            chat.saveChat();
        }
        return;
    }
    
    public static void handleDownload(ByteBuffer message, SocketChannel sock) {
        // first check if the user is already transferring a file
        User userTemp = users.getUser(socketToUIDMap.get(sock));
        if (userTemp.isTransferringFile()) {
            connectionHandler.sendMessage(sock, (short)-1);
            return;
        }
        
        // Next, get the length of the filename + the filename
        int fileNameLength = message.getInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        message.get(fileNameBytes);
        
        String fileName = new String(fileNameBytes);
        
        // Check if the filename actually exists in files
        String filesPath = new File("").getAbsolutePath() + File.separator + "files";
        
        File targetFile = new File(filesPath + File.separator + fileName);
        
        try {
            if (targetFile.exists()) {
                // Get ready to start sending the file over!
                connectionHandler.sendMessage(sock, (short)1);
            }
            else
                throw new IOException("Requested file " + fileName + " does not exist");
        } catch (Exception e) {
            System.out.println(e);
            connectionHandler.sendMessage(sock, (short)2);
        }
        
        // Prep the user's session data
        userTemp.setFileInputStream(filesPath + File.separator + fileName);
        userTemp.fileSize = targetFile.length();
        userTemp.fileBytesTransferred = 0;
        userTemp.setTransferringFile(true);
    }
    
    public static void handleDownloadSendBytes(ByteBuffer message, SocketChannel sock) {
        // First make sure the user is transferring a file
        User userTemp = users.getUser(socketToUIDMap.get(sock));
        // If they're not transferring a file, return a response of -1
        if (!userTemp.isTransferringFile()) {
            System.out.println("ERROR: Client requested download bytes but client is not currently transferring a file");
            connectionHandler.sendMessage(sock, (short)-1);
            return;
        }
        
        // Next, grab the input stream for the file
        InputStream is = userTemp.getFileInputStream();
        
        // Get how many bytes we're gonna fit into the message
        // (file size - bytes we've transferred so far) mod MAX_MESSAGE_SIZE then subtract 4 so we have room to put a short + an int at the beginning
        long bytesToRead = (((userTemp.fileSize - userTemp.fileBytesTransferred) % ConnectionHandler.MAX_MESSAGE_SIZE) - 6);
        byte[] buf = new byte[ConnectionHandler.MAX_MESSAGE_SIZE];
        int bytesRead = 0;
        try {
            bytesRead = is.read(buf, 6, buf.length - 6);
            
        } catch (Exception e) {
            System.out.println("ERROR: Failed to read file.  Abort file transfer.");
            connectionHandler.sendMessage(sock, (short)-2);
            return;
        }
        
        // Now that buf contains our bytes, make the first 6 bytes of buf a short and then an integer that specifies how many bytes we're sending
        ByteBuffer msgBuf = ByteBuffer.wrap(buf);
        // msgBuf position should be zero, meaning we can write a short and then an int to it.  Instead of flipping, however, we will manually set position to 0
        msgBuf.putShort((short)0);
        msgBuf.putInt(bytesRead);
        msgBuf.position(0);
        
        // Now the limit is the end of buf, position is 0, and the data is correct
        // Time to send!
        connectionHandler.sendMessage(sock, msgBuf);
        
        // Update user session data
        if (bytesRead != -1) {
            userTemp.fileBytesTransferred += bytesRead;
        }
        
        return;
    }
    
    public static void handleDownloadCancel(ByteBuffer message, SocketChannel sock) {
        User userTemp = users.getUser(socketToUIDMap.get(sock));
        // reset all session data
        userTemp.setTransferringFile(false);
        userTemp.fileBytesTransferred = 0;
        userTemp.fileSize = 0;
        userTemp.closeFileInputStream();
        
        return;
    }

	public static void handleCreateChat(ByteBuffer message, SocketChannel sock) {
        int len = message.getInt();
        String chatName = connectionHandler.retrieveString(message, len);
        int CID = -1;

        try {
            File folder = new File(ChatDB.folderName);
            if (!folder.exists()) {
                if (!folder.mkdir())
                    throw new IOException("ERROR: Could not create folder for ChatDB");
            }
            CID = folder.list().length;
            if (CID > 0) {
                CID = CID/2;
            }
        } catch (Exception e) {
            System.out.println(e);
        }


        ChatDB cdb = ChatDB.create(CID, chatName);

        int UID = socketToUIDMap.get(sock);
        cdb.addUser(UID);
        cdb.saveChat();
        chats.put(CID, cdb);

        User sockUser = users.getUser(UID);
        sockUser.addChat(CID);
        users.saveAllUsers();

        connectionHandler.sendMessage(sock,  (short)0);

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
            User addUser = users.getUser(UID);
            addUser.addChat(CID);
            // Send success message
        }
        else {
            // Send you are not a member message
        }

	}

	public static void handleGetChatList(ByteBuffer message, SocketChannel sock) {
        User sockUser = users.getUser(socketToUIDMap.get(sock));
        ArrayList<Integer> chatList = new ArrayList<Integer>(sockUser.getChats());
        int chatSize = chatList.size();
        ByteBuffer out = ByteBuffer.allocate(ConnectionHandler.MAX_MESSAGE_SIZE);
        out.putInt(chatSize);   // put number of chats into out buffer
        int CID;
        ChatDB cdb;
        String chatName;

        if (chatSize > 0) {
            for (int i = 0; i < chatSize; i++) {
                CID = chatList.get(i);

                // Check if the chat is loaded and mapped to memory
                if (loadChat(CID)) {

                }
                else {
                    //TODO Error
                }

                chatName = chats.get(CID).getName();
                try {
                    out.putInt(CID);
                    out.putInt(chatName.length());
                    out.put(chatName.getBytes("UTF-8"));
                } catch (Exception e) {
                    System.out.println("Couldn't put chat name into buffer");
                    e.printStackTrace();
                }
            }
        }

        out.flip();
        connectionHandler.sendMessage(sock, out);
	}


    public static HashMap<SocketChannel, Integer> getSocketToUIDMap() {
        return socketToUIDMap;
    }

}
