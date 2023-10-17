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
    private ObjectInputStream in; // stream read from the socket ( this may be able to be switched to
                                  // DataInputStream)
    private ObjectOutputStream out; // stream write to the socket ( this may be able to be switched to
                                    // DataInputStream)

    public MessageHandler(Socket socket, Peer peer, ObjectInputStream in, ObjectOutputStream out) {
        // get sender and receiver info
        this.peer = peer;
        this.connection = socket;
        this.in = in;
        this.out = out;
    }

    public static MessageHandler interpretMessage(String msg, int senderID, int receiverID) {
        byte[] msg_bytes = msg.getBytes();
        byte msgTypeValue = msg_bytes[4];
        MessageType messageType = MessageType.fromValue(msgTypeValue);

        switch (messageType) {
            case BITFIELD:
            case CHOKE:
            case HANDSHAKE:
            case INTERESTED:
            case PIECE:
            case REQUEST:
            case UNCHOKE:
            case UNINTERESTED:
        }

        return null;
    }

}
