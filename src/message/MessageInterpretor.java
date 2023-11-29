package message;

import java.nio.ByteBuffer;

public class MessageInterpretor {
    public int getIdFromHandshake(byte[] handshakeMessage) {
        ByteBuffer buffer = ByteBuffer.wrap(handshakeMessage, 28, 4);
        int peerID = buffer.getInt();
        return peerID;
    }
}
