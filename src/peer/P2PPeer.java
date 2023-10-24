package src.peer;

import java.io.*;
import java.net.*;
import java.util.*;

// Having a Peer-to-peer connection, currently testing with localhost with cmd
public class P2PPeer {

    public static void main(String[] args) {

        // Asks for input
        // Start with peer ID = name of the peer
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your peer ID: ");
        String peerId = scanner.nextLine();

        // Must be different port numbers
        System.out.print("Enter your port number to listen on: ");
        int peerPort = scanner.nextInt();

        // "Eats" the newline because of interactions with using int after string
        // newLine
        scanner.nextLine();

        // Threads for connections
        // If not using threads, connection ends up disconnecting
        new Thread(new Runnable() {
            public void run() {
                try {

                    // Takes input and listens to the connection on port number above
                    ServerSocket listener = new ServerSocket(peerPort);

                    while (true) {

                        // If succesfully connected to port number, sends message to peer that
                        // connection was successful
                        Socket peerSocket = listener.accept();
                        System.out.println("\nPeer IP " + peerSocket.getRemoteSocketAddress()
                                + " has successfully connected to you");
                        new Thread(new CommunicationHandler(peerSocket)).start();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Main thread for outgoing connections
        while (true) {

            // Takes in the IP address and peer's port number to try and connect
            // System.out.println("");
            System.out.println("\nWaiting for peer connections on port " + peerPort + "...\n");

            System.out.print("Enter the IP address of peer to connect: ");
            String peerIp = scanner.nextLine();
            // scanner.nextLine();

            System.out.print("\nEnter the port of peer to connect: ");
            int peerConnectPort = scanner.nextInt();
            scanner.nextLine();

            try {

                // Connecting to the peer's IP address / port number
                Socket socket = new Socket(peerIp, peerConnectPort);
                System.out.print("Successfully connected to peer at " + peerIp + " : " + peerConnectPort + "\n");

                // Starting a new thread to handle communication with this peer
                // This stops disruption between sending/receiving
                new Thread(new CommunicationHandler(socket)).start();

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                while (true) {

                    // Sending messages to the peer - either a string or a file
                    System.out.print("\nEnter a message or the path of the file to send: ");
                    String message = scanner.nextLine();

                    // Creates a file with value of the path
                    File isFile = new File(message);

                    // Checks if a file exists is not a directory
                    if (isFile.exists() && !isFile.isDirectory()) {

                        // Outputs to a output stream
                        out.writeUTF("FILE");
                        out.writeUTF(isFile.getName());
                        out.writeLong(isFile.length());

                        byte[] buffer = new byte[4096];

                        // Reads the isFile file
                        FileInputStream fis = new FileInputStream(isFile);
                        int bytesRead;

                        // Reads file content then write to output stream
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }

                        fis.close();
                        System.out.println("File sent successfully.");

                    }

                    else {

                        out.writeUTF("TEXT");
                        out.writeUTF(peerId + ": " + message);

                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class CommunicationHandler implements Runnable {
    private Socket socket;

    public CommunicationHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        // Creates a DataInputStream and closes when block ends
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            while (true) {

                // Reading a UTF-8 string from DataInputStream
                String type = in.readUTF();

                // Checks if peer sends a string. If so, display it
                if ("TEXT".equals(type)) {
                    String receivedMessage = in.readUTF();
                    System.out.println("\nReceived: " + receivedMessage);
                }

                // Checks if peer sends a file instead of string. If so, send file directory and
                // ask if they want to download
                else if ("FILE".equals(type)) {

                    // Reads uTF-8 encoded string from DataInputStream and the size of file sent to
                    // peer
                    String fileName = in.readUTF();
                    long fileSize = in.readLong();
                    File file = new File("received_" + fileName);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = 0;

                    // Ask if peer wants to save file
                    System.out.println("\nReceived a file: " + fileName + ". Do you want to save it? (yes/no): ");
                    Scanner scanner = new Scanner(System.in);
                    String response = scanner.nextLine();

                    // Checks if peer types yes or no. If yes, write file with new name and save it
                    if ("yes".equalsIgnoreCase(response)) {
                        FileOutputStream fos = new FileOutputStream(file);

                        while (totalBytesRead < fileSize) {
                            bytesRead = in.read(buffer);
                            totalBytesRead += bytesRead;
                            fos.write(buffer, 0, bytesRead);
                        }

                        fos.close();
                        System.out.println("\nFile saved as: " + file.getName());
                        System.out.println(new File(".").getAbsolutePath());

                    }

                    else {

                        // Consume the bytes for the file without saving them
                        while (totalBytesRead < fileSize) {
                            bytesRead = in.read(buffer);
                            totalBytesRead += bytesRead;
                        }

                        System.out.println("File was not saved.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
