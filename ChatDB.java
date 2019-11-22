import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/*
 * This class represents a single chat's message history and users
 */
public class ChatDB extends DB {
    public static final String folderName = "chats";
    
    private ChatDB(String path, String dbName) {
        super(path, dbName);
    }
    
    public static ChatDB create(String path, String dbName) {
        // Check if the chats folder already exists.  If it doesn't, try to make it.
        try {
            File folder = new File(path);
            if (!folder.exists()) {
                if (!folder.mkdir())
                    throw new IOException("ERROR: Could not create folder for ChatDB");
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        ChatDB temp = new ChatDB(path + File.separator + folderName, dbName);
        if (temp.checkCreate()) {
            return temp;
        }
        return null;
    }
}
