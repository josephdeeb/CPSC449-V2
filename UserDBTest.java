import java.io.File;
import java.util.ArrayList;

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
        
        ArrayList<User> users = new ArrayList<User>();
        users.add(user1);
        users.add(user2);
        users.add(user3);
        users.add(user4);
        
        db.users = users;
        
        db.saveAllUsers();
        
        users = db.loadAllUsers();
        for (User user : users) {
            System.out.println("uID=" + user.getuID() + "\nusername=" + user.getusername() + "\npassword=" + user.getpassword() + "\n");
        }
    }
}
