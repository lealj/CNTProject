package peer;

import java.util.HashMap;
import java.util.Map;

public class PeerInfo {
    private int listeningPort;
    private int peerID;
    private String hostName;
    private boolean hasFile;
    private byte[] bitfield;

    private boolean hasNothing;

    private Map<Integer, byte[]> neighborBitfieldTracker;

    // Constructor that initializes values provided in parameter
    public PeerInfo(int peerID, String hostName, int listeningPort, boolean hasFile) {
        this.peerID = peerID;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFile = hasFile;
        this.bitfield = null;

        this.hasNothing = true;
        this.neighborBitfieldTracker = new HashMap<>();
        // this.outputStream = null;
    }

    public void initializeBitfield(int size) {
        this.bitfield = new byte[size];
        if (this.hasFile) {
            for (int i = 0; i < size; i++) {
                this.bitfield[i] = (byte) 0xFF; // Set all bits to 1
            }
        } else {
            for (int i = 0; i < size; i++) {
                this.bitfield[i] = (byte) 0x00; // Set all bits to 0
            }
        }
        System.out.println(bitfield.length);
    }

    public void setAllBitsToOne() {
        for (int i = 0; i < bitfield.length; i++) {
            bitfield[i] = (byte) 0xFF; // Set all bits to 1
        }
    }

    public void setAllBitsToZero() {
        for (int i = 0; i < bitfield.length; i++) {
            bitfield[i] = (byte) 0x00; // Set all bits to 0
        }
    }

    public byte[] getBitfield() {
        return bitfield;
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
    /*
     * public static List<PeerInfo> loadFromFile(String filename) throws IOException
     * {
     * 
     * List<PeerInfo> peers = new ArrayList<>();
     * List<String> lines = Files.readAllLines(Paths.get(filename));
     * 
     * // Goes through each line in the list, creating new PeerInfo object using
     * values
     * // from parts array and adds to peers list
     * for (String line : lines) {
     * String[] parts = line.split(" ");
     * peers.add(new PeerInfo(Integer.parseInt(parts[0]), parts[1],
     * Integer.parseInt(parts[2]),
     * parts[3].equals("1")));
     * }
     * 
     * return peers;
     * 
     * }
     */
}