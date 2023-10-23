import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

// Class for the handshake message
public class HandShake {

    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private int peerID;

    public HandShake(Socket socket, int peerID) throws IOException {

        // Initialize the socket, peerID, input stream, and output stream
        this.socket = socket;
        this.peerID = peerID;

        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

    }

    public void sendHandshake() throws IOException {

        // Entire message is 32 bytes
        byte[] handshakeMessage = new byte[32];

        // Handshake header is 18 byte string called P2PFILESHARINGPROJ
        System.arraycopy("P2PFILESHARINGPROJ".getBytes(), 0, handshakeMessage, 0, 18);

        // Followed by 10 byte zero bits
        System.arraycopy(new byte[10], 0, handshakeMessage, 18, 10);

        // Followed by 4 byte integer representation of peerID
        System.arraycopy(ByteBuffer.allocate(4).putInt(peerID).array(), 0, handshakeMessage, 28, 4);

        // Send message to output stream
        out.write(handshakeMessage);
        out.flush();

    }

    public void sendMessage(byte[] message) throws IOException {

        // Creates byte array for message
        byte[] lengthPrefixedMessage = new byte[4 + message.length];

        // Copys 4 byte message length at beggining of message
        System.arraycopy(ByteBuffer.allocate(4).putInt(message.length).array(), 0, lengthPrefixedMessage, 0, 4);

        // Copys actual message payload after length prefix
        System.arraycopy(message, 0, lengthPrefixedMessage, 4, message.length);

        // Send message to output stream
        out.write(lengthPrefixedMessage);
        out.flush();

    }

    public byte[] receiveMessage() throws IOException {

        // Read the 4 byte message to find total legnth
        int length = in.readInt();

        // Create array to hold message and read it
        byte[] receivedMessage = new byte[length];
        in.readFully(receivedMessage);
        return receivedMessage;

    }
}