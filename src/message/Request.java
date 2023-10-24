package src.message;

import java.nio.ByteBuffer;

import src.peer.MessageHandler;

public class Request extends MessageHandler {

    public Request(int sender, int receiver) {
        super(sender, receiver);

    }

    public static byte[] sendRequestMessage(int pieceIndex) {
        byte[] message = new byte[9];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        // Message length
        buffer.putInt(5);
        // Message type
        buffer.put((byte) 6);
        // insert piece index
        buffer.putInt(pieceIndex);

        return message;
    }

}
