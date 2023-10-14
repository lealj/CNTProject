package src.peer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageHandler {
    // this is the main message handler
    private Peer peer;
    private String message; // message received from the client
    private String MESSAGE; // uppercase message send to the client
    private Socket connection;
    private ObjectInputStream in; // stream read from the socket
    private ObjectOutputStream out; // stream write to the socket

    public MessageHandler(Socket socket, Peer peer, ObjectInputStream in, ObjectOutputStream out) {
        // get sender and receiver info
        this.peer = peer;
        this.connection = socket;
        this.in = in;
        this.out = out;
    }

    public void run() {
        // handshake
        System.out.println("MessageHandler running...");
    }
    // function to decodeMessage: once decoded, rely on other message files to
    // handle based on message type
}
