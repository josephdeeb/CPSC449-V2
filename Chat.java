import java.io.File;
import java.util.*;

public class Chat {
    private int cID;
    private String name;
    // List of uID's of users
    private ArrayList<Integer> users;
    private ChatDB db;
    
    public Chat(int cID, String name, ArrayList<Integer> users) {
        this.cID = cID;
        this.name = name;
        this.users = users;
        this.db = ChatDB.create(new File("").getAbsolutePath(), Integer.toString(cID));
    }
    
    public int getcID() {
        return cID;
    }
    
    public String getName() {
        return name;
    }
    
    // Adds user to users ArrayList.  If the user is already there, returns false.  Otherwise, returns true.
    public boolean addUser(int uID) {
        if (users.contains(uID)) {
            return false;
        }
        else {
            users.add(uID);
            return true;
        }
    }
}
