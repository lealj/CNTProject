package message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageGenerator {

    public byte[] createHandshakeMessage(int peerID) {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        byte[] headerBytes = "P2PFILESHARINGPROJ".getBytes();
        byte[] zeroBits = new byte[10];
        byte[] peerIDBytes = ByteBuffer.allocate(4).putInt(peerID).array();

        try {
            byteOutputStream.write(headerBytes);
            byteOutputStream.write(zeroBits);
            byteOutputStream.write(peerIDBytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                byteOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteOutputStream.toByteArray();
    }

    public byte[] createBitfieldMessage(byte[] bitfield) {
        int totalLength = 0;
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        byte[] lengthBytes;

        byte[] messageTypeBytes = new byte[1];
        messageTypeBytes[0] = 5;

        totalLength += messageTypeBytes.length;
        totalLength += bitfield.length;

        ByteBuffer byteBufferLength = ByteBuffer.allocate(4);
        byteBufferLength.putInt(totalLength);
        lengthBytes = byteBufferLength.array();

        try {
            byteOutputStream.write(lengthBytes);
            byteOutputStream.write(messageTypeBytes);
            byteOutputStream.write(bitfield);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                byteOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteOutputStream.toByteArray();
    }

    public byte[] createChokeMessage() {
        byte[] message = new byte[5];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        buffer.putInt(1);
        buffer.put((byte) 0);

        return message;
    }

    public byte[] createPieceMessage(int pieceIndex, byte[] pieceData) {
        int totalLength = 0;
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        byte[] lengthBytes;
        byte messageType = 7;

        // message type
        totalLength += 1;
        // piece index field
        totalLength += 4;
        // payload
        totalLength += pieceData.length;

        ByteBuffer byteBufferLength = ByteBuffer.allocate(4);
        byteBufferLength.putInt(totalLength);
        lengthBytes = byteBufferLength.array();

        try {
            byteOutputStream.write(lengthBytes);
            byteOutputStream.write(messageType);

            // write piece index field
            ByteBuffer pieceIndexBuffer = ByteBuffer.allocate(4);
            pieceIndexBuffer.putInt(pieceIndex);
            byteOutputStream.write(pieceIndexBuffer.array());

            // write piece data payload
            byteOutputStream.write(pieceData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteOutputStream.toByteArray();
    }

    public byte[] createRequestMessage(int pieceIndex) {
        int totalLength = 0;
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        byte[] lengthBytes;
        byte messageType = 6;

        totalLength += 1;
        totalLength += 4;

        ByteBuffer byteBufferLength = ByteBuffer.allocate(4);
        byteBufferLength.putInt(totalLength);
        lengthBytes = byteBufferLength.array();

        try {
            byteOutputStream.write(lengthBytes);
            byteOutputStream.write(messageType);

            // write piece index field
            ByteBuffer pieceIndexBuffer = ByteBuffer.allocate(4);
            pieceIndexBuffer.putInt(pieceIndex);
            byteOutputStream.write(pieceIndexBuffer.array());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteOutputStream.toByteArray();
    }

    public byte[] createUnchokeMessage() {
        byte[] message = new byte[5];
        ByteBuffer buffer = ByteBuffer.wrap(message);

        // message length (excluding itself)
        buffer.putInt(1);
        // message type: unchoke
        buffer.put((byte) 1);

        return message;
    }

    public byte[] createInterestedMessage() {
        int totalLength = 1;
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(totalLength);
        byte[] lengthBytes = lengthBuffer.array();

        byte[] messageTypeBytes = { 2 };
        try {
            byteOutputStream.write(lengthBytes);
            byteOutputStream.write(messageTypeBytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                byteOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteOutputStream.toByteArray();
    }

    public byte[] createUninterestedMessage() {
        int totalLength = 1;
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(totalLength);
        byte[] lengthBytes = lengthBuffer.array();

        byte[] messageTypeBytes = { 3 };

        try {
            byteOutputStream.write(lengthBytes);
            byteOutputStream.write(messageTypeBytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                byteOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteOutputStream.toByteArray();
    }

    public byte[] createHaveMessage() {
        return null;
    }
}
