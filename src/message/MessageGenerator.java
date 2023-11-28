package message;

import java.nio.ByteBuffer;

public class MessageGenerator {
    /*
     * Deleted message classes may contain
     */

    public byte[] handshakeMessage(int peerID) {

        // Entire message is 32 bytes
        byte[] handshakeMessage = new byte[32];

        // Handshake header is 18 byte string called P2PFILESHARINGPROJ
        System.arraycopy("P2PFILESHARINGPROJ".getBytes(), 0, handshakeMessage, 0, 18);

        // Followed by 10 byte zero bits
        System.arraycopy(new byte[10], 0, handshakeMessage, 18, 10);

        // Followed by 4 byte integer representation of peerID
        System.arraycopy(ByteBuffer.allocate(4).putInt(peerID).array(), 0, handshakeMessage, 28, 4);

        return handshakeMessage;
    }

    public byte[] createBitfieldMessage(byte[] bitfield) {
        int bitfieldLength = (bitfield.length + 7) / 8;
        byte[] message = new byte[bitfieldLength + 5];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        // Message length (excluding itself)
        buffer.putInt(bitfieldLength + 1);
        // Message type: bitfield
        buffer.put((byte) 5);
        // Bitfield payload
        buffer.put(bitfield);

        return message;
    }

    public byte[] sendChokeMessage() {
        byte[] message = new byte[5];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        buffer.putInt(1); // Message length (excluding itself)
        buffer.put((byte) 0); // Message type: choke

        return message;
    }

    public byte[] pieceMessage(int pieceIndex, byte[] payload) {
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

    public byte[] requestMessage(int pieceIndex) {
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

    public byte[] unchokeMessage() {
        byte[] message = new byte[5];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        // Message length (excluding itself)
        buffer.putInt(1);
        // Message type: unchoke
        buffer.put((byte) 1);

        return message;
    }

    public byte[] interestedMessage() {
        byte[] message = new byte[5];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        // Message length
        buffer.putInt(1);
        // Message type
        buffer.put((byte) 3);

        return message;
    }

    public byte[] uninterestedMessage() {
        byte[] message = new byte[5];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        // Message length
        buffer.putInt(1);
        // Message type
        buffer.put((byte) 3);

        return message;
    }
}
