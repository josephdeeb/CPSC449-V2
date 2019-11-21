import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UserDB {
    // Path to the folder containing the db
	public final String path;
	// Name of the db file, including the file extension
	public final String dbName;
	// File extension of the db file
	public final String fileExtension;
	// List of users
	public ArrayList<User> users;
	
	/*
	 * path is a path to the folder containing the db
	 * dbName is the name of the db file
	 */
	private UserDB(String path, String dbName) {
		this.path = path;
		this.dbName = dbName;
		this.fileExtension = "." + dbName.split(".")[1];
	}
	
	// Returns null if the userdb is not readable or not writable
	// If the userDB doesn't exist, it creates one, and if it fails to create one, it returns null
	// path is a path to the folder containing the db, dbName is the name of the db file
	public static UserDB create(String path, String dbName) {
	    // First make sure dbName contains no periods other than the file extension so as to not break getdbNameNoExtension()
	    if (dbName.split(".").length != 2) {
	        System.out.println("ERROR: dbName may contain only one period '.' character");
	        return null;
	    }
	    
		UserDB returned = new UserDB(path, dbName);
		
		// Check if the UserDB file exists and is readable + writable
		File userDB = new File(returned.getdbPath());
		if (!userDB.exists()) {
			try {
				userDB.createNewFile();
			} catch (Exception e) {
				System.out.println(e);
				return null;
			}
		}
		
		if (!(Files.isReadable(Paths.get(returned.getdbPath())) && Files.isWritable(Paths.get(returned.getdbPath()))))
			return null;
		
		else
			return returned;
	}
	
	public String getdbPath() {
	    return this.path + File.separator + this.dbName;
	}
	
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
	
	// Returns dbName minus the file extension
	// Make sure dbName contains no dots other than the file extension
	public String getdbNameNoExtension() {
	    return this.dbName.split(".")[0];
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
		for (User user : users) {
		    // First line is the users uID, username, and password
		    writer.println(Integer.toString(user.getuID()) + "," + user.getusername() + "," + user.getpassword());
		    
		    // Next line is the users friends list
		    // For each friend in the users friends list, append their uID and a comma
		    line = "";
		    for (User friend : user.getFriends()) {
		        line += Integer.toString(friend.getuID()) + ",";
		    }
		    // Get rid of the last comma in line and write it to the file
		    line = line.substring(0, line.length()-1);
		    writer.println(line);
		    
		    // Next line is the users chat list
		    // For each chat in the chat list, append its cID and a comma
		    line = "";
		    for (Chat chat : user.getChats()) {
		        line += Integer.toString(chat.getcID()) + ",";
		    }
		    // Get rid of the last comma in line and write to the file
		    line = line.substring(0, line.length()-1);
		    writer.println(line);
		}
		// All data of each user should now be in the file
		writer.close();
		System.out.println("UserDB save finished.");
		return true;
	}
	
	/*
	 * Function that returns file for the next save, and renames the old file
	 */
	public File getNextFile() {
	    File db = new File(this.getdbPath());
	    File dbOld = new File(this.getdbNameNoExtension() + "." + this.fileExtension);
	    
	    // if the dbOld file already exists, delete it
	    if (dbOld.exists())
	        dbOld.delete();
	    
	    // Try to rename the current db file to be the old db file
	    if (db.renameTo(dbOld)) {
	        // If successful, create a new db file and try to create it
	        File nextdb = new File(this.getdbPath());
	        try {
	            if (nextdb.createNewFile()) {
	                nextdb.setReadable(true);
	                nextdb.setWritable(true);
	                return nextdb;
	            }
	            else
	                throw new IOException("ERROR: Could not create new db file");
	        } catch (Exception e) {
	            System.out.println(e);
	            return null;
	        }
	    }
	    // Otherwise, we couldn't rename the current db file so return null
	    // Keep in mind the old db file will still be deleted regardless of if we successfully rename the current db file
	    else
	        return null;
	}
}
