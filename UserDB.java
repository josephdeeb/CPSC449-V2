import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UserDB {
	public final Path path;
	
	private UserDB(String sPath) {
		this.path = Paths.get(sPath);
	}
	
	// Returns null if the userdb is not readable or not writable
	// If the userDB doesn't exist, it creates one, and if it fails to create one, it returns null
	public UserDB create(String sPath) {
		UserDB returned = new UserDB(sPath);
		
		// Check if the UserDB file exists and is readable + writable
		File userDB = new File(sPath);
		if (!userDB.exists()) {
			try {
				userDB.createNewFile();
			} catch (Exception e) {
				System.out.println(e);
				return null;
			}
		}
		
		if (!(Files.isReadable(returned.path) && Files.isWritable(returned.path)))
			return null;
		
		else
			return returned;
	}
	
	// NOT FINISHED
	public ArrayList<User> getAllUsers() {
		File db = new File(path.toString());
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
	
	public boolean saveAllUsers(ArrayList<User> users) {
		File db = new File(path.toString());
	}
}
