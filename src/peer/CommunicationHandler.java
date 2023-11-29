package peer;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import message.MessageGenerator;
import message.MessageInterpretor;
import message.MessageType;

class CommunicationHandler implements Runnable {
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private PeerInfo peer;
    private int otherPeerID;
    private boolean hasSentMessage;
    private Socket socket;
    private MessageGenerator messageGenerator;
    private MessageInterpretor messageInterpretor;

    public CommunicationHandler(ObjectInputStream inputStream, ObjectOutputStream outputStream, PeerInfo peer,
            Socket socket) {
        this.peer = peer;
        this.otherPeerID = -1;

        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.hasSentMessage = false;
        this.socket = socket;

        this.messageGenerator = new MessageGenerator();
        this.messageInterpretor = new MessageInterpretor();
    }

    @Override
    public void run() {
        // System.out.println("Message handler running for peer.." + peer.getPeerID()
        // +"..");

        // Send handshake before while loop
        byte[] handshakeMessage = messageGenerator.handshakeMessage(peer.getPeerID());
        sendMessage(handshakeMessage);

        while (true) {
            // exit while loop if this.peer has the file and all other neighbors do as well.
            try {
                Object receivedMessage = inputStream.readObject();

                if (receivedMessage instanceof byte[]) {
                    byte[] msg = (byte[]) receivedMessage;
                    interpretMessage(msg);
                }

            } catch (EOFException e) {
                System.err.println("Socket reached end of stream (EOF). " + "Peer: " + peer.getPeerID() + " Port: "
                        + peer.getListeningPortNumber());
            } catch (SocketException e) {
                System.err.println("Socket closed. Continuing... " + peer.getPeerID());

                // COULD CAUSE ISSUES IN TESTING *****************************
                System.exit(0);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void sendMessage(byte[] message) {
        try {
            if (outputStream == null) {
                System.out.println("output stream null");
            }
            // System.out.println("Peer " + peer.getPeerID() + " attempts to send message");
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xff & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public void interpretMessage(byte[] msg) {
        // System.out.println("Peer " + peer.getPeerID() + " received message: " + msg);

        if (msg.length == 32) {
            // dealing with handshake message - send a bitfield
            System.out.println("Handshaked");
            int neighborID = messageInterpretor.getIdFromHandshake(msg);
            peer.createBitfieldMapEntry(neighborID);
            otherPeerID = neighborID;

            // this peers bitfield
            if (!peer.hasNothing) {
                byte[] bitfield = peer.getBitfield();
                byte[] bitfieldMessage = messageGenerator.createBitfieldMessage(bitfield);
                sendMessage(bitfieldMessage);
            }

            return;
        }

        byte msgTypeValue = msg[4];
        MessageType messageType = MessageType.fromValue(msgTypeValue);

        if (messageType != null) {
            switch (messageType) {
                case BITFIELD:
                    System.out.println("Bitfield received");
                    // store other peers bitfield
                    peer.storeNeighborBitfield(msg);
                    // determine if they have pieces current peer does not have
                    // if yes -> send interested message
                    // else -> not interested message
                    break;
                case CHOKE:
                case INTERESTED:
                case PIECE:
                case REQUEST:
                case UNCHOKE:
                case UNINTERESTED:
                default:
                    System.out.println("Default message type case");
                    break;
            }
        } else {
            System.out.println("Message type null");
            return;
        }

    }
}
