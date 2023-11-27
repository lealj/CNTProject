package peer;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.*;
import message.MessageType;
import message.MessageGenerator;

class MessageHandler implements Runnable{
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private PeerInfo peer;
    private boolean hasSentMessage;
    private Socket socket;
    
    public MessageHandler(ObjectInputStream inputStream, ObjectOutputStream outputStream, PeerInfo peer, Socket socket) {
        this.peer = peer;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.hasSentMessage = false;
        this.socket = socket;
        peer.initializeOutputStream(outputStream);
    }

    @Override
    public void run() {
        //System.out.println("Message handler running for peer.." + peer.getPeerID() +"..");
        
        //Send handshake before while loop

        // sample send message
        if (peer.getPeerID() == 1002) {
            peer.sendMessage("Hi, how are you");
        } else {
            peer.sendMessage("Good, you?");
        }
        
        while (true) {
            // exit while loop if this.peer has the file and all other neighbors do as well.
            try {
                System.out.println("HELLLOOOO");
                Object receivedMessage = inputStream.readObject();
                
                if (receivedMessage instanceof String) {
                    String msg = (String) receivedMessage;
                    interpretMessage(msg);
                } else {
                    // Handle other types of messages if needed
                }
            } catch (EOFException e) {
                System.out.println(" Peer: " + peer.getPeerID() + " Port: " + peer.getListeningPortNumber());
                System.err.println("Socket reached end of stream (EOF). " + "Peer: " + peer.getPeerID() + " Port: " + peer.getListeningPortNumber());
            } catch (SocketException e) {
                System.out.println(" Peer: " + peer.getPeerID() + " Port: " + peer.getListeningPortNumber());
                System.err.println("Socket closed. Continuing...");
                System.exit(0);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            } finally {
                try{ 
                    inputStream.close();
                    outputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public MessageHandler interpretMessage(String msg) {
        System.out.println("Peer " + peer.getPeerID() + " received message: " + msg); 

        byte[] msg_bytes = msg.getBytes();
        byte msgTypeValue = msg_bytes[4];
        MessageType messageType = MessageType.fromValue(msgTypeValue);

        switch (messageType) {
            case BITFIELD:
            case CHOKE:
                //return new Choke(sender, receiver);
            case HANDSHAKE:
            case INTERESTED:
            case PIECE:
            case REQUEST:
            case UNCHOKE:
            case UNINTERESTED:
        }
        return null;
    }
}
