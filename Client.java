import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Client {
    public static final int MAX_MESSAGE_SIZE = 4096;
    public static final String ENCODING = "UTF-8";
    private static ClientConnectionHandler clientConnectionHandler;
    private static UI ui;
    private static boolean running;
    private static ByteBuffer buf;
    public static String username = "";
    
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
                
            case "mainmenu":
                nextUI = parseMainMenu();
                break;
                
            case "chatsmenu":
                nextUI = parseChatsMenu();
                break;
                
            case "friendslist":
                nextUI = parseFriendsList();
                break;
                
            case "accountsettings":
                nextUI = parseAccountSettings();
                break;
                
            case "uploadfile":
                nextUI = parseUploadFile();
                break;
                
            case "downloadfile":
                nextUI = parseDownloadFile();
                break;

            case "deletemessage":
                nextUI =  parseDeleteMessage();
                break;
                
            case "save":
                nextUI = parseSave();
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
        UIPacket temp = ui.login((short)0);
        String msg = temp.args[0] + "," + temp.args[1];
        buf.clear();
        buf.putShort((short)2);
        try {
        	buf.putInt(msg.length());
        	buf.put(msg.getBytes("UTF-8"));
        } catch (Exception e) {
        	System.out.println("Couldn't get bytes from the msg in parseLogin()");
        	return "startup";
        }
        
        buf.flip();
        clientConnectionHandler.sendMessage(buf);
        
        ByteBuffer response = clientConnectionHandler.receiveMessage();
        short type = response.getShort();
        
        return ui.login(type).nextUI;
    }
    
    private static String parseRegister() {
        UIPacket temp = ui.register((short)0);
        // args[0] = username, args[1] = password
        String msg = temp.args[0] + "," + temp.args[1];
        buf.clear();
        // Put 1 as a short in to signal the message type
        buf.putShort((short)1);
        // Next, put the message length and then try to put username,password into buf
        try {
        	buf.putInt(msg.length());
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
        if (type == 1) {
            username = temp.args[0];
        }
        return ui.register(type).nextUI;
    }

    private static String parseDeleteMessage() {
        UIPacket temp = ui.deleteMessage((short)0);
        String msg = temp.args[0] + "," + temp.args[1];
        // args[0] = chatID, args[1] = messageContents
        buf.clear();
        buf.putShort((short)1);
        try {
            buf.putInt(msg.length());
            buf.put(msg.getBytes("UTF-8"));
        } catch (Exception e) {
            System.out.println("ERROR: Could not get bytes from the msg in parseDeleteMessage");
            return "startup";
        }
        buf.flip();
        clientConnectionHandler.sendMessage(buf);

        ByteBuffer response = clientConnectionHandler.receiveMessage();
        short type = response.getShort();

        return ui.deleteMessage(type).nextUI;
    }
    
    private static String parseChatsMenu() {
        
    }
    
    private static String parseFriendsList() {
        
    }
    
    private static String parseAccountSettings() {
        
    }
    
    private static String parseUploadFile() {
        UIPacket temp = ui.uploadFile();
        // If it returned mainmenu, then go back to the mainmenu
        if (temp.nextUI.equals("mainmenu")) {
            return "mainmenu";
        }
        
        try {
            // First put msg type (300), then the length of the file as a long, then the length of the file name + the file name
            ByteBuffer message = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
            File file = new File(temp.args[0]);
            long bytes = file.length();
            message.putShort((short)300);
            message.putLong(bytes);
            
            String fileName = file.getName();
            byte[] b = fileName.getBytes();
            int fileNameSize = b.length;
            
            message.putInt(fileNameSize);
            message.put(b);
            
            message.flip();
            // Send our message
            clientConnectionHandler.sendMessage(message);
            // Await the response
            message = clientConnectionHandler.receiveMessage();
            short response = message.getShort();
            
            if (response == -2) {
                System.out.println("ERROR: The server thinks you are already transferring a file");
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }
            else if (response == -1) {
                System.out.println("ERROR: A file with that name already exists on the server");
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }
            else if (response == 1) {
                System.out.println("Starting file transfer...");
            }
            else if (response == 2) {
                System.out.println("ERROR: Server cannot accept your file.");
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }
            else {
                System.out.println("ERROR: Unknown error type");
                System.out.println("Please press enter to return to the main menu");
                ui.input.nextLine();
                return "mainmenu";
            }
            
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
        
        try {
            ByteBuffer buf = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
            // b is size - 6 because it the first 6 bytes are the message type and then amount of bytes in the message as an integer
            byte[] b = new byte[MAX_MESSAGE_SIZE-6];
            InputStream is = new FileInputStream(temp.args[0]);
            int readBytes = 0;
            
            while ((readBytes = is.read(b)) != -1) {
                // Put message type as 301 (file transfer in progress)
                buf.putShort((short)301);
                // Put the amount of bytes read as an int (4 bytes)
                buf.putInt(readBytes);
                // Then put the actual bytes from the file
                buf.put(b, 0, readBytes);
                // Flip the buffer to prepare it for read operations
                buf.flip();
                // Finally, send the buffer as a message to the server
                clientConnectionHandler.sendMessage(buf);
                buf.clear();
            }
            
            // Once we've finished sending the file, send the teardown message
            clientConnectionHandler.sendMessage((short)302);
            System.out.println("File upload successful!");
            System.out.println("Please press enter to return to the main menu");
            ui.input.nextLine();
            return "mainmenu";
            
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
    }
    
    private static String parseDownloadFile() {
        UIPacket temp = ui.downloadFile(0);
        String filename = temp.args[0];
        String downloadPathName = temp.args[1];
        
        // FIRST OFF, make sure the file doesn't already exist and that we have permission to write to it
        File file = new File(downloadPathName + File.separator + filename);
        File folder = new File(downloadPathName);
        try {
            if (folder.exists()) {
                if (folder.canWrite()) {}
                else
                    throw new IOException("ERROR: You do not have permission to write to the given folder");
            }
            else
                throw new IOException("ERROR: The given folder you're downloading to does not exist");
            
            if (file.exists()) {
                throw new IOException("ERROR: There is already a file in the specified folder with that name");
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
        
        // Sending our initial message
        ByteBuffer message = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
        
        // Put the type at the beginning
        message.putShort((short)303);
        
        // Next turn filename into a byte array
        byte[] filenameBytes = filename.getBytes();
        
        // Put filenameBytes length then the bytes into message
        message.putInt(filenameBytes.length);
        message.put(filenameBytes);
        
        // Don't do the same for downloadPathName, server doesn't need to know it
        
        // flip our message and send it!
        message.flip();
        clientConnectionHandler.sendMessage(message);
        
        // Wait for the response
        message = clientConnectionHandler.receiveMessage();
        
        short response = message.getShort();
        // Already transferring file
        if (response == (short)-1) {
            System.out.println("ERROR: You are already transferring a file, according to the server");
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
        else if (response == (short)2) {
            System.out.println("ERROR: Requested file " + filename + " does not exist on the server.");
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
        // Success!  Start getting ready to read the file bytes
        else if (response == (short)1) {
            System.out.println("Starting file transfer...");
        }
        else {
            System.out.println("ERROR: Unknown response from server");
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
        // If we've gotten here, we're about to start accepting the file transfer
        
        // First create the file and get the output stream
        OutputStream os = null;
        try {
            file.createNewFile();
            os = new FileOutputStream(file);
        } catch (Exception e) {
            System.out.println("ERROR: Failed to create the local file.  Cancelling file transfer...");
            // send cancel file transfer message to reset server session data
            clientConnectionHandler.sendMessage((short)306);
            System.out.println("Please press enter to continue");
            ui.input.nextLine();
            return "mainmenu";
        }
        
        // Ask for bytes and receive them until there are none left!
        boolean transferring = true;
        while (transferring) {
            clientConnectionHandler.sendMessage((short)304);
            message = clientConnectionHandler.receiveMessage();
            
            response = message.getShort();
            
            if (response == -1) {
                System.out.println("ERROR: The server says you are not transferring a file right now");
                clientConnectionHandler.sendMessage((short)306);
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }
            else if (response == -2) {
                System.out.println("ERROR: The server failed to read the file on the server-side");
                clientConnectionHandler.sendMessage((short)306);
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }
            else if (response == 0) {
                // success!
            }
            else {
                System.out.println("ERROR: Unknown message from server.  Aborting file transfer.");
                clientConnectionHandler.sendMessage((short)306);
                System.out.println("Please press enter to continue");
                ui.input.nextLine();
                return "mainmenu";
            }
            
            // Next get an int
            
            int bytesToRead = message.getInt();
            
            // If its -1, that means file transfer is complete
            if (bytesToRead == -1) {
                System.out.println("Finished transferring file!");
                transferring = false;
                break;
            }
            // Otherwise, we're about to write that many bytes to our file
            else {
                byte[] fileBytes = new byte[bytesToRead];
                message.get(fileBytes);
                try {
                    os.write(fileBytes);
                } catch (Exception e) {
                    System.out.println("ERROR: Failed to write bytes from server to the file.  Aborting file transfer.");
                    clientConnectionHandler.sendMessage((short)306);
                    System.out.println("Please press enter to continue");
                    ui.input.nextLine();
                    return "mainmenu";
                }
            }
        }
        // After while(transferring)
        System.out.println("File successfully transferred!");
        clientConnectionHandler.sendMessage((short)306);
        try {
            os.close();
        } catch (Exception e) {
            System.out.println("ERROR: Failed to close file stream");
        }
        
        return "mainmenu";
    }
    
    private static String parseMainMenu() {
        return ui.mainMenu().nextUI;
    }
    
    private static String parseSave() {
        // No UI needed
        // Just put message type 310 and send
        ByteBuffer message = ByteBuffer.allocate(2);
        message.putShort((short)310);
        message.flip();
        
        clientConnectionHandler.sendMessage(message);
        
        System.out.println("\n\nSave message sent successfully\n\n");
        
        return "startup";
    }
    
}
