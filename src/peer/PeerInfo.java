package src.peer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PeerInfo {

    private int listeningPort;
    private int peerID;
    private String hostName;
    private boolean hasFile;

    // Constructor that initializes values provided in parameter
    public PeerInfo(int peerID, String hostName, int listeningPort, boolean hasFile) {

        this.peerID = peerID;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFile = hasFile;

    }

    // Getters that return the value of the constructor above
    public int getListeningPortNumber() {
        return listeningPort;
    }

    public int getPeerID() {
        return peerID;
    }

    public boolean getHasFile() {
        return hasFile;
    }

    public String getHostName() {
        return hostName;
    }

    // Reads data from a file, having each line be information about a peer and
    // constructs list of PeerInfo objects from that data.
    public static List<PeerInfo> loadFromFile(String filename) throws IOException {

        List<PeerInfo> peers = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filename));

        // Goes through each line in the list, creating new PeerInfo object using values
        // from parts array and adds to peers list
        for (String line : lines) {
            String[] parts = line.split(" ");
            peers.add(new PeerInfo(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]),
                    parts[3].equals("1")));
        }

        return peers;

    }
}