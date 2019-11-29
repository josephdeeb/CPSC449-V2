import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class UserDBTest {
    public static void main(String[] args) {
        UserDB db = UserDB.create(new File("").getAbsolutePath(), "users.csv");
        System.out.println("db path = " + db.path + "\ndb name = " + db.dbName + "\ndb file extension = " + db.fileExtension);
        
        User user1 = User.create(0, "joseph", "blah");
        User user2 = User.create(1, "denis", "stuff");
        User user3 = User.create(2, "brian", "password");
        User user4 = User.create(3, "layla", "stuffs");
        
        user1.addFriend(1);
        user2.addFriend(0);
        user2.addFriend(2);
        user3.addFriend(1);
        user3.addFriend(3);
        user4.addFriend(2);
        user4.addFriend(0);
        user1.addFriend(3);
        user1.addFriendRequest(2);
        user2.addFriendRequest(3);
        
        HashMap<Integer, User> users = new HashMap<Integer, User>();
        users.put(user1.getuID(), user1);
        users.put(user2.getuID(), user2);
        users.put(user3.getuID(), user3);
        users.put(user4.getuID(), user4);
        
        db.users = users;
        
        db.saveAllUsers();
        
        db.loadAllUsers();
        String line = "";
        
        for (User user : db.getUsers()) {
            System.out.println("uID=" + user.getuID() + "\nusername=" + user.getusername() + "\npassword=" + user.getpassword());
            for (int i : user.getFriends()) {
                line += Integer.toString(i) + ", ";
            }
            System.out.println("Friends=" + line);
            
            line = "";
            for (int i : user.getFriendRequests()) {
                line += Integer.toString(i) + ", ";
            }
            System.out.println("FriendRequests=" + line + "\n");
            line = "";
        }
        
        // Testing stuff for Chat saving / loading
        
        ChatDB chat0 = ChatDB.create(new File("").getAbsolutePath(), 0, "chat0");
        
        chat0.addUser(0);
        chat0.addUser(1);
        chat0.addUser(2);
        
        Message msg0 = new Message("Haha", 0);
        Message msg1 = new Message("why did the chicken cross the road", 1);
        chat0.addMessage(msg0);
        chat0.addMessage(msg1);
        
        chat0.saveChat();
        
        ChatDB chatLoaded = ChatDB.loadChat(new File("").getAbsolutePath(), 0);
        System.out.println("done");
        
        Message msg2 = new Message("stuff", 0);
        chatLoaded.addMessage(msg2);
        
        chatLoaded.saveChat();
    }
}
