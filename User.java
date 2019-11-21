import java.util.*;

public class User {
	private String username;
	private String password;
	private boolean isLoggedIn;
	private ArrayList<User> friends;
	private ArrayList<Chat> chats;
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
}
