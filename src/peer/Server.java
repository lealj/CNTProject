package src.peer;

import src.peer.MessageHandler;
import src.peer.Peer;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

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
                MessageHandler messageHandler = new MessageHandler(sock, peer, in, out);
                messageHandler.run();
                System.out.println("Peer connected.");
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
