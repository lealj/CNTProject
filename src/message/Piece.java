package src.message;

import java.nio.ByteBuffer;

import src.peer.MessageHandler;

public class Piece extends MessageHandler {

    public Piece(int sender, int receiver) {
        super(sender, receiver);

    }

    public static byte[] sendPieceMessage(int pieceIndex, byte[] payload) {
        int payloadLength = payload.length;
        byte[] message = new byte[payloadLength + 9];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        // Message length
        buffer.putInt(payloadLength + 5);
        // Message type:
        buffer.put((byte) 7);
        // Piece index
        buffer.putInt(pieceIndex);
        // Piece data payload
        buffer.put(payload);

        return message;
    }

}
