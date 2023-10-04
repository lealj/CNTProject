package src;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Peers {
	private int peer_id;
	private Socket socket;
	private Set<Integer> pieces;

	public void Peers(int peer_id) {
		this.peer_id = peer_id;
		this.pieces = new HashSet<>();
	}
}
