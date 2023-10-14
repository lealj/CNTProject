package src;

import src.peer.Server;
import src.peer.Peer;

// start peer processes here

public class Main {
    public static void main(String arg[]) throws Exception {
        // handle peer config file
        Peer peer = new Peer(1001);
        Server server = new Server(peer);
        server.run();
    }
}
