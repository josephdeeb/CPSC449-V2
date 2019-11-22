import java.util.*;

public class User {
    private int uID;
    // username cannot contain any commas
	private String username;
	// password cannot contain any commas
	private String password;
	private boolean isLoggedIn;
	private ArrayList<Integer> friends;
	private ArrayList<Integer> chats;
	
	private User(int uID, String username, String password) {
	    this.uID = uID;
		this.username = username;
		this.password = password;
		// List of uID's of friends
		this.friends = new ArrayList<Integer>();
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
	    if (chats.contains(uID)) {
	        return false;
	    }
	    else {
	        chats.add(uID);
	        return true;
	    }
	}
	
}
