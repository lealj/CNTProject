package src;

import src.peer.Server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import src.peer.Peer;

// start peer processes here

public class Main {
    private static HashMap<Integer, Peer> peerMap = new HashMap<>();

    public static void main(String arg[]) throws Exception {
        // Read peer configuration from Common.cfg
        Properties peerProperties = getPeerConfigInfo();

        int peerId = 1001;
        // Create a Peer object with the configuration settings from Common.cfg
        Peer peer = new Peer(
                peerId,
                Integer.parseInt(peerProperties.getProperty("NumberOfPreferredNeighbors")),
                Integer.parseInt(peerProperties.getProperty("UnchokingInterval")),
                Integer.parseInt(peerProperties.getProperty("OptimisticUnchokingInterval")),
                peerProperties.getProperty("FileName"),
                Integer.parseInt(peerProperties.getProperty("FileSize")),
                Integer.parseInt(peerProperties.getProperty("PieceSize")));

        peerMap.put(peerId, peer);
        Server server = new Server(peer);
        server.run();
    }

    private static Properties getPeerConfigInfo() {
        Properties properties = new Properties();

        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("src/Common.cfg")) {
            if (inputStream != null) {
                properties.load(inputStream);
                System.out.println("Loaded configuration from Common.cfg");
            } else {
                System.err.println("Common.cfg not found on the classpath.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    public static Peer getPeer(int peerId) {
        return peerMap.get(peerId);
    }

}
