package src.message;

import java.nio.ByteBuffer;

import src.peer.MessageHandler;

public class Uninterested extends MessageHandler {

    public Uninterested(int sender, int receiver) {
        super(sender, receiver);
    }

    public static byte[] sendInterestedMessage() {
        byte[] message = new byte[5];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        // Message length
        buffer.putInt(1);
        // Message type
        buffer.put((byte) 3);

        return message;
    }

}
