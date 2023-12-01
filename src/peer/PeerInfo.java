package peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    // trackers
    private Map<Integer, byte[]> neighborBitfieldTracker;

    // test
    private int piecesReceived;

    // Constructor that initializes values provided in parameter
    public PeerInfo(int peerID, String hostName, int listeningPort, boolean hasFile) {
        this.peerID = peerID;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFile = hasFile;
        this.bitfield = null;
        this.filePath = "peerFiles/" + Integer.toString(this.peerID);
        this.hasNothing = hasFile ? true : false;

        this.neighborBitfieldTracker = new HashMap<>();
        // this.outputStream = null;

        // test
        this.piecesReceived = 0;
    }

    public void SetCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
        this.lastPieceSize = commonConfig.getFileSize() % commonConfig.getPieceSize();
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
        try (FileOutputStream fileOutputStream = new FileOutputStream(this.filePath + "/asdfFile")) {
            fileOutputStream.write(assembledFile);
        }
    }

    public void loadFile(int numbPieces) {
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
            filePieces[pieceIndex] = piece;
            updateBitfield(pieceIndex);
            piecesReceived++;
        }
        if (pieceIndex == 132) {
            try {
                assembleFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // printFilePiecesInHex();
        }
    }

    /* Bitfield management functions */

    public void createBitfieldMapEntry(int neighborID) {
        if (!neighborBitfieldTracker.containsKey(neighborID)) {
            neighborBitfieldTracker.put(neighborID, null);
        }
    }

    public void storeNeighborBitfield(int neighborID, byte[] neighborBitfield) {
        neighborBitfieldTracker.put(neighborID, neighborBitfield);
    }

    public byte[] getNeighborBitfield(int neighborID) {
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
        for (int i = 0; i < bitfieldSize; i++) {
            if (this.hasFile) {
                this.bitfield[i] = (byte) 0xFF;
            } else {
                this.bitfield[i] = (byte) 0x00;
            }
        }
    }

    private void updateBitfield(int pieceIndex) {
        int byteIndex = pieceIndex / 8;
        int bitIndex = pieceIndex % 8;
        this.bitfield[byteIndex] |= (1 << (7 - bitIndex));
        if (this.piecesReceived == 132) {
            printBitfield(bitfield);
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

    // testing
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