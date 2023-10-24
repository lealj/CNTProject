package src.message;

import java.nio.ByteBuffer;

import src.peer.MessageHandler;

public class Unchoke extends MessageHandler {
    public Unchoke(int sender, int receiver) {
        super(sender, receiver);
    }

    public void performUnchoke(int receiver) {

    }

    public static byte[] sendUnchokeMessage() {
        byte[] message = new byte[5];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        // Message length (excluding itself)
        buffer.putInt(1);
        // Message type: unchoke
        buffer.put((byte) 1);

        return message;
    }
}
