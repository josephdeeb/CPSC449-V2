import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserDB extends DB {
	// List of users
	public HashMap<Integer, User> users;
	public HashMap<String, Integer> nameToUIDMap;
	int highestuID;
	
	/*
	 * path is a path to the folder containing the db
	 * dbName is the name of the db file
	 */
	private UserDB(String path, String dbName) {
	    super(path, dbName);
	    users = new HashMap<Integer, User>();
	    nameToUIDMap = new HashMap<String, Integer>();
	    highestuID = 0;
	}
	
	// Returns null if the userdb is not readable or not writable
	// If the userDB doesn't exist, it creates one, and if it fails to create one, it returns null
	// path is a path to the folder containing the db, dbName is the name of the db file
	public static UserDB create(String path, String dbName) {
	    UserDB temp = new UserDB(path, dbName);
	    if (temp.checkCreate()) {
	        return temp;
	    }
	    return null;
	}
	
	/*
	 * Loads users from this UserDB's database file and puts it into users
	 */
	public boolean loadAllUsers() {
	    BufferedReader reader = null;
		File db = new File(this.getdbPath());
		HashMap<Integer, User> temp = new HashMap<Integer, User>();
		try {
			// First make sure the database file exists
			if (!db.exists())
				throw new IOException("ERROR: getAllUsers() failed because the database stored in UserDB.path doesn't exist");
			
			// Then establish the reader
			reader = new BufferedReader(new FileReader(db));
			
			// Next, create the ArrayList
			String line;
			String[] splitted;
			User tempUser = null;
			int counter = 0;
			while ((line = reader.readLine()) != null) {
			    // User info line
			    if (counter == 0) {
			        // First check if we have encountered an empty user line, which means we're out of users to parse
			        if (line.trim().equals(""))
			            break;
			        
			        // Split the line by commas
			        splitted = line.split(",");
			        // Make sure there are exactly 3 values leftover
			        if (splitted.length < 3)
			            throw new IOException("ERROR: One of the user info lines does not contain three comma separated values.");
			        
			        // Then create a new User object with those three values as the arguments
			        tempUser = User.create(Integer.parseInt(splitted[0]), splitted[1], splitted[2]);
			        // Check to make sure it succeeded
			        if (tempUser == null)
			            throw new IOException("ERROR: One of the user info lines contains invalid data for username or password");
			        
			        // Add the users username and uID to the nameToUIDMap
			        nameToUIDMap.put(tempUser.getusername(), tempUser.getuID());
			        
			        // Set highestuID
			        if (tempUser.getuID() > highestuID) {
			        	highestuID = tempUser.getuID();
			        }
			        
			        // Increment counter to get the friends line next
			        counter++;
			    }
			    
			    // Friends line
			    else if (counter == 1) {
			        // If they have no friends, increment counter and continue
			        if (line.trim().equals("")) {
			            counter++;
			            continue;
			        }
			        
			        // Otherwise, gather the uID's of their friends and add them to tempUser.friends
			        splitted = line.split(",");
			        for (String str : splitted) {
			            // compiler lies, tempUser must be created to get here
			            tempUser.addFriend(Integer.parseInt(str));
			        }
			        
			        // Increment the counter to get the chats line next
			        counter++;
			    }
			    
			    // Chats line
			    else {
			        // If they have no chats: add the user to temp, set counter to 0, and continue
			        if (line.trim().equals("")) {
			            temp.put(tempUser.getuID(), tempUser);
			            counter = 0;
			            continue;
			        }
			        
			        // Otherwise, gather the cID's of their chats and add them to tempUser.chats
			        splitted = line.split(",");
			        for (String str : splitted) {
			            // compiler lies, tempUser must be created to get here
			            tempUser.addChat(Integer.parseInt(str));
			        }
			        
			        // Add the user we've been creating to temp (list of users) and set counter to 0 to gather the next users data
			        temp.put(tempUser.getuID(), tempUser);
			        counter = 0;
			    }
			}
			
			// All the user data should now be in temp.  Time to return!
			reader.close();
			this.users = temp;
			return true;
		} catch (Exception e) {
			System.out.println(e);
			
			try {
			    reader.close();
			} catch (Exception f) {
			    System.out.println(f);
			    return false;
			}
			
			return false;
		}
	}
	
	// Saves users to a new db file
	public boolean saveAllUsers() {
	    System.out.println("Saving UserDB...");
	    // Sets the current DB to be dbname with OLD at the end (before the file extension), and returns a new file with the name of the db that is readable and writable
		File db = this.getNextFile();
		
		// if getNextFile failed for some reason, return false
		if (db == null) {
		    return false;
		}
		
		PrintWriter writer = null;
		try {
		    writer = new PrintWriter(db, "UTF-8");
		} catch (Exception e) {
		    System.out.println(e);
		    return false;
		}
		
		String line;
		// Otherwise, we can save this UserDB to file
		for (Map.Entry<Integer, User> me : users.entrySet()) {
		    User user = me.getValue();
		    // First line is the users uID, username, and password
		    writer.println(Integer.toString(user.getuID()) + "," + user.getusername() + "," + user.getpassword());
		    
		    // Next line is the users friends list
		    // For each friend in the users friends list, append their uID and a comma
		    line = "";
		    // If the user has friends...
		    if (!(user.getFriends().size() == 0)) {
    		    for (Integer frienduID : user.getFriends()) {
    		        line += Integer.toString(frienduID) + ",";
    		    }
    		    // Get rid of the last comma in line and write it to the file
    		    line = line.substring(0, line.length()-1);
    		    writer.println(line);
		    }
		    else {
		        writer.println("");
		    }
		    
		    // Next line is the users chat list
		    // For each chat in the chat list, append its cID and a comma
		    line = "";
		    // If there are actually chats the user is in...
		    if (!(user.getChats().size() == 0)) {
    		    for (Integer chatcID : user.getChats()) {
    		        line += Integer.toString(chatcID) + ",";
    		    }
    		    // Get rid of the last comma in line and write to the file
                line = line.substring(0, line.length()-1);
                writer.println(line);
		    }
		    else {
		        writer.println("");
		    }
		}
		// All data of each user should now be in the file
		writer.close();
		System.out.println("UserDB save finished.");
		return true;
	}
	
	public ArrayList<User> getUsers() {
	    return new ArrayList<>(users.values());
	}
	
	public User getUser(String username) {
	    return users.get(nameToUIDMap.get(username));
	}
	
	public User getUser(int uID) {
	    return users.get(uID);
	}
	
	public int validateUser(String username, String password) {
	    return this.getUser(username).validate(username, password);
	}
	
	public boolean usernameExists(String username) {
		if (nameToUIDMap.containsKey(username)) {
			return true;
		}
		return false;
	}
	
	public void registerUser(String username, String password) {
		users.put(highestuID + 1, User.create(highestuID + 1, username, password));
		nameToUIDMap.put(username, highestuID + 1);
		highestuID++;
	}
}
