import java.util.*;

public class Chat {
    private int cID;
    private String name;
    private ArrayList<User> users;
    private History history;
    
    public Chat(int cID, String name, ArrayList<User> users) {
        this.cID = cID;
        this.name = name;
        this.users = users;
    }
    
    public int getcID() {
        return cID;
    }
    
    public String getName() {
        return name;
    }
    
    // Adds user to users ArrayList.  If the user is already there, returns false.  Otherwise, returns true.
    public boolean addUser(User user) {
        if (users.contains(user)) {
            return false;
        }
        else {
            users.add(user);
            return true;
        }
    }
}
