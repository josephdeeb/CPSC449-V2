/*
 * Heavily modified version of SelectServer.java created by Prof. Mea Wang
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Set;

public class ConnectionHandler {
    public static final int MAX_MESSAGE_SIZE = 4096;

    private int listeningPort;
    private Charset charset;
    private CharsetDecoder decoder;
    private CharsetEncoder encoder;
    private ByteBuffer inBuffer;
    private CharBuffer cBuffer;
    private int bytesSent, bytesRecv;
    
    private Selector selector;
    private ServerSocketChannel channel;
    private InetSocketAddress isa;

    
    // Factory constructor to ensure no errors with initialization
    // Returns null if there were errors
    public static ConnectionHandler create(int listeningPort) {
        // Create the handler
        ConnectionHandler handler = new ConnectionHandler(listeningPort);
        
        // Try to initialize it.  If there were any errors, return null
        if (handler.init() == false)
            return null;
        
        // Otherwise, there were no errors with initialization, so we return the handler
        return handler;
    }
    
    // Private constructor, factory method 'create' is the public constructor
    private ConnectionHandler(int listeningPort) {
        this.listeningPort = listeningPort;
        this.charset = Charset.forName("us-ascii");
        this.decoder = charset.newDecoder();
        this.encoder = charset.newEncoder();
        this.inBuffer = null;
        this.cBuffer = null;
    }
    
    // Initializes the handler, used by create
    private boolean init() {
        try {
            // Initialize selector
            selector = Selector.open();
            
            // Create non-blocking server channel
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            
            // Bind socket
            isa = new InetSocketAddress(listeningPort);
            channel.socket().bind(isa);
            
            // Register our selector, accepting connection requests
            channel.register(selector,  SelectionKey.OP_ACCEPT);
            
            return true;
            
        } catch (Exception e) {
            System.out.println(e);
            return false;
            
        }
    }
    
    public void run() {
        boolean terminated = false;
        while (!terminated) {
            try {
                // Try to select a set of keys with timeout of 500 ms
                if (selector.select(500) < 0) {
                    throw new IOException("select() timed out");
                }
            } catch (Exception e) {
                System.out.println(e);
                terminated = true;
                break;
            }
            
            // Get set of ready sockets
            Set readyKeys = selector.selectedKeys();
            Iterator readyItor = readyKeys.iterator();
            
            // Go through the ready set
            while (readyItor.hasNext()) {
                // Get key from the set
                SelectionKey key = (SelectionKey)readyItor.next();
                
                // Remove current entry
                readyItor.remove();
                
                // Accept new connections, if any
                if (key.isAcceptable()) {
                    try {
                        SocketChannel cchannel = ((ServerSocketChannel)key.channel()).accept();
                        cchannel.configureBlocking(false);
                        System.out.println("Accept connection from " + cchannel.socket().toString());
                        
                        // Register the new connection for read operation
                        cchannel.register(selector, SelectionKey.OP_READ);
                    } catch (Exception e) {
                        System.out.println(e);
                        key.cancel();
                    }
                }
                else {
                    try {
                        SocketChannel cchannel = (SocketChannel) key.channel();
                        // If the given SocketChannel has been registered and can be read...
                        if (key.isReadable()) {
                            // Retrieve the message from the socket
                            inBuffer = retrieveMessage(cchannel);

                            // Occurs if read() failed or the connection is closed
                            if (inBuffer == null) {
                                key.cancel();
                                continue;
                            }

                            System.out.println(inBuffer);

                            // Next, pass the buffer off to handleMessage which will, y'know, handle the message
                            if (!handleMessage(inBuffer, cchannel)) {
                                Server.disconnectUser(cchannel);
                                key.cancel();
                                continue;
                            }

                            // At this point the message has been dealt with
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        Set keys = selector.keys();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            try {
                SelectionKey key = (SelectionKey)itr.next();
                if (key.isAcceptable())
                    ((ServerSocketChannel)key.channel()).socket().close();
                else if (key.isValid())
                    ((SocketChannel)key.channel()).socket().close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
    
    // Returns a ByteBuffer with the contents from cchannel's socket.  ByteBuffer is already flipped
    private ByteBuffer retrieveMessage(SocketChannel cchannel) {
        try {
            ByteBuffer buf = ByteBuffer.allocateDirect(MAX_MESSAGE_SIZE);
            
            // Read from socket
            bytesRecv = cchannel.read(buf);
            if (bytesRecv <= 0) {
                throw new IOException("read() error, or connection closed");
            }
            buf.flip();
            
            return buf;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
    
    // If the boolean returned is false, the key will be cancelled.
    /*
     * This is where all of the servers message handling logic is contained.
     * 
     * The first short of each initial message will tell handleMessage what type of message
     * it is.  For example, if type = 1 means its a message to a chat room, then the logic for
     * handling messages to chat room should be contained in case 1
     * 
     * HOW TO DO:
     *      1) Find a free message type (i.e. if type = 37 hasn't been used, you can use that type)
     *      2) Create a function that handles the logic for that specific message type
     *      3) Call that function from the case for your chosen type
     * 
     * NOTE:
     *      Make sure each case you write is followed by a break statement.
     *      If you need to terminate the connection with the user that sent the message, put "return false;" at the end of your case
     */

    private boolean handleMessage(ByteBuffer message, SocketChannel sock) throws IOException {
        short type = message.getShort();
        int port = sock.socket().getPort();
        switch(type) {
            // register
        	case 1:
        	    Server.handleRegister(message, sock);
        	    break;
        	// login
        	case 2:
        		Server.handleLogin(message, sock);
        		break;
			case 3:
				Server.handleDisplayFriends(message, sock);
				break;
			case 4:
				Server.handleSendFriendRequest(message, sock);
				break;
			case 5:
				Server.handleDisplayFriendRequests(message, sock);
				break;
			case 6:
				Server.handleAcceptFriendRequest(message, sock);
			
            // send message to chat
            case 14:
                Server.handleSendChatMesssage(message, sock);
                break;
            // get chat history
            case 15:
                Server.handleGetChatHistory(message, sock);
                break;
            // delete message
            case 16:
                Server.handleDeleteMessage(message, sock);
            // create chat
            case 17:
                Server.handleCreateChat(message, sock);
                break;
            // add chat user
            case 18:
                Server.handleAddChatUser(message, sock);
                break;
            // remove chat user
            case 19:
                Server.handleRemoveChatUser(message, sock);
                break;
            // get chat list
            case 20:
                Server.handleGetChatList(message, sock);
                break;
        	// upload file
        	case 300:
        	    Server.handleUpload(message, sock);
        	    break;
        	// Mid-progress upload file
        	case 301:
        	    Server.handleUploadInProgress(message, sock);
        	    break;
        	// Upload file finish
        	case 302:
        	    Server.handleUploadFinish(message, sock);
        	    break;
        	// Download file
        	case 303:
        	    Server.handleDownload(message, sock);
        	    break;
        	// Mid-progress download file
        	case 304:
        	    Server.handleDownloadSendBytes(message, sock);
        	    break;
        	// Cancel download file
        	case 306:
        	    Server.handleDownloadCancel(message, sock);
        	    break;
        	// Save everything
        	case 310:
        	    Server.handleSave(message, sock);
        	    break;
			case 400:
				Server.handleChangeUsername(message, sock);
				break;
			case 401:
				Server.handleChangePassword(message, sock);
				break;
			case 666:
				Server.handleDeleteAccount(message, sock);
				break;
        		
        default:
            // should never hit this
            System.out.println("ERROR: Unknown Message Type " + type);
        }
        
        return true;
    }
    
    public String retrieveString(ByteBuffer msg, int len) {
    	byte[] temp = new byte[len];

    	try {
    		msg.get(temp);
    		return new String(temp, "UTF-8");
    	} catch (Exception e) {
    		System.out.println(e);
    		return null;
    	}
    }
    
    // Shouldn't use this, kinda dangerous, could stall server
    public ByteBuffer awaitMessage(SocketChannel channel) {
        ByteBuffer buf = ByteBuffer.allocateDirect(MAX_MESSAGE_SIZE);
        try {
            if (channel.read(buf) <= 0) {
                throw new IOException("read() error, or connection closed");
            }
            buf.flip();
            return buf;
        } catch (Exception e) {
            System.out.println(e);
            buf = null;
            return buf;
        }
    }
    
    public boolean sendError(SocketChannel channel) {
    	ByteBuffer buf = ByteBuffer.allocate(2);
    	buf.putShort((short)-1);
    	buf.flip();
    	return sendMessage(channel, buf);
    }
    
    public int countOccurrences(String message, char character) {
    	int count = 0;
    	for (int i = 0; i < message.length(); i++) {
    		if (message.charAt(i) == character) {
    			count++;
    		}
    	}
    	return count;
    }
    
    public boolean sendMessage(SocketChannel channel, short message) {
    	ByteBuffer buf = ByteBuffer.allocate(2);
    	buf.putShort(message);
    	buf.flip();
    	return this.sendMessage(channel, buf);
    }
    
    public boolean sendMessage(SocketChannel channel, byte[] message) {
        ByteBuffer buf = ByteBuffer.wrap(message);
        return this.sendMessage(channel,  buf);
    }
    
    public boolean sendMessage(SocketChannel channel, ByteBuffer message) {
        try {
            channel.write(message);
            return true;
        } catch (Exception e) {
            System.out.println("sendMessage cannot write to the channel");
            return false;
        }
        
    }

}