import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UserDB extends DB {
	// List of users
	public ArrayList<User> users;
	
	/*
	 * path is a path to the folder containing the db
	 * dbName is the name of the db file
	 */
	private UserDB(String path, String dbName) {
	    super(path, dbName);
	    users = new ArrayList<User>();
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
	// NOT FINISHED
	public ArrayList<User> getAllUsers() {
		File db = new File(this.getdbPath());
		try {
			// First make sure the database file exists
			if (!db.exists())
				throw new IOException("ERROR: getAllUsers() failed because the database stored in UserDB.path doesn't exist");
			
			// Then establish the reader
			BufferedReader reader = new BufferedReader(new FileReader(db));
			
			// Next, create the ArrayList
			ArrayList<User> users = new ArrayList<User>();
			
			// Now start reading in lines
			String line = reader.readLine();
			String[] lineSplit = line.split(",");
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}
	*/
	
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
		for (User user : users) {
		    // First line is the users uID, username, and password
		    writer.println(Integer.toString(user.getuID()) + "," + user.getusername() + "," + user.getpassword());
		    
		    // Next line is the users friends list
		    // For each friend in the users friends list, append their uID and a comma
		    line = "";
		    // If the user has friends...
		    if (!(user.getFriends().size() == 0)) {
    		    for (User friend : user.getFriends()) {
    		        line += Integer.toString(friend.getuID()) + ",";
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
    		    for (Chat chat : user.getChats()) {
    		        line += Integer.toString(chat.getcID()) + ",";
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
	
}
