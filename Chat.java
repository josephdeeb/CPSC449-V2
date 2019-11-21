import java.util.*;

public class Chat {
    private int cID;
    private String name;
    private ArrayList<User> users;
    private History history;
    
    public Chat(int cID, String name, ArrayList<User> users) {
        
    }
    
    public int getcID() {
        return cID;
    }
    
    public String getName() {
        return name;
    }
}
