import java.util.*;

public class User {
    private int uID;
    // username cannot contain any commas
	private String username;
	// password cannot contain any commas
	private String password;
	private boolean isLoggedIn;
	private ArrayList<User> friends;
	private ArrayList<Chat> chats;
	
	private User(int uID, String username, String password) {
	    this.uID = uID;
		this.username = username;
		this.password = password;
		this.friends = new ArrayList<User>();
		this.chats = new ArrayList<Chat>();
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
	
	public ArrayList<User> getFriends() {
	    return friends;
	}
	
	public ArrayList<Chat> getChats() {
	    return chats;
	}
	
	public boolean addFriend(User friend) {
	    if (friends.contains(friend)) {
	        return false;
	    }
	    else {
	        friends.add(friend);
	        return true;
	    }
	}
	
}
