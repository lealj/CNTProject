package src.message;

import java.nio.ByteBuffer;

import src.peer.MessageHandler;

public class Interested extends MessageHandler {

    public Interested(int sender, int receiver) {
        super(sender, receiver);
    }

    public static byte[] sendInterestedMessage() {
        byte[] message = new byte[5];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        // Message length
        buffer.putInt(1);
        // Message type: interested
        buffer.put((byte) 2);

        return message;
    }

}
