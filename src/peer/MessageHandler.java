package src.peer;

import src.message.Choke;

public class MessageHandler {
    public int sender;
    public int receiver;

    public MessageHandler(int sender, int receiver) {
        // get sender and receiver info
        this.sender = sender;
        this.receiver = receiver;
    }

    public static MessageHandler interpretMessage(String msg, int sender, int receiver) {
        byte[] msg_bytes = msg.getBytes();
        byte msgTypeValue = msg_bytes[4];
        MessageType messageType = MessageType.fromValue(msgTypeValue);

        switch (messageType) {
            case BITFIELD:
            case CHOKE:
                return new Choke(sender, receiver);
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
