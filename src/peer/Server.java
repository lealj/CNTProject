package src.peer;

import java.net.*;
import java.io.*;

// move functionality to p2ppeer
public class Server {
    private static final int sPort = 8000;
    private Peer peer;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket sock;

    public Server(Peer peer) {
        this.peer = peer;
    }

    public void run() throws Exception {
        System.out.println("Server running.");
        ServerSocket listener = new ServerSocket(sPort);

        int peerID = 1001; // example

        try {
            while (true) {
                sock = listener.accept();
            }
        } catch (IOException err) {
            System.err.println("Error with server");
            err.printStackTrace();
        } finally {
            // maybe if statement should go here
            System.out.println("Listener closed");
            listener.close();
        }

    }
    // Implement constructors and methods for listening and accepting incoming
    // connections
    // e.g., startServer(), acceptIncomingConnections(), sendMessage(),
    // receiveMessage(), etc.
}
