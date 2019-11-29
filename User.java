import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class User {
    /*
     * IMPORTANT NOTICE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * DO NOT CHANGE ANY OF THE TYPES OR ADD ANY NEW INSTANCE VARIABLES TO THIS CLASS WITHOUT FIRST CONSULTING JOSEPH
     * UserDB RELIES ON uID, username, password, friends, AND chats HAVING THESE EXACT TYPES AND NAMES
     */
    private int uID;
    // username cannot contain any commas
	private String username;
	// password cannot contain any commas
	private String password;
	private ArrayList<Integer> friends;
	private ArrayList<Integer> friendRequests;
	private ArrayList<Integer> chats;
	
	// Session data (not saved)
	private boolean transferringFile = false;
	private boolean isLoggedIn;
	private OutputStream fileStream;
	private InputStream fileInputStream;
	public long fileSize = 0;
	public long fileBytesTransferred = 0;
	
	private User(int uID, String username, String password) {
	    this.uID = uID;
		this.username = username;
		this.password = password;
		// List of uID's of friends
		this.friends = new ArrayList<Integer>();
		// List of uID's of friend requests
		this.friendRequests = new ArrayList<Integer>();
		// List of cID's of chats
		this.chats = new ArrayList<Integer>();
	}
	
	public static User create(int uID, String username, String password) {
	    if (username.contains(",") || password.contains(",")) {
	        System.out.println("ERROR: username or password contains a comma ',' character");
	        return null;
	    }
	    
	    return new User(uID, username, password);
	}
	
	public int getuID() {
	    return uID;
	}
	
	public String getusername() {
	    return username;
	}
	
	public String getpassword() {
	    return password;
	}
	
	public ArrayList<Integer> getFriends() {
	    return friends;
	}
	
	public ArrayList<Integer> getChats() {
	    return chats;
	}
	
	public boolean addFriend(int uID) {
	    if (friends.contains(uID)) {
	        return false;
	    }
	    else {
	        friends.add(uID);
	        return true;
	    }
	}
	
	public boolean addChat(int cID) {
	    if (chats.contains(cID)) {
	        return false;
	    }
	    else {
	        chats.add(cID);
	        return true;
	    }
	}

	public boolean removeChat(int cID) {
		if (chats.contains(cID)) {
			chats.remove(cID);
			return true;
		}
		else {
			return false;
		}
	}
	
	public void addFriendRequest(int friendReq) {
	    if (!friendRequests.contains(friendReq)) {
	        friendRequests.add(friendReq);
	    }
	}
	
	// this could be a source of bugs
	public void removeFriendRequest(int friendReq) {
	    if (friendRequests.contains(friendReq)) {
	        friendRequests.remove((Integer)friendReq);
	    }
	}
	
	public ArrayList<Integer> getFriendRequests() {
	    return friendRequests;
	}
	
	public void changePassword(String newPassword){
		this.password = newPassword;
	}
	
	public int validate(String name, String pass) {
	    if (name.equals(this.username) && pass.equals(this.password))
	        return this.uID;
	    
	    return -1;
	}
	
	public boolean isTransferringFile() {
	    return this.transferringFile;
	}
	
	public boolean setFileStream(String filePath) {
	    File temp = new File(filePath);
	    try {
	        fileStream = new FileOutputStream(temp);
	        return true;
	    } catch (Exception e) {
	        System.out.println(e);
	        return false;
	    }
	}
	
	public void closeFileStream() {
	    try {
	        fileStream.close();
	    } catch (Exception e) {
	        System.out.println(e);
	        fileStream = null;
	    }
	}
	
	public OutputStream getFileStream() {
	    return this.fileStream;
	}
	
	public boolean setFileInputStream(String filePath) {
	    File temp = new File(filePath);
	    try {
	        fileInputStream = new FileInputStream(temp);
	        return true;
	    } catch (Exception e) {
	        System.out.println(e);
	        return false;
	    }
	}
	
	public void closeFileInputStream() {
	    try {
	        fileStream.close();
	    } catch (Exception e) {
	        System.out.println(e);
	    }
	}
	
	public InputStream getFileInputStream() {
	    return this.fileInputStream;
	}
	
	public void setTransferringFile(boolean temp) {
	    this.transferringFile = temp;
	}
}
