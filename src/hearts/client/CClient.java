package hearts.client;

import static hearts.PacketConstants.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import hearts.DebugUtil;
import hearts.Deck;
import hearts.Packet;
import hearts.client.swing.ClientWindow;
public class CClient extends Thread {
	public Socket socket;
	public DataInputStream in;
	public DataOutputStream out;
	public Thread packetHandler;
	public UserIO userio;
	public long userid;
	public boolean running = true;
	public boolean gamestarted = false;
	public CopyOnWriteArrayList<byte[]> packets = new CopyOnWriteArrayList<byte[]>();
	public HashMap<Long, OtherPlayer> otherClients = new HashMap<Long, OtherPlayer>();
	public String username = "Player" + (new Random()).nextInt(100000);
	public ClientWindow window;
	public Deck currentDeck;
	
	public CClient(Socket sock) {
		try {
			socket = sock;
			socket.setSoTimeout(5000);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		packetHandler = new Thread() {
			public void run() {
				while (running) {
					try {
						int packetLen = in.readInt();
						byte[] packet = new byte[packetLen];
						in.read(packet);
						packets.add(packet);
					} catch (SocketException e) {
						window.chat.println("Exiting.." + e.getMessage());
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		window = new ClientWindow(this);
		this.userio = new UserIO(this);
		packetHandler.start();
		this.userio.start();
		this.start();
		
		
		
	}
	
	public void run() {
		while (running) {
			try {
				if (packets.size() == 0) continue;
				byte[] rawPacket = packets.remove(0);
				byte packetType = rawPacket[0];
				byte[] packetContent = Packet.shiftBackwards(rawPacket);
				switch(packetType) {
				case pc_keepalive:
					Packet.sendPacket(out, pc_keepalive, new byte[] {});
					break;
				case pc_message:
					String msg = new String(packetContent);
					this.window.chat.println(msg);
					break;
				case pc_datarequest:
					switch(packetContent[0]) {
					case pc_myname:
						Packet.sendDataPacket(out, pc_myname, username.getBytes());
						break;
					case pc_myplaycard:
						byte card = 0;
						Packet.sendDataPacket(out, pc_myplaycard, new byte[] {card});
						break;
					}
					break;
				case pc_datapayload:
					byte[] datacontent = Packet.shiftBackwards(packetContent);
					this.window.chat.println("Received data packet: " + packetContent[0] + "; content=" + DebugUtil.bstr(datacontent));
					
					switch(packetContent[0]) {
					case pc_gamestarted:
						if (datacontent[0] == 1) {
							this.window.chat.println("Game starting");
							gamestarted = true;
						} else {
							gamestarted = false;
						}
						break;
					case pc_myid:
						this.userid = Packet.bytesToLong(datacontent);
						this.window.chat.println("My ID is " + this.userid);
						break;
					case pc_turnwarning: // going dangerously late
						{
							// TODO
							long time = Packet.bytesToLong(datacontent);
							long elapsed = System.currentTimeMillis() - time;
							this.window.chat.println("Warning: going dangerously late: " + elapsed/1000 + "s left");
						}
						break;
					case pc_playerid:
						{
							long id = Packet.bytesToLong(datacontent);
							otherClients.put(id, new OtherPlayer(id));
						}
						break;
					case pc_playername:
						{
							long id = Packet.bytesToLong(datacontent);
							byte[] name = Packet.shiftBackwards(datacontent, 8);
							otherClients.get(id).setName(new String(name));
							this.window.chat.println("Added client with name: " + new String(name));
						}
						break;
					case pc_currentcards:
						{
							Deck d = Deck.parseTCP(datacontent);
							this.window.chat.println("Received cards: " + d);
							this.currentDeck = d;
						}
						break;
					case pc_playerdc:
						{
							long id = Packet.bytesToLong(datacontent);
							String name = otherClients.get(id).getName();
							this.window.chat.println("Player disconnected: " + name);
						}
						break;
					}
					break;
				}
			} catch (SocketException e) {
				e.printStackTrace();
				this.window.chat.println("Exiting.." + e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				this.window.chat.println("Exiting.." + e.getMessage());
			}
			
		}
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket sock = new Socket(InetAddress.getByName("0.0.0.0"), 5555);
		CClient client = new CClient(sock);
		client.getName();
	}
}
