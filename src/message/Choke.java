package src.message;

import java.nio.ByteBuffer;

import src.peer.MessageHandler;

public class Choke extends MessageHandler {
    public Choke(int sender, int receiver) {
        super(sender, receiver);
    }

    public void performChoke(int receiver) {
        // get peer object from list of peers

        // make sure our peer is connected to the one they want to choke

        // create new
    }

    public static byte[] sendChokeMessage() {
        byte[] message = new byte[5];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        buffer.putInt(1); // Message length (excluding itself)
        buffer.put((byte) 0); // Message type: choke

        return message;
    }
}
