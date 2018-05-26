package hearts.server;


import static hearts.PacketConstants.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;

import hearts.DebugUtil;
import hearts.Deck;
import hearts.Packet;

public class Server extends Thread {
	protected CopyOnWriteArrayList<SClient> clients = new CopyOnWriteArrayList<SClient>();
	protected ServerSocket serverSocket;
	protected int status = -1;
	protected int port = 5555;
	protected String ip = "127.0.0.1";
	protected Thread listenerThread;
	protected Thread keepaliveThread;
	protected int maxplayers = 2;
	public final int STATUS_ACCEPTING = 0;
	public final int STATUS_INGAME = 1;
	public final int STATUS_CLOSEGAME = 2;
	
	protected void addClient(SClient c) {
		clients.add(c);
	}
	
	public Server(int port) {
		try {
			this.port = port;
			this.serverSocket = new ServerSocket(this.port);
			this.listenerThread = new ClientListener(this);
			this.ip = serverSocket.getInetAddress().toString();
			this.keepaliveThread = new ServerKeepalives(this);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void startServer() {
		this.status = STATUS_ACCEPTING;
		this.listenerThread.start();
		this.keepaliveThread.start();
		this.setName("Server");
		this.start();
	}
	
	public boolean allClientsReady() {
		for (SClient c : clients) {
			if (!c.ready()) {
				return false;
			}
		}
		return true;
	}
	
	public void run() {
		try {
			this.listenerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		broadcast(pc_datapayload, new byte[] {pc_gamestarted, 1});
		// telling clients that game has started
		
		while (!allClientsReady()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		// telling clients about other players. redo at start of game.
		for (SClient to : clients) {
			for (SClient from : clients) {
				try {
					Packet.sendDataPacket(to.out, pc_playerid, Packet.longToBytes(from.id));
					Packet.sendDataPacket(to.out, pc_playername, Packet.longToBytes(from.id), from.name.getBytes());
				} catch (SocketException e) {
					e.printStackTrace();
					to.dispose();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Deck[] dealt = Deck.shuffled().deal(maxplayers);
		for (int i = 0; i < clients.size(); i++) {
			clients.get(i).currentCards = dealt[i];
			try {
				Packet.sendDataPacket(clients.get(i).out, pc_currentcards, dealt[i].tcpBytes());
			} catch (IOException e) {
				clients.get(i).dispose();
			}
			
		}
		
	}
	
	public void broadcast(byte messageType, byte[]... content) {
		System.out.println("Broadcast " + messageType + "_" + DebugUtil.bstr(Packet.ccat(content)));
		for (SClient to : clients) {
			try {
				Packet.sendPacket(to.out, messageType, content);
			} catch (SocketException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server(5555);
		server.startServer();
	}
}
