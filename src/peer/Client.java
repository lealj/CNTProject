package peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private PeerInfo peer;
    private PeerInfo otherPeer;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public Client(PeerInfo peer, PeerInfo otherPeer) {
        this.peer = peer;
        this.otherPeer = otherPeer;
        System.out.println("Client created for Peer " + peer.getPeerID() + " to Peer " + otherPeer.getPeerID());
    }

    public void Connect() {
        Socket socket = null;
        try {
            socket = new Socket(otherPeer.getHostName(), otherPeer.getListeningPortNumber());
            
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();

            inputStream = new ObjectInputStream(socket.getInputStream());

            MessageHandler messageHandler = new MessageHandler(inputStream, outputStream, peer, socket);

            System.out.println("(Client) Peer " + peer.getPeerID() + " attempts to connect to Peer " + otherPeer.getPeerID());

            Thread messageHandlerThread = new Thread(messageHandler);
            messageHandlerThread.start();
        } catch (IOException err) {

            err.printStackTrace();

        } 
    }
}
