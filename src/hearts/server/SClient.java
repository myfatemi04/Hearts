package hearts.server;

import static hearts.Constants.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import hearts.Deck;
import hearts.Packet;

public class SClient {
	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	
	Server parentServer;
	CopyOnWriteArrayList<Packet> packets = new CopyOnWriteArrayList<Packet>();
	
	Thread packetReceiver = null;
	Thread packetHandler = null;
	SPacketStackHandler outpacks = null;
	
	boolean alive = true;
	String name = null;
	long id;
	byte playcard = -1;
	Deck currentCards;
	int points = 0;
	
	public void dispose() {
		parentServer.clients.remove(this);
		this.alive = false;
		parentServer.broadcast(pc_datapayload, new byte[] {pc_playerdc}, Packet.longToBytes(id));
		try {
			this.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean ready() {
		boolean hasName = name != null;
		return hasName;
	}
	
	public byte getCardindex() { // asks the client for which card they play. if they don't respond in time, they are autoplayed.
		playcard = -1;
		outpacks.send(pc_datarequest, new byte[] {pc_myplaycard});
		// request which card it is
		int elapsed = 0;
		long until = System.currentTimeMillis() + 15000; // gives user 15s to play
		while (playcard < 0) {
			try {
				Thread.sleep(100);
				elapsed += 100;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (elapsed < 15000 && elapsed > 10000 && elapsed % 1000 == 0) {
				 outpacks.sendData(pc_turnwarning, Packet.longToBytes(until));
			} else if (elapsed >= 15000) {
				return 0;
			}
		}
		
		byte tmp = playcard;
		playcard = -1;
		return tmp;
		
	}
	
	public SClient(Socket sock, Server parent) {
		
		try {
			socket = sock;
			socket.setSoTimeout(5000);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.id = System.currentTimeMillis();
		this.parentServer = parent;
		this.outpacks = new SPacketStackHandler(this);
		
		packetReceiver = new SClientReceiver(this);
		packetHandler = new SClientHandler(this);
		
		packetReceiver.setName("Packet receiver");
		packetHandler.setName("Packet handler");
		
		packetHandler.setDaemon(true);
		packetReceiver.setDaemon(true);
		
		packetReceiver.start();
		packetHandler.start();
		
		outpacks.send(pc_datarequest, new byte[] {pc_myname});
		outpacks.sendData(pc_myid, Packet.longToBytes(this.id));
		//Packet.sendDataPacket(out, pc_currentcards, new Deck().tcpBytes());
		for (SClient from : parent.clients) {
			outpacks.send(pc_playernameid, Packet.longToBytes(from.id), from.name.getBytes());
		}
	}
}
