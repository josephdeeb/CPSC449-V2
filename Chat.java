import java.io.File;
import java.util.*;

public class Chat {
    /*
    private int cID;
    private String name;
    // List of uID's of users
    private ChatDB db;
    
    private Chat(int cID, String name) {
        this.cID = cID;
        this.name = name;
        this.db = ChatDB.create(new File("").getAbsolutePath(), Integer.toString(cID) + ".csv");
    }
    
    public static Chat create(int cID, String name) {
        Chat temp = new Chat(cID, name);
        if (temp.db == null) {
            return null;
        }
        return temp;
    }
    
    public int getcID() {
        return cID;
    }
    
    public String getName() {
        return name;
    }
    
    // Adds user to users ArrayList.  If the user is already there, returns false.  Otherwise, returns true.
    public boolean addUser(int uID) {
        return db.addUser(uID);
    }
    
    public boolean saveChat() {
        return db.saveChat(cID, name);
    }
    
    public void addMessage(Message msg) {
        db.addMessage(msg);
    }
    
    public boolean loadChat() {
        return db.loadChat();
    }
    */
}
