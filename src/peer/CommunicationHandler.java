package peer;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

                Thread.sleep(50);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (EOFException e) {
                System.err.println("Socket reached end of stream (EOF). " + "Peer: " + peer.getPeerID() + " Port: "
                        + peer.getListeningPortNumber());
            } catch (SocketException e) {
                System.err.println("Socket closed. Continuing... " + peer.getPeerID());

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
            System.out.println("Handshaked with Peer " + otherPeerID);

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
                case INTERESTED:
                    System.out.println("Peer " + peer.getPeerID() + " received `Interest` from " + otherPeerID);
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
                    break;
                case UNINTERESTED:
                    // log uninterested - nothing else really specified ?
                    System.out.println("Peer " + peer.getPeerID() + " received `Uninterest` from " + otherPeerID);

                    /* For testing purposes, remove later: */
                    receivedUninterestedMessage();
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
        System.out.println("Peer " + peer.getPeerID() + " received `Request` from " + otherPeerID);

        // get index from msg
        int pieceIndex = messageInterpretor.getPieceIndex(msg);

        // get piece data
        byte[] filePiece = peer.transferPiece(pieceIndex);

        // send piece message
        byte[] pieceMessage = messageGenerator.createPieceMessage(pieceIndex, filePiece);

        sendMessage(pieceMessage);

        // probably remove in submission
        try {
            // introduces delay
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

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
        System.out.println("Peer " + peer.getPeerID() + " received `Piece` from " + otherPeerID);
        int pieceSize = peer.getPieceSize();
        if (peer.getNumbPieces() == 133) {
            pieceSize = peer.getLastPieceSize();
        }

        int pieceIndex = messageInterpretor.getPieceIndex(msg);
        byte[] piece = messageInterpretor.getPieceContent(pieceSize, msg);
        // printPieceInHex(pieceIndex, piece);

        // ensure we don't already have the piece
        peer.receivePiece(pieceIndex, piece);

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

    // copy of request message for sending entire file - remove from submission
    private void receivedRequestMessageSIMULATION(byte[] msg) {
        System.out.println("Peer " + peer.getPeerID() + " received `Request` from " + otherPeerID);

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

            try {
                // introduces delay
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
