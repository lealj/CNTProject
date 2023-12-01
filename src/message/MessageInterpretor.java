package message;

import java.nio.ByteBuffer;

public class MessageInterpretor {
    public int getIdFromHandshake(byte[] handshakeMessage) {
        ByteBuffer buffer = ByteBuffer.wrap(handshakeMessage, 28, 4);
        int peerID = buffer.getInt();
        return peerID;
    }

    public byte[] getBitfieldFromMessage(byte[] message) {
        byte[] bitfield = null;

        // Ensure the message is at least 5 bytes long (length + type)
        if (message.length >= 5) {
            ByteBuffer buffer = ByteBuffer.wrap(message);

            // Read the length of the message from the first 4 bytes
            int length = buffer.getInt();

            // Ensure the length matches the actual message length
            if (length == message.length - 4) {
                // Read the message type
                byte messageType = buffer.get();

                // Check if the message type is for a bitfield (type 5)
                if (messageType == 5) {
                    // Extract the bitfield bytes
                    bitfield = new byte[length - 1]; // length - 1 (excluding type byte)
                    buffer.get(bitfield);
                }
            }
        }

        return bitfield;
    }

    public int getPieceIndex(byte[] message) {
        ByteBuffer pieceIndexBuffer = ByteBuffer.wrap(message, 5, 4);
        int pieceIndex = pieceIndexBuffer.getInt();

        return pieceIndex;
    }

    public byte[] getPieceContent(int pieceSize, byte[] message) {
        byte[] pieceData = null;

        int totalLength = ByteBuffer.wrap(message, 0, 4).getInt();
        // Extract piece data
        pieceData = new byte[totalLength - 5];

        // investigate if srcPos value breaks this in different file
        System.arraycopy(message, 9, pieceData, 0, pieceData.length);

        return pieceData;
    }
}
