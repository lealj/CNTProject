package peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerInfo {
    private int listeningPort;
    private int peerID;
    private String hostName;
    private boolean hasFile;
    private byte[] bitfield;
    public boolean hasNothing;

    // specifically file related
    private String filePath;
    private CommonConfig commonConfig;
    private byte[][] filePieces;
    private int lastPieceSize;
    private int numbPieces;

    // trackers
    private Map<Integer, byte[]> neighborBitfieldTracker;
    private Map<Integer, byte[]> interestMessageQueue; // id, interest/uninterest msg
    private Map<Integer, ObjectOutputStream> neighborOutputStreams;
    private ArrayList<Integer> handshakedNeighborIds;

    private Map<Integer, ArrayList<Long>> neighborDownloadSpeeds;
    private Map<Integer, ArrayList<Long>> sentReceivedPieceTracker;
    private Map<Integer, Boolean> hasFileTracker;
    // test
    public int piecesReceived;

    // Constructor that initializes values provided in parameter
    public PeerInfo(int peerID, String hostName, int listeningPort, boolean hasFile) {
        this.peerID = peerID;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFile = hasFile;
        this.bitfield = null;
        this.filePath = "peer_" + Integer.toString(this.peerID);
        this.numbPieces = -1;

        if (this.hasFile) {
            this.hasNothing = false;
        } else {
            this.hasNothing = true;
        }

        this.neighborBitfieldTracker = new HashMap<>();
        this.interestMessageQueue = new HashMap<>();
        this.neighborOutputStreams = new HashMap<>();
        this.handshakedNeighborIds = new ArrayList<>();

        this.neighborDownloadSpeeds = new HashMap<>();
        this.sentReceivedPieceTracker = new HashMap<>();
        this.hasFileTracker = new HashMap<>();
        // this.outputStream = null;

        // test
        this.piecesReceived = 0;
    }

    public void sendMessage(int neighborID, byte[] msg) {
        ObjectOutputStream outputStream = neighborOutputStreams.get(neighborID);
        try {
            if (outputStream == null) {
                System.out.println("output stream null");
                return;
            }
            // System.out.println("Peer " + peer.getPeerID() + " attempts to send message");
            outputStream.flush(); // comment out
            outputStream.writeObject(msg);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SetCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
        this.lastPieceSize = commonConfig.getFileSize() % commonConfig.getPieceSize();
    }

    public void addNeighborOutputStream(int neighborID, ObjectOutputStream outputStream) {
        System.out.println("Peer " + peerID + " sets outputStream for Peer " + neighborID);
        neighborOutputStreams.put(neighborID, outputStream);
    }

    public void assembleFile() throws FileNotFoundException, IOException {
        byte[] assembledFile = new byte[commonConfig.getFileSize()];

        int offset = 0;
        for (int i = 0; i < filePieces.length; i++) {
            byte[] piece = filePieces[i];
            System.arraycopy(piece, 0, assembledFile, offset, piece.length);
            offset += piece.length;
        }

        // add /thefile to filepath instead of asdfFile in submission
        try (FileOutputStream fileOutputStream = new FileOutputStream(
                this.filePath + "/" + commonConfig.getFileName())) {
            fileOutputStream.write(assembledFile);
        }
    }

    public void loadFile(int numbPieces) {
        this.numbPieces = numbPieces;
        filePieces = new byte[numbPieces][];

        if (!hasFile) {
            return;
        }

        File file = new File(filePath + "/" + commonConfig.getFileName());

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            int pieceIndex = 0;
            int bytesRead;
            byte[] buffer = new byte[commonConfig.getPieceSize()];

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                filePieces[pieceIndex] = new byte[bytesRead];
                System.arraycopy(buffer, 0, filePieces[pieceIndex], 0, bytesRead);
                pieceIndex++;
            }

        } catch (FileNotFoundException fnf) {
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // printFilePiecesInHex();
    }

    public byte[] transferPiece(int pieceIndex) {
        if (pieceIndex >= 0 && pieceIndex < filePieces.length) {
            return filePieces[pieceIndex];
        }
        return null;
    }

    public void receivePiece(int pieceIndex, byte[] piece) {
        if (pieceIndex >= 0 && pieceIndex < filePieces.length && piece != null) {
            hasNothing = false;
            filePieces[pieceIndex] = piece;
            updateBitfield(pieceIndex);
            this.piecesReceived++;
        }

        // check if we have all pieces
        int count = 0;
        for (byte b : this.bitfield) {
            for (int i = 0; i < 8; i++) {
                if ((b & (1 << i)) != 0) {
                    count++;
                }
            }
        }

        if (count == numbPieces) {
            try {
                assembleFile();
                this.hasFile = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            // printFilePiecesInHex();
        }
    }

    /* Bitfield management functions */

    public void createBitfieldMapEntry(int neighborID) {
        // if
        if (!neighborBitfieldTracker.containsKey(neighborID)) {
            neighborBitfieldTracker.put(neighborID, null);
        }
    }

    public void storeNeighborBitfield(int neighborID, byte[] neighborBitfield) {
        neighborBitfieldTracker.put(neighborID, neighborBitfield);
        if (hasFile) {
            if (Arrays.equals(neighborBitfield, this.bitfield)) {
                hasFileTracker.put(neighborID, true);
            }
        }
    }

    public boolean allNeighborsHaveFile() {
        if (hasFileTracker.keySet().size() == 9) {
            for (Integer key : hasFileTracker.keySet()) {
                if (hasFileTracker.get(key) == false) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public byte[] getNeighborBitfield(int neighborID) {
        byte[] neighborBitfield = neighborBitfieldTracker.get(neighborID);

        if (neighborBitfield == null) {
            int bitfieldSize = (int) Math.ceil((double) numbPieces / 8);
            neighborBitfield = new byte[bitfieldSize];

            for (int i = 0; i < bitfieldSize - 1; i++) {
                if (this.hasFile) {
                    this.bitfield[i] = (byte) 0xFF;
                } else {
                    this.bitfield[i] = (byte) 0x00;
                }
            }

            neighborBitfield[bitfieldSize - 1] = 0x00;
            neighborBitfieldTracker.put(neighborID, neighborBitfield);
        }

        return neighborBitfieldTracker.get(neighborID);
    }

    public boolean neighborHasVitalPieces(int neighborID) {
        byte[] neighborBitfield = neighborBitfieldTracker.get(neighborID);

        /* SUSPECTED FUTURE ISSUE - handling 3 cases with binary variable (bool) */
        if (neighborBitfield == null) {
            return false;
        }
        // determine if they have pieces we don't have
        if (getPiecesDifference(neighborBitfield).size() > 0) {
            return true;
        }
        return false;
    }

    public List<Integer> getPiecesDifference(byte[] neighborBitfield) {
        List<Integer> missingPieces = new ArrayList<>();
        int numbPieces = neighborBitfield.length * 8;
        // System.out.println("Length: " + neighborBitfield.length + "Numb pieces: " +
        // numbPieces);
        for (int i = 0; i < numbPieces; i++) {
            int peerIndex = i / 8;
            int neighborIndex = i / 8;
            int peerBitIndex = 7 - (i % 8);
            int neighborBitIndex = 7 - (i % 8);

            int peerBit = (this.bitfield[peerIndex] >> peerBitIndex) & 1;
            int neighborBit = (neighborBitfield[neighborIndex] >> neighborBitIndex) & 1;

            if (neighborBit == 1 && peerBit == 0) {
                // System.out.print(i + " ");
                missingPieces.add(i);
            }
        }
        return missingPieces;
    }

    public void initializeBitfield(int numbPieces) {
        int bitfieldSize = (int) Math.ceil((double) numbPieces / 8);
        this.bitfield = new byte[bitfieldSize];

        for (int i = 0; i < bitfieldSize - 1; i++) {
            if (this.hasFile) {
                this.bitfield[i] = (byte) 0xFF;
            } else {
                this.bitfield[i] = (byte) 0x00;
            }
        }

        // set bits in last byte of bitfield individually
        int lastByteBits = numbPieces % 8;
        byte lastByteMask = (byte) ((1 << lastByteBits) - 1);
        lastByteMask <<= (8 - lastByteBits);

        if (this.hasFile) {
            this.bitfield[bitfieldSize - 1] = lastByteMask;
        } else {
            this.bitfield[bitfieldSize - 1] = 0x00;
        }

        // printBitfield(this.bitfield);
    }

    private void updateBitfield(int pieceIndex) {
        int byteIndex = pieceIndex / 8;
        int bitIndex = pieceIndex % 8;

        if ((this.bitfield[byteIndex] >> (7 - bitIndex) & 1) == 1) {
            System.out.println("Already have piece " + pieceIndex);
        }

        this.bitfield[byteIndex] |= (1 << (7 - bitIndex));
        // System.out.println("Pieces Received: " + this.piecesReceived);
        if (this.piecesReceived == 132) {
            printBitfield(bitfield);
        }
    }

    /* GETTERS */
    public void addPeerHandshakeEntry(int neighborID) {
        // actually updates most trackers
        if (!handshakedNeighborIds.contains(neighborID)) {
            handshakedNeighborIds.add(neighborID);
        }

        if (!sentReceivedPieceTracker.containsKey(neighborID)) {
            ArrayList<Long> defaultTimesEntry = new ArrayList<>(Arrays.asList(0L, 0L));
            sentReceivedPieceTracker.put(neighborID, defaultTimesEntry);
        }

        if (!neighborDownloadSpeeds.containsKey(neighborID)) {
            ArrayList<Long> defaultTimeDifferences = new ArrayList<>();
            neighborDownloadSpeeds.put(neighborID, defaultTimeDifferences);
        }

        if (!hasFileTracker.containsKey(neighborID)) {
            hasFileTracker.put(neighborID, false);
        }
    }

    public ArrayList<Integer> getHandshakedPeers() {
        return handshakedNeighborIds;
    }

    public Map<Integer, byte[]> getNeighborBitfieldTracker() {
        return neighborBitfieldTracker;
    }

    public Map<Integer, byte[]> getInterestMessageQueue() {
        return interestMessageQueue;
    }

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

    public int getPieceSize() {
        return commonConfig.getPieceSize();
    }

    public int getNumbPieces() {
        int fileSize = commonConfig.getFileSize();
        int numbPieces = (int) Math.ceil((double) fileSize / commonConfig.getPieceSize());
        return numbPieces;
    }

    public int getLastPieceSize() {
        return lastPieceSize;
    }

    public Map<Integer, ArrayList<Long>> getNeighborDownloadSpeeds() {
        return neighborDownloadSpeeds;
    }

    public boolean neighborHasFile(int neighborID) {
        return hasFileTracker.get(neighborID);
    }

    /* Sent received functions */

    public void addSentEntry(int neighborID, long sentTime) {
        // System.out.println("Adding sent entry");
        ArrayList<Long> sentReceive = sentReceivedPieceTracker.get(neighborID);
        if (sentReceive != null) {
            if (sentReceive.get(0) == 0L) {
                sentReceive.set(0, sentTime);
            }
        }

    }

    public void addReceivedEntry(int neighborID, long receivedTime) {
        // System.out.println("Adding recv entry");

        ArrayList<Long> sentReceive = sentReceivedPieceTracker.get(neighborID);
        if (sentReceive != null) {
            // check that rcv time is 0, and sent time is populated
            if (sentReceive.get(1) == 0L && sentReceive.get(0) != 0L) {
                sentReceive.set(1, receivedTime);

                determineDownloadSpeed(neighborID);
            } else {
                // System.out.println("ISSUE");
            }
        }

    }

    private void determineDownloadSpeed(int neighborID) {
        ArrayList<Long> sentReceived = sentReceivedPieceTracker.get(neighborID);
        if (sentReceived == null) {
            System.out.println("Sent rcv null");
            return;
        }

        // record difference in time sent/received
        long timeSent = sentReceived.get(0);
        long timeReceived = sentReceived.get(1);

        long timeDifference = timeReceived - timeSent;
        // System.out.println("Time difference for Peer " + neighborID + ": " +
        // timeDifference);

        ArrayList<Long> timeDifferences = neighborDownloadSpeeds.get(neighborID);
        // System.out.println("Peer " + peerID + " : " + neighborID);
        timeDifferences.add(timeDifference);

        // update sent received tracker
        sentReceived.set(0, 0L);
        sentReceived.set(1, 0L);
    }

    // reset when unchoked, begin new tracking interval
    public void resetDownloadSpeeds(int neighborID) {
        ArrayList<Long> timeDifferences = neighborDownloadSpeeds.get(neighborID);
        if (timeDifferences != null) {
            timeDifferences.clear();
        } else {
            System.out.println("Time diffs null");
        }
    }

    /* TESTING FUNCTIONS */
    public void printBitfield(byte[] bitfield) {
        System.out.print("Bitfield: ");
        for (byte b : bitfield) {
            for (int i = 7; i >= 0; i--) {
                int bit = (b >> i) & 1;
                System.out.print(bit);
            }
            System.out.print(" ");
        }
        System.out.println();
    }

    public void printFilePiecesInHex() {
        for (int i = 0; i < 1; i++) {
            System.out.print("Piece " + i + ": ");

            if (filePieces[i] != null) {
                for (byte b : filePieces[i]) {
                    System.out.print(String.format("%02X ", b));
                }
            } else {
                System.out.print("null");
            }

            System.out.println();
        }
    }
}