package peer;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.util.Date;
import java.util.Iterator;

import message.MessageGenerator;
import message.MessageInterpretor;
import message.MessageType;

class CommunicationHandler implements Runnable {
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private PeerInfo peer;
    private int otherPeerID;

    private MessageGenerator messageGenerator;
    private MessageInterpretor messageInterpretor;

    // Choking interval in milliseconds
    private static final int P_INTERVAL = 25;

    // Optimistic unchoking interval in milliseconds (60 seconds)
    private static final int M_INTERVAL = 25;

    long lastChokingTime = System.currentTimeMillis();
    long lastOptimisticUnchokingTime = System.currentTimeMillis();

    private Set<Integer> chokedNeighbors = new HashSet<>();
    private Set<Integer> unchokedNeighbors = new HashSet<>();
    private int k = 3; // Number of preferred neighbors

    public CommunicationHandler(ObjectInputStream inputStream, ObjectOutputStream outputStream, PeerInfo peer,
            Socket socket) {
        this.peer = peer;
        this.otherPeerID = -1;

        this.inputStream = inputStream;
        this.outputStream = outputStream;

        this.messageGenerator = new MessageGenerator();
        this.messageInterpretor = new MessageInterpretor();
    }

    @Override
    public void run() {
        // System.out.println("Message handler running for peer.." + peer.getPeerID()
        // +"..");

        // Send handshake before while loop
        byte[] handshakeMessage = messageGenerator.createHandshakeMessage(peer.getPeerID());
        sendMessage(handshakeMessage);

        while (true) {
            // exit while loop if this.peer has the file and all other neighbors do as well.
            try {
                Object receivedMessage = inputStream.readObject();

                // Send queued interest/uninterest messages if exist
                sendQueuedInterestMessages();

                if (receivedMessage instanceof byte[]) {
                    byte[] msg = (byte[]) receivedMessage;
                    interpretMessage(msg);

                }

                // Implement choking logic every p seconds
                long currentTime = System.currentTimeMillis();

                if (peer.getPeerID() == 1001) {
                    if (currentTime - lastChokingTime >= P_INTERVAL) {
                        System.out.println("Executing choking logic. Current time: " + currentTime);
                        implementChokingLogic();
                        lastChokingTime = currentTime;
                    }
                }

                // Implement optimistic unchoking logic every m seconds
                if (currentTime - lastOptimisticUnchokingTime >= M_INTERVAL) {
                    implementOptimisticUnchokingLogic();
                    lastOptimisticUnchokingTime = currentTime;
                }

                // Thread.sleep(50);

                // } catch (InterruptedException e) {
                // Thread.currentThread().interrupt();
            } catch (EOFException e) {
                System.err.println("Socket reached end of stream (EOF). " + "Peer: " + peer.getPeerID() + " Port: "
                        + peer.getListeningPortNumber());
                break;
            } catch (SocketException e) {
                System.err.println("Socket closed. Continuing... " + peer.getPeerID());

                try {
                Date date = new Date();
                
                FileWriter log = new FileWriter("log_peer_" + peer.getPeerID() + ".log", true);
                log.write(
                    "[" + date + "]: " + "Peer " + peer.getPeerID() + " has downloaded the complete file.\n");
                log.close();
                }
                catch(Exception error) {
                    error.printStackTrace();
                }
        
                // COULD CAUSE ISSUES IN TESTING *****************************
                System.exit(0);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void sendMessage(byte[] message) {
        try {
            if (outputStream == null) {
                System.out.println("output stream null");
            }
            // System.out.println("Peer " + peer.getPeerID() + " attempts to send message");
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void interpretMessage(byte[] msg) {
        // System.out.println("Peer " + peer.getPeerID() + " received message: " + msg);

        if (msg.length == 32) {
            // dealing with handshake message - send a bitfield

            int neighborID = messageInterpretor.getIdFromHandshake(msg);
            peer.createBitfieldMapEntry(neighborID);
            otherPeerID = neighborID;
            //System.out.println("Handshaked with Peer " + otherPeerID);

            //Since handshake has already been completed, TCP connected, print for both client & server 
            try {
                Date date = new Date();

                FileWriter logClient = new FileWriter("log_peer_" + peer.getPeerID() + ".log", true);
                if(peer.getPeerID() > otherPeerID) {
                    logClient.write(
                        "[" + date + "]: " + "Peer " + peer.getPeerID() + " makes a connection to Peer " + otherPeerID + ".\n");
                }
                else {
                    logClient.write(
                    "[" + date + "]: " + "Peer " + peer.getPeerID() + " is connected from Peer " + otherPeerID + ".\n");
                }
                logClient.close();
            }
            catch(Exception error) {
                error.printStackTrace();
            }
            
            // skip if peer has nothing - comment out if statemnt to help w/ testing
            // if (!peer.hasNothing) {
            byte[] bitfield = peer.getBitfield();
            byte[] bitfieldMessage = messageGenerator.createBitfieldMessage(bitfield);
            sendMessage(bitfieldMessage);
            // }
            return;
        }

        byte msgTypeValue = msg[4];
        MessageType messageType = MessageType.fromValue(msgTypeValue);

        if (messageType != null) {
            switch (messageType) {
                case BITFIELD:
                    receivedBitfieldMessage(msg);
                    break;
                case CHOKE:
                    // implementChokingLogic();
                    try {
                        Date date = new Date();
                    
                        FileWriter log = new FileWriter("log_peer_" + otherPeerID + ".log", true);
                        log.write(
                            "[" + date + "]: " + "Peer " + otherPeerID + " is choked by " + peer.getPeerID() + ".\n");
                        log.close();
                    }
                    catch(Exception error) {
                        error.printStackTrace();
                    }
                    break;
                case INTERESTED:
                    //System.out.println("Peer " + peer.getPeerID() + " received `Interest` from " + otherPeerID);
                    try {
                        Date date = new Date();

                        FileWriter log = new FileWriter("log_peer_" + peer.getPeerID() + ".log", true);
                        log.write(
                            "[" + date + "]: " + "Peer " + peer.getPeerID() + " received the 'interested' message from " + otherPeerID + ".\n");
                        log.close();
                    }
                    catch(Exception error) {
                        error.printStackTrace();
                    }
                    // log interested - nothing else really specified ?
                    break;
                case PIECE:
                    // send have message - confirming with other peer that this peer "have piece".
                    receivedPieceMessage(msg);
                    // - send another request message
                    break;
                case REQUEST:
                    // send piece message (contains actual piece)
                    receivedRequestMessage(msg);
                    // receivedRequestMessageSIMULATION(msg);
                    break;
                case UNCHOKE:
                    // send REQUEST message for piece it does not have and has not request from
                    // other neighbors
                    // implementChokingLogic();
                    try {
                        Date date = new Date();
                    
                        FileWriter log = new FileWriter("log_peer_" + otherPeerID + ".log", true);
                        log.write(
                            "[" + date + "]: " + "Peer " + otherPeerID + " has been unchoked by " + peer.getPeerID() + ".\n");
                        log.close();
                    }
                    catch(Exception error) {
                        error.printStackTrace();
                    }
                    break;
                case UNINTERESTED:
                    // log uninterested - nothing else really specified ?
                    //System.out.println("Peer " + peer.getPeerID() + " received `Uninterest` from " + otherPeerID);
                    try {
                        Date date = new Date();

                        FileWriter log = new FileWriter("log_peer_" + peer.getPeerID() + ".log", true);
                        log.write(
                            "[" + date + "]: " + "Peer " + peer.getPeerID() + " received the 'not interested' message from " + otherPeerID + ".\n");
                        log.close();
                    }
                    catch(Exception error) {
                        error.printStackTrace();
                    }
                    /* For testing purposes, remove later: */
                    // receivedUninterestedMessage();
                    break;
                case HAVE:
                    receivedHaveMessage(msg);
                    break;
                default:
                    System.out.println("Default message type case");
                    break;
            }

        } else {
            System.out.println("Message type null");
            return;
        }
    }

    // Should not be in final submission - just use switch case for handling
    private void receivedUninterestedMessage() {
        /* TEST FUNCTION - NOT ACTUAL IMPLEMENTATION */

        if (peer.piecesReceived >= 1) {
            return;
        }
        // USED FOR SENDING ENTIRE FILE (TESTING)
        // System.out.println("Testing: sending request msg");
        // int pieceIndex = 0;
        // byte[] requestMessage = messageGenerator.createRequestMessage(pieceIndex);
        // sendMessage(requestMessage);
    }

    private void receivedRequestMessage(byte[] msg) {
        //System.out.println("Peer " + peer.getPeerID() + " received `Request` from " + otherPeerID);
        // get index from msg
        int pieceIndex = messageInterpretor.getPieceIndex(msg);
        
        try {
            Date date = new Date();

            FileWriter log = new FileWriter("log_peer_" + peer.getPeerID() + ".log", true);
            log.write(
                "[" + date + "]: " + "Peer " + peer.getPeerID() + " received the 'have' message from " + otherPeerID + " for the piece " + pieceIndex + ".\n");
            log.close();
        }
        catch(Exception error) {
            error.printStackTrace();
        }

        // get piece data
        byte[] filePiece = peer.transferPiece(pieceIndex);

        // send piece message
        byte[] pieceMessage = messageGenerator.createPieceMessage(pieceIndex, filePiece);

        sendMessage(pieceMessage);

        // probably remove in submission
        // try {
        // introduces delay
        // Thread.sleep(50);
        // } catch (InterruptedException e) {
        // Thread.currentThread().interrupt();
        // }

    }

    /*
     * Even though peer A sends a ‘request’ message to peer B, it may not receive a
     * ‘piece’
     * message corresponding to it. This situation happens when peer B re-determines
     * preferred neighbors or optimistically unchoked a neighbor and peer A is
     * choked as the
     * result before peer B responds to peer A. Your program should consider this
     * case.
     */

    private void receivedPieceMessage(byte[] msg) {
        // download the piece
        //System.out.println("Peer " + peer.getPeerID() + " received `Piece` from " + otherPeerID);
        int pieceSize = peer.getPieceSize();
        if (peer.getNumbPieces() == 133) {
            pieceSize = peer.getLastPieceSize();
        }

        int pieceIndex = messageInterpretor.getPieceIndex(msg);
        byte[] piece = messageInterpretor.getPieceContent(pieceSize, msg);
        // printPieceInHex(pieceIndex, piece);

        // ensure we don't already have the piece
        peer.receivePiece(pieceIndex, piece);

        try {
            Date date = new Date();

            FileWriter log = new FileWriter("log_peer_" + peer.getPeerID() + ".log", true);
            log.write(
                "[" + date + "]: " + "Peer " + peer.getPeerID() + " has downloaded the piece " + pieceIndex + " from " + otherPeerID + ". Now the number of pieces it has is " + peer.piecesReceived + ".\n");
            log.close();
        }
        catch(Exception error) {
            error.printStackTrace();
        }

        // send have message
        byte[] haveMessage = messageGenerator.createHaveMessage(pieceIndex);
        sendMessage(haveMessage);

        // check neighbor bitfields, populate interestMessageQueue w/ interest msgs
        populateInterestMessageQueue();

        // send another request message until peer has no more interesting pieces or
        // receive choke message
        byte[] neighborBitfield = peer.getNeighborBitfield(otherPeerID);
        List<Integer> pieceDifference = peer.getPiecesDifference(neighborBitfield);
        System.out.println(pieceDifference.size());
        if (pieceDifference.size() > 0) {
            // randomly select pieceIndex
            Random random = new Random();
            int randomIndex = random.nextInt(pieceDifference.size());
            int randomPieceIndex = pieceDifference.get(randomIndex);

            byte[] requestMessage = messageGenerator.createRequestMessage(randomPieceIndex);
            sendMessage(requestMessage);
        }

    }

    private void receivedBitfieldMessage(byte[] msg) {
        System.out.println("Peer " + peer.getPeerID() + " received `Bitfield` from " + otherPeerID);
        // store other bitfield
        byte[] neighborBitfield = messageInterpretor.getBitfieldFromMessage(msg);
        peer.storeNeighborBitfield(otherPeerID, neighborBitfield);
        // determine if neighbor has piece this peer doesn't have
        boolean neighborHasPieces = peer.neighborHasVitalPieces(otherPeerID);

        // if yes -> send interested message
        if (neighborHasPieces) {
            byte[] interestedMessage = messageGenerator.createInterestedMessage();
            sendMessage(interestedMessage);
        } else {
            // else -> not interested message
            byte[] uninterestedMessage = messageGenerator.createUninterestedMessage();
            sendMessage(uninterestedMessage);
        }
    }

    private void receivedHaveMessage(byte[] msg) {
        System.out.println("Peer " + peer.getPeerID() + " received `Have` from " + otherPeerID);
        // determine type of interest message (compare bitfields)
        boolean neighborHasPieces = peer.neighborHasVitalPieces(otherPeerID);
        byte[] messageToSend;
        if (neighborHasPieces) {
            messageToSend = messageGenerator.createInterestedMessage();
            System.out.println("Peer: " + peer.getPeerID() + " interested in Peer: " + otherPeerID);
            sendMessage(messageToSend);
        } else {
            messageToSend = messageGenerator.createUninterestedMessage();
            System.out.println("Peer: " + peer.getPeerID() + " uinterested in Peer: " + otherPeerID);
            sendMessage(messageToSend);
        }

        // get piece index field from message - Update other bitfield
        int pieceIndex = messageInterpretor.getPieceIndex(msg);

        try {
            Date date = new Date();

            FileWriter log = new FileWriter("log_peer_" + peer.getPeerID() + ".log", true);
            log.write(
                "[" + date + "]: " + "Peer " + peer.getPeerID() + " received the 'have' message from " + otherPeerID + " for the piece " + pieceIndex + ".\n");
            log.close();
        }
        catch(Exception error) {
            error.printStackTrace();
        }

        // get current neighbor bitfield
        byte[] neighborBitfield = peer.getNeighborBitfield(otherPeerID);
        // update it using the piece index
        if (neighborBitfield != null) {
            int byteIndex = pieceIndex / 8;
            int bitIndex = 7 - (pieceIndex % 8);
            neighborBitfield[byteIndex] |= (1 << bitIndex);
            peer.storeNeighborBitfield(otherPeerID, neighborBitfield);
        }
    }

    /* QUEUEING FUNCTIONS */

    private void sendQueuedInterestMessages() {
        Map<Integer, byte[]> interestMessageQueue = peer.getInterestMessageQueue();
        if (!interestMessageQueue.isEmpty()) {
            for (Integer key : interestMessageQueue.keySet()) {
                byte[] interestMessage = interestMessageQueue.get(key);
                if (interestMessage != null) {
                    System.out.println("Peer " + peer.getPeerID() + " sent queued msg to Peer " + otherPeerID);
                    sendMessage(interestMessage);
                    interestMessageQueue.put(key, null);
                }
            }
        }
    }

    private void populateInterestMessageQueue() {
        Map<Integer, byte[]> neighborBitfieldTracker = peer.getNeighborBitfieldTracker();
        Map<Integer, byte[]> interestMessageQueue = peer.getInterestMessageQueue();

        for (Integer key : neighborBitfieldTracker.keySet()) {
            if (key == otherPeerID) {
                continue;
            }
            if (peer.neighborHasVitalPieces(key) && interestMessageQueue.get(key) == null) {
                // queue interest message
                byte[] interestMessage = messageGenerator.createInterestedMessage();
                interestMessageQueue.put(key, interestMessage);

                System.out.println("Peer " + peer.getPeerID() + " queued interest msg for peer" + key);

            } else if (!peer.neighborHasVitalPieces(key)) {
                // queue uninterest message
                byte[] uninterestMessage = messageGenerator.createInterestedMessage();
                interestMessageQueue.put(key, uninterestMessage);

                System.out.println("Peer " + peer.getPeerID() + " queued uninterest msg for peer" + key);

            }
        }
    }

    /* TESTING FUNCTIONS */

    public void printPieceInHex(int i, byte[] piece) {
        // manipulate count to print specific values of piece
        System.out.print("Piece " + i + ": ");
        int count = 0;
        if (piece != null) {

            for (byte b : piece) {
                if (count < 5) {
                    System.out.print(String.format("%02X ", b));
                }
                count++;
            }

        } else {
            System.out.print("null");
        }

        System.out.println();
    }

    private void implementChokingLogic() {

        // long currentTime = System.currentTimeMillis();
        // System.out.println("Current time: " + currentTime);
        // System.out.println("Last choking time: " + lastChokingTime);

        if (peer.getPeerID() == 1001) {

            // Calculate downloading rates and select preferred neighbors
            Set<Integer> downloadingRates = calculateDownloadingRates();
            Set<Integer> preferredNeighbors = selectPreferredNeighbors(downloadingRates);

            // Print decides which friends to unchoke
            // System.out.println("Exectuting choking logic...");
            System.out.println("Peer " + peer.getPeerID() + " decides which friends to unchoke.");

            // Send unchoke messages to preferred neighbors
            sendUnchokeMessages(preferredNeighbors);

            // Send choke messages to all other neighbors
            sendChokeMessages(preferredNeighbors);

            // Update the set of unchoked neighbors
            updateUnchokedNeighbors(preferredNeighbors);

        }

    }

    private void implementOptimisticUnchokingLogic() {
        // Randomly select one choked neighbor

        if (peer.getPeerID() == 1001) {

        int optimisticUnchokedNeighbor = selectRandomChokedNeighbor();
        
        try {
            Date date = new Date();

            FileWriter log = new FileWriter("log_peer_" + peer.getPeerID() + ".log", true);
            log.write(
                "[" + date + "]: " + "Peer " + peer.getPeerID() + " has the optimistically unchoked neighbor " + optimisticUnchokedNeighbor + ".\n");
            log.close();
        }
        catch(Exception error) {
            error.printStackTrace();
        }
    
        // Send unchoke message to the optimistically unchoked neighbor
        sendUnchokeMessage(optimisticUnchokedNeighbor);
    
        // Update the set of unchoked neighbors
        Set<Integer> optimisticUnchokedSet = new HashSet<>();
        optimisticUnchokedSet.add(optimisticUnchokedNeighbor);
        updateUnchokedNeighbors(optimisticUnchokedSet);

        }
    }

    private Set<Integer> calculateDownloadingRates() {
        // Calculate downloading rates for all neighbors
        // Return a set of neighbors with their downloading rates

        Set<Integer> downloadingRates = new HashSet<>();
        downloadingRates.add(1002);
        downloadingRates.add(1003);
        downloadingRates.add(1004);
        downloadingRates.add(1005);

        // System.out.println("Downloading Rates: " + downloadingRates);

        return downloadingRates;
    }

    private Set<Integer> selectPreferredNeighbors(Set<Integer> downloadingRates) {
        // Select k preferred neighbors based on downloading rates
        // Return a set of selected preferred neighbors

        Set<Integer> allNeighbors = getAllNeighbors();

        // Sort neighbors based on downloading rates
        List<Integer> sortedNeighbors = sortNeighborsByDownloadingRates(downloadingRates);

        // Select top k neighbors
        Set<Integer> preferredNeighbors = new HashSet<>(
                sortedNeighbors.subList(0, Math.min(k, sortedNeighbors.size())));

        //System.out.println("Preferred Neighbors: " + preferredNeighbors);
        try {
            Date date = new Date();

            FileWriter log = new FileWriter("log_peer_" + peer.getPeerID() + ".log", true);
            log.write(
                "[" + date + "]: " + "Peer " + peer.getPeerID() + " has the preferred neighbors");

            Iterator<Integer> it = preferredNeighbors.iterator();
            while(it.hasNext()) {
                log.write(" " + it.next());
                if(it.hasNext()) {
                    log.write(",");
                }
            }
            log.write(".\n");
            log.close();
        }
        catch(Exception error) {
            error.printStackTrace();
        }

        return preferredNeighbors;
    }

    private List<Integer> sortNeighborsByDownloadingRates(Set<Integer> downloadingRates) {

        // Convert set to a list for sorting
        List<Integer> sortedNeighbors = new ArrayList<>(downloadingRates);

        // Sort neighbors based on downloading rates
        sortedNeighbors.sort(Comparator.reverseOrder());

        return sortedNeighbors;
    }

    private void sendUnchokeMessages(Set<Integer> preferredNeighbors) {

        // Send 'unchoke' messages to preferred neighbors

        for (int neighbor : preferredNeighbors) {
            byte[] unchokeMessage = messageGenerator.createUnchokeMessage();
            sendMessageToNeighbor(neighbor, unchokeMessage);

            // Print a message indicating the unchoke
            //System.out.println("Peer " + peer.getPeerID() + " unchokes " + neighbor);  
        }
    }
    

    private void sendChokeMessages(Set<Integer> preferredNeighbors) {

        // Send 'choke' messages to non-preferred neighbors
        Set<Integer> allNeighbors = getAllNeighbors();

        // Non-preferred neighbors
        allNeighbors.removeAll(preferredNeighbors);

        for (int neighbor : allNeighbors) {
            if (unchokedNeighbors.contains(neighbor)) {

                // Send 'choke' message to the neighbor
                byte[] chokeMessage = messageGenerator.createChokeMessage();
                sendMessageToNeighbor(neighbor, chokeMessage);

            // Print a message indicating the choke
            //System.out.println("Peer " + peer.getPeerID() + " choked " + neighbor);
            }
        }
    }

    private void sendUnchokeMessage(int neighbor) {
        // Send a single 'unchoke' message to a specific neighbor

        if (!unchokedNeighbors.contains(neighbor)) {
            // Send 'unchoke' message to the neighbor
            byte[] unchokeMessage = messageGenerator.createUnchokeMessage();
            sendMessageToNeighbor(neighbor, unchokeMessage);

            System.out.println("Peer " + peer.getPeerID() + " unchoked (optimistic) " + neighbor);
        }
    }

    private void updateUnchokedNeighbors(Set<Integer> newlyUnchokedNeighbors) {
        // Update the set of unchoked neighbors
        unchokedNeighbors.clear();
        unchokedNeighbors.addAll(newlyUnchokedNeighbors);
    }

    private int selectRandomChokedNeighbor() {

        Set<Integer> chokedNeighbors = getChokedNeighbors();
        if (!chokedNeighbors.isEmpty()) {
            List<Integer> chokedList = new ArrayList<>(chokedNeighbors);
            return chokedList.get(new Random().nextInt(chokedList.size()));
        }

        // Return -1 if no choked neighbors
        return -1;
    }

    private Set<Integer> getChokedNeighbors() {

        Set<Integer> allNeighbors = getAllNeighbors();

        // Choked neighbors
        allNeighbors.removeAll(unchokedNeighbors);
        return allNeighbors;
    }

    private Set<Integer> getAllNeighbors() {

        Set<Integer> allNeighbors = new HashSet<>();
        allNeighbors.add(1001);
        allNeighbors.add(1002);
        allNeighbors.add(1003);
        allNeighbors.add(1004);
        allNeighbors.add(1005);

        return allNeighbors;
    }

    private Set<Integer> unchokedPeers = new HashSet<>();

    private void sendMessageToNeighbor(int neighbor, byte[] message) {
        // Send a message to a specific neighbor
        if (neighbor != peer.getPeerID()) {
            // Check if the neighbor was not previously unchoked
            if (!unchokedPeers.contains(neighbor)) {
                // Check if the number of unchoked neighbors is less than k
                if (unchokedPeers.size() < k) {
                    System.out.println("Peer " + neighbor + " unchoked by Peer " + peer.getPeerID() + ", can download");
                    // Mark the neighbor as unchoked
                    unchokedPeers.add(neighbor);
                }
            }
        }

        // Send 'unchoked' message to the neighbor
        sendMessage(message);
    }

    // copy of request message for sending entire file - remove from submission
    private void receivedRequestMessageSIMULATION(byte[] msg) {
        //System.out.println("Peer " + peer.getPeerID() + " received `Request` from " + otherPeerID);
        // get index from msg
        int pieceIndex = messageInterpretor.getPieceIndex(msg);

        for (int i = 0; i < 133; i++) {
            // System.out.println(pieceIndex);
            // get piece data
            byte[] filePiece = peer.transferPiece(pieceIndex);
            // System.out.println("File Piece Length: " + filePiece.length);
            // printPieceInHex(i, filePiece);

            // send piece message
            byte[] pieceMessage = messageGenerator.createPieceMessage(pieceIndex, filePiece);

            sendMessage(pieceMessage);
            pieceIndex++;

            // try {
            // introduces delay
            // Thread.sleep(50);
            // } catch (InterruptedException e) {
            // Thread.currentThread().interrupt();
            // }
        }
    }
}
