import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ActualMessage {

    // 4-byte message length field
    private int length;  
    
    // 1-byte message type field
    private byte type;   
    
    // Message payload with variable size
    private byte[] payload;  

    // Message types
    public static final byte CHOKE = 0;
    public static final byte UNCHOKE = 1;
    public static final byte INTERESTED = 2;
    public static final byte NOT_INTERESTED = 3;
    public static final byte HAVE = 4;
    public static final byte BITFIELD = 5;
    public static final byte REQUEST = 6;
    public static final byte PIECE = 7;

    public ActualMessage() {
        payload = null;
        length = 0;
        type = 0;
    }

    // Read Actual message from socket input stream
    public void read(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();

        // Read the 4-byte message length field
        byte[] lengthBytes = new byte[4];
        in.read(lengthBytes);
        length = ByteBuffer.wrap(lengthBytes).getInt();

        // Read the 1-byte message type field
        type = (byte) in.read();

        // Determine payload size based on message length
        if (length > 5) {
            payload = new byte[length - 5];
            in.read(payload);
        }

        else {
            payload = null;
        }
    }

    // Send an actual message to a socket output stream
    public void send(Socket socket) throws IOException {
        OutputStream out = socket.getOutputStream();

        // Calculate message length based on payload
        if (payload == null) {
            length = 5;
        }

        else {
            length = 5 + payload.length;
        }

        // Write the 4-byte message length field
        out.write(ByteBuffer.allocate(4).putInt(length).array());

        // Write the 1-byte message type field
        out.write(type);

        // Write the message payload (if not null)
        if (payload != null) {
            out.write(payload);
        }

        out.flush();
    }

    // Getter and setter methods
    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
