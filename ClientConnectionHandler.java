/*
 * TCP Client for our messaging system, heavily modified from TCPClient.java created by Prof. Mea Wang
 */

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class ClientConnectionHandler {
    public static int MAX_MESSAGE_SIZE = 4096;

    private String serverIP;
    private int serverPort;

    private Socket clientSocket;
    private DataOutputStream outBuffer;
    private DataInputStream inBuffer;

    // Factory constructor to ensure no errors with initialization
    // Returns null if initialization fails
    public static ClientConnectionHandler create(String serverIP, int serverPort) {
        // Create the handler
        ClientConnectionHandler handler = new ClientConnectionHandler(serverIP, serverPort);
        // Try to initialize it
        if (!handler.init())
            return null;

        return handler;
    }

    private ClientConnectionHandler(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    private boolean init() {
        try {
            // Initialize client socket connection to server
            Socket clientSocket = new Socket(serverIP, serverPort);
            // Initialize input/output streams for the connection
            outBuffer = new DataOutputStream(clientSocket.getOutputStream());
            inBuffer = new DataInputStream(clientSocket.getInputStream());
            return true;

        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public void close() {
        try {
            clientSocket.close();
        } catch (Exception e) {
            System.out.println("Failed to close socket");
            return;
        }
    }
    
    public boolean sendMessage(ByteBuffer message) {
        try {
            byte[] tempBytes = new byte[message.remaining()];
            message.get(tempBytes);
            outBuffer.write(tempBytes);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public ByteBuffer receiveMessage() {
        try {
            byte[] message = new byte[MAX_MESSAGE_SIZE];
            if (inBuffer.read(message) <= 0)
                throw new IOException("Didn't read any bytes from inBuffer");

            return ByteBuffer.wrap(message);

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
