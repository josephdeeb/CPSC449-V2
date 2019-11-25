
public class Message {
    // contents of messages are delimited by newline chars \n at the end, which means messages cannot contain newline characters
    private String contents;
    private int owner;
    
    public Message(String contents, int owner) {
        this.contents = contents;
        this.owner = owner;
    }
    
    public int getOwner() {
        return owner;
    }
    
    public String getContents() {
        return contents;
    }
}
