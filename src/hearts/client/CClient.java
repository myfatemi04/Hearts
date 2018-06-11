package hearts.client;

import static hearts.Constants.*;

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

import hearts.Deck;
import hearts.Packet;
import hearts.client.swing.ClientWindow;
public class CClient extends Thread {
	public Socket socket;
	public DataInputStream in;
	public DataOutputStream out;
	public Thread packetHandler;
	public long userid;
	public boolean running = true;
	public boolean gamestarted = false;
	public CopyOnWriteArrayList<byte[]> receivedPackets = new CopyOnWriteArrayList<byte[]>();
	public HashMap<Long, OtherPlayer> otherClients = new HashMap<Long, OtherPlayer>();
	public String username = "Player" + (new Random()).nextInt(100000);
	public ClientWindow window;
	public Deck currentDeck;
	public CPacketStackHandler outpacks;
	
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
						receivedPackets.add(packet);
					} catch (SocketException e) {
						window.chat.println("Exiting.." + e.getMessage());
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		this.window = new ClientWindow(this);
		this.outpacks = new CPacketStackHandler(this);
		this.packetHandler.start();
		this.start();
		
		
		
	}
	
	public void run() {
		byte suit = -1;
		while (running) {
			try {
				if (receivedPackets.size() == 0) continue;
				byte[] rawPacket = receivedPackets.remove(0);
				byte packetType = rawPacket[0];
				byte[] packetContent = Packet.shiftBackwards(rawPacket);
				switch(packetType) {
				case pc_keepalive:
					outpacks.send(pc_keepalive, new byte[] {});
					break;
				case pc_message:
					String msg = new String(packetContent);
					this.window.chat.println(msg);
					break;
				case pc_datarequest:
					switch(packetContent[0]) {
					case pc_myname:
						outpacks.sendData(pc_myname, username.getBytes());
						break;
					case pc_myplaycard:
						this.window.chat.println("Requesting YOUR card.");
						//{byte card = 0;
						//outpacks.sendData(pc_myplaycard, Packet.longToBytes(card));}
						break;
					case pc_mystart:
						this.window.info.setCards(this.window.info.playableCards, currentDeck);
						this.window.chat.println("Requesting YOUR card.");
						//{byte card = 0;
						//outpacks.sendData(pc_myplaycard, Packet.longToBytes(card));}
						break;
					}
					break;
				case pc_datapayload:
					byte[] datacontent = Packet.shiftBackwards(packetContent);
					//this.window.chat.println("Received data packet: " + packetContent[0] + "; content=" + DebugUtil.bstr(datacontent));
					
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
							long until = time - System.currentTimeMillis();
							this.window.chat.println("Warning: going dangerously late: " + until/1000 + "s left");
						}
						break;
					case pc_playernameid:
						{
							long id = Packet.bytesToLong(datacontent);
							byte[] name = Packet.shiftBackwards(datacontent, 8);
							if (!otherClients.containsKey(id)) {
								otherClients.put(id, new OtherPlayer(id));
							}
							
							otherClients.get(id).setName(new String(name));
							this.window.chat.println("Added client with name: " + new String(name));
								
						}
						break;
					case pc_currentcards:
						{
							Deck d = Deck.parseTCP(datacontent);
							//this.window.chat.println("Received cards: " + d);
							this.currentDeck = d;
							this.window.info.setCards(this.window.info.cards, d/*.sorted()*/);
							this.window.info.setCards(this.window.info.playableCards, d.getPlayable(suit)/*.sorted()*/);
						}
						break;
					case pc_playerdc:
						{
							long id = Packet.bytesToLong(datacontent);
							String name = otherClients.get(id).getName();
							this.window.chat.println("Player disconnected: " + name);
						}
						break;
					case pc_tricksuit:
						{
							suit = datacontent[0];
							if(datacontent[0] > -1) {
								this.window.chat.println("Suit: " + Deck.suits[suit]);
								Deck available = this.currentDeck.getPlayable(datacontent[0]);
								//this.window.chat.println("Current playable cards: " + available);
								this.window.info.setCards(this.window.info.playableCards, available/*.sorted()*/);
							} else {
								this.window.info.setCards(this.window.info.playableCards, currentDeck/*.sorted()*/);
							}
						}
						break;
					case pc_iwon:
						{
							this.window.chat.println("You won the trick.");
						}
						break;
					case pc_confirmcard:
						this.window.chat.println("You played a " + Deck.cardToString(datacontent[0]));
						break;
					case pc_currentpoints:
						this.window.chat.println("You have " + datacontent[0] + " point(s).");
						break;
					case pc_trickcards:
						Deck trickCards = Deck.parseTCP(datacontent);
						this.window.info.setCards(this.window.info.trickCards, trickCards/*.sorted()*/);
					}
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		String ip = "0.0.0.0";
		if (args.length > 0) {
			ip = args[0];
		}
		Socket sock = new Socket(InetAddress.getByName(ip), 5555);
		CClient client = new CClient(sock);
		client.getName();
	}
}
