package src.message;

import java.nio.ByteBuffer;

import src.peer.MessageHandler;

public class Bitfield extends MessageHandler {

    public Bitfield(int sender, int receiver) {
        super(sender, receiver);

    }

    public static byte[] createBitfieldMessage(byte[] bitfield) {
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

}
