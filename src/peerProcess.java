import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import peer.Client;
import peer.CommonConfig;
import peer.PeerInfo;
import peer.Server;

/*
 * To run: 
 * javac -g peerProcess.java
 * javac -g peer/*.java 
 * java peerProcess 1001 , java peerProcess 1002, ...
 */

public class peerProcess {
    public static void main(String args[]) throws FileNotFoundException {
        int currentPeerID = Integer.parseInt(args[0]);

        // reads peer config file and creates the peers
        List<PeerInfo> peers = readPeerConfig("PeerInfoTest.cfg");

        // reads common config file and creates a CommonConfig object (shared by all
        // peers)
        CommonConfig commonConfig = readCommonConfig("Common.cfg");

        // Bitfield data
        int fileSize = commonConfig.getFileSize();
        int numPieces = (int) Math.ceil((double) fileSize / commonConfig.getPieceSize());

        PeerInfo currentPeer = null;
        for (PeerInfo peer : peers) {
            peer.updateBitfieldSize(numPieces);
            if (peer.getPeerID() == currentPeerID) {
                currentPeer = peer;
            }
        }

        createServer(currentPeer);
        createClients(currentPeer, peers);
    }

    // handle "outgoing connections" for each peer (client).
    private static void createServer(PeerInfo peer) {
        Server server = new Server(peer);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    private static void createClients(PeerInfo currentPeer, List<PeerInfo> peers) {
        int currentPeerID = currentPeer.getPeerID();
        for (int i = 0; i < peers.size(); i++) {
            int otherPeerID = peers.get(i).getPeerID();
            if (otherPeerID != currentPeerID && otherPeerID < currentPeerID) {
                Client client = new Client(currentPeer, peers.get(i));
                client.Connect();
            }
        }
    }

    private static List<PeerInfo> readPeerConfig(String filePath) throws FileNotFoundException {
        List<PeerInfo> peers = new ArrayList<>();

        File configFile = new File(filePath);
        Scanner scanner = new Scanner(configFile);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(" ");

            int peerID = Integer.parseInt(parts[0]);
            String peerAddress = parts[1];
            int peerPort = Integer.parseInt(parts[2]);
            boolean hasFile = Integer.parseInt(parts[3]) == 1;

            PeerInfo peer = new PeerInfo(peerID, peerAddress, peerPort, hasFile);
            peers.add(peer);
        }
        scanner.close();

        return peers;
    }

    private static CommonConfig readCommonConfig(String filePath) throws FileNotFoundException {
        File commonFile = new File(filePath);
        Scanner scanner = new Scanner(commonFile);

        int numPreferredNeighbors = 0;
        int unchokingInterval = 0;
        int optimisticUnchokingInterval = 0;
        String fileName = "";
        int fileSize = 0;
        int pieceSize = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(" ");
            switch (parts[0]) {
                case "NumberOfPreferredNeighbors":
                    numPreferredNeighbors = Integer.parseInt(parts[1]);
                    break;
                case "UnchokingInterval":
                    unchokingInterval = Integer.parseInt(parts[1]);
                    break;
                case "OptimisticUnchokingInterval":
                    optimisticUnchokingInterval = Integer.parseInt(parts[1]);
                    break;
                case "FileName":
                    fileName = parts[1];
                    break;
                case "FileSize":
                    fileSize = Integer.parseInt(parts[1]);
                    break;
                case "PieceSize":
                    pieceSize = Integer.parseInt(parts[1]);
                    break;
                default:
                    break;
            }
        }
        scanner.close();
        return new CommonConfig(numPreferredNeighbors, unchokingInterval, optimisticUnchokingInterval, fileName,
                fileSize, pieceSize);
    }
}