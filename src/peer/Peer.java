package src.peer;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Peer extends Thread {
	private int peerId;
	private List<Integer> peers;
	private Set<Integer> pieces;

	public Peer(int peerId) {
		this.peerId = peerId;
		peers = new ArrayList<>();
		// read config file here
		// peerList(?), isFileOwner(?), other initialization variables that are passed
		// in to this constructor
		// this.pieces = new HashSet<>(); ?
	}

	public void run() {
		// handle p2p interactions here (if this message received, do this etc. (?))
		// start server
	}
}
