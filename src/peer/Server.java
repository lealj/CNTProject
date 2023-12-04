package peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private PeerInfo peer;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public Server(PeerInfo peer) {
        this.peer = peer;
        System.out.println("Peer " + peer.getPeerID() + " server created.");
    }

    @Override
    public void run() {
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(peer.getListeningPortNumber());
        } catch (IOException e) {
            System.err.println("Problem starting server");
            e.printStackTrace();
        }
        try {
            while (true) {
                socket = listener.accept();

                outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.flush();

                inputStream = new ObjectInputStream(socket.getInputStream());

                CommunicationHandler handler = new CommunicationHandler(inputStream, outputStream, peer, socket);
                System.out.println("(Server) Message handler created for Peer " + peer.getPeerID());
                // start handler on thread
                Thread serverThread = new Thread(handler);
                serverThread.start();
            }

        } catch (IOException err) {
            err.printStackTrace();
        } finally {
            try {
                listener.close();
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

    }

}
