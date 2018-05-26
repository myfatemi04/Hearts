package hearts.server;

import static hearts.PacketConstants.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
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
	
	boolean alive = true;
	String name = null;
	long id;
	Deck currentCards;
	
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
		packetReceiver = new SClientReceiver(this);
		packetHandler = new SClientHandler(this);
		
		packetReceiver.setName("Packet receiver");
		packetHandler.setName("Packet handler");
		
		packetHandler.setDaemon(true);
		packetReceiver.setDaemon(true);
		
		packetReceiver.start();
		packetHandler.start();
		
		try { 
			Packet.sendRequestPacket(out, pc_myname);
			Packet.sendDataPacket(out, pc_myid, Packet.longToBytes(this.id));
			//Packet.sendDataPacket(out, pc_currentcards, new Deck().tcpBytes());
			for (SClient from : parent.clients) {
				try {
					Packet.sendDataPacket(out, pc_playerid, Packet.longToBytes(from.id));
					Packet.sendDataPacket(out, pc_playername, Packet.longToBytes(from.id), from.name.getBytes());
				} catch (SocketException e) {
					e.printStackTrace();
					this.dispose();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (SocketException e) {
			this.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
