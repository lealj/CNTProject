package src.peer;

import java.io.InputStream;

import java.util.*;

public class Peer extends Thread {
	private int peerId;
	private List<Integer> peers;
	private Set<Integer> pieces;
	public int NumberOfPreferredNeighbors;
	public int UnchokingInterval;
	public int OptimisticUnchokingInterval;
	public String FileName;
	public int FileSize;
	public int PieceSize;

	public Peer(int peerId, int NumberOfPreferredNeighbors, int UnchokingInterval, int OptimisticUnchokingInterval,
			String FileName, int FileSize, int PieceSize) {
		this.peerId = peerId;
		peers = new ArrayList<>();
		this.NumberOfPreferredNeighbors = NumberOfPreferredNeighbors;
		this.UnchokingInterval = UnchokingInterval;
		this.OptimisticUnchokingInterval = OptimisticUnchokingInterval;
		this.FileName = FileName;
		this.FileSize = FileSize;
		this.PieceSize = PieceSize;

		// peerList(?), isFileOwner(?), other initialization variables that are passed
		// in to this constructor
		// this.pieces = new HashSet<>(); ?
	}

	public void run() {
		// handle p2p interactions here (if this message received, do this etc. (?))
		// start server
	}
}
