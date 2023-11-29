package peer;

import java.util.HashMap;
import java.util.Map;

public class PeerInfo {
    private int listeningPort;
    private int peerID;
    private String hostName;
    private boolean hasFile;
    private byte[] bitfield;

    public boolean hasNothing;

    private Map<Integer, byte[]> neighborBitfieldTracker;

    // Constructor that initializes values provided in parameter
    public PeerInfo(int peerID, String hostName, int listeningPort, boolean hasFile) {
        this.peerID = peerID;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFile = hasFile;
        this.bitfield = null;

        this.hasNothing = hasFile ? true : false;
        this.neighborBitfieldTracker = new HashMap<>();
        // this.outputStream = null;
    }

    public void createBitfieldMapEntry(int neighborID) {
        if (!neighborBitfieldTracker.containsKey(neighborID)) {
            neighborBitfieldTracker.put(neighborID, null);
        }
    }

    public void storeNeighborBitfield(byte[] msg) {
        // get bitfield from msg
        // store in neighborBitfieldTracker
        // determine if they have pieces we don't have
    }

    public boolean neighborHasVitalPieces(byte[] otherPeerBitfield) {
        return false;
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

    /* GETTERS */

    public byte[] getBitfield() {
        return bitfield;
    }

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
}