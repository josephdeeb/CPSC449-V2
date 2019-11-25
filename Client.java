import java.io.IOException;
import java.nio.ByteBuffer;

public class Client {
    public static final int MAX_MESSAGE_SIZE = 4096;
    public static final String ENCODING = "UTF-8";
    private static ClientConnectionHandler clientConnectionHandler;
    private static UI ui;
    private static boolean running;
    private static ByteBuffer buf;
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("ERROR: Incorrect number of arguments.  Proper usage: java Client [server ip] [server port]");
        }
        
        try {
            clientConnectionHandler = ClientConnectionHandler.create(args[0], Integer.parseInt(args[1]));
            if (clientConnectionHandler == null)
                throw new IOException("Failed to establish connection on the given port.");
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
        
        buf = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
        
        ui = new UI();
        running = true;
        String next = "startup";
        while (running) {
            next = parseUI(next);
        }
    }
    
    private static String parseUI(String selection) {
        String nextUI;
        
        switch (selection) {
            case "startup":
                nextUI = parseStartup();
                break;
            
            case "login":
                nextUI = parseLogin();
                break;
                
            case "register":
                nextUI = parseRegister();
                break;
                
            case "exit":
                running = false;
                nextUI = null;
                break;
                
            default:
                nextUI = "exit";
                System.out.println("ERROR: UI Type \"" + selection + "\" unknown...");
        }
        
        return nextUI;
    }
    
    private static String parseStartup() {
        return ui.startup().nextUI;
    }
    
    private static String parseLogin() {
        
    }
    
    private static String parseRegister() {
        UIPacket temp = ui.register((short)0);
        // args[0] = username, args[1] = password
        String msg = temp.args[0] + "," + temp.args[1];
        buf.clear();
        // Put 1 as a short in to signal the message type
        buf.putShort((short)1);
        // Next, try to put username,password into buf
        try {
            buf.put(msg.getBytes("UTF-8"));
        } catch (Exception e) {
            System.out.println("ERROR: Could not get bytes from the msg in parseRegister");
            return "register";
        }
        // Flip the buf before we send
        buf.flip();
        
        // Finally, send the messsage!
        clientConnectionHandler.sendMessage(buf);
        
        // Await our response
        ByteBuffer response = clientConnectionHandler.receiveMessage();
        
        short type = response.getShort();
        // 1 means successful registration
        return ui.register(type).nextUI;
    }
    
    
}
