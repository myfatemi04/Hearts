package hearts.server;

import static hearts.Constants.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CopyOnWriteArrayList;

import hearts.DebugUtil;
import hearts.Deck;
import hearts.Packet;
import hearts.server.swing.ServerWindow;

public class Server extends Thread {
	protected CopyOnWriteArrayList<SClient> clients = new CopyOnWriteArrayList<SClient>();
	public ServerSocket serverSocket;
	protected int status = -1;
	protected int port = 5555;
	protected String ip = "127.0.0.1";
	protected Thread listenerThread;
	protected Thread keepaliveThread;
	protected int maxplayers = 2;
	public final int STATUS_ACCEPTING = 0;
	public final int STATUS_INGAME = 1;
	public final int STATUS_CLOSEGAME = 2;
	public ServerWindow window = null;
	
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
		
		this.window = new ServerWindow(this);
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
				to.outpacks.sendData(pc_playernameid, Packet.longToBytes(from.id), from.name.getBytes());
			}
		}
		broadcast(pc_message, " --- CURRENT STANDINGS --- \nPoints | Name".getBytes());
		for (SClient s : clients) {
			broadcast(pc_message, (padRight(s.points + "", 6) + "      " + s.name).getBytes());
		}
		for (int i = 0; i < 4; i++) {
			broadcast(pc_message, ("This is the start of a new round. ").getBytes());
			playRound();
		}
		
		broadcast(pc_message, " --- FINAL STANDINGS --- \nPoints | Name ".getBytes());
		for (SClient s : clients) {
			broadcast(pc_message, (padRight(s.points + "", 6) + "      " + s.name).getBytes());
		}
		
		broadcast(pc_message, ("[Server] Game has ended. Thank you for playing.").getBytes());
	}
	public void playRound() {
		Deck[] dealt = Deck.shuffled().deal(clients.size());
		for (int i = 0; i < clients.size(); i++) {
			clients.get(i).currentCards = dealt[i].sorted();
			clients.get(i).outpacks.sendData(pc_currentcards, dealt[i].tcpBytes());
		}
		
		
		try {
			int lastwinner = 0;
			for (int i = 0; i < 52 / clients.size(); i++) {
				broadcast(pc_message, ("[Server] Trick #" + (i + 1)).getBytes());
				byte[] trickCards = new byte[clients.size()];
				for (int x = 0; x < clients.size(); x++) {
					trickCards[x] = -1;
				}
				byte suit = 0;
				broadcast(pc_datapayload, new byte[] {pc_trickcards}, new Deck(trickCards).tcpBytes());
				broadcast(pc_datapayload, new byte[] {pc_tricksuit, -1});
				{
					SClient client = clients.get(lastwinner);
					
					client.outpacks.sendData(pc_mystart, new byte[0]);
					client.outpacks.sendData(pc_currentcards, client.currentCards.tcpBytes());
					
					byte cardindex = client.getCardindex();
					byte card = client.currentCards.cards[cardindex % client.currentCards.ncards];
					client.currentCards.remove(card);
					suit = (byte) (card / 13);
					
					client.outpacks.sendData(pc_confirmcard, new byte[] {card});
					
					broadcast(pc_datapayload, new byte[] {pc_tricksuit, suit});
					client.outpacks.send(pc_currentcards, client.currentCards.tcpBytes());
					trickCards[0] = card;
				}
				
				for (int c = 1; c < clients.size(); c++) {
					SClient client = clients.get((lastwinner + c) % clients.size());
					broadcast(pc_datapayload, new byte[] {pc_trickcards}, new Deck(trickCards).tcpBytes());
					client.outpacks.sendData(pc_myturn, new byte[0]);
					client.outpacks.sendData(pc_currentcards, client.currentCards.tcpBytes());
					
					Deck playableCards = client.currentCards.getPlayable(suit);
					byte cardindex = client.getCardindex();
					byte card = playableCards.cards[cardindex % playableCards.ncards];
					
					client.outpacks.sendData(pc_confirmcard, new byte[] {card});
					
					client.currentCards.remove(card);
					trickCards[c] = card;
					client.outpacks.sendData(pc_currentcards, client.currentCards.tcpBytes());
					broadcast(pc_datapayload, new byte[] {pc_trickcards}, new Deck(trickCards).tcpBytes());
				}
				byte points = 0;
				int winner = 0;
				int lastCard = trickCards[0];
				for (int c = 0; c < clients.size(); c++) {
					if (trickCards[c] / 13 == suit) {
						if (trickCards[c] > lastCard) {
							winner = (lastwinner + c) % clients.size();
							lastCard = trickCards[c];
						}
					}
					if (trickCards[c] / 13 == 0) {
						points += 1;
					} else if (trickCards[c] == 49) {
						points += 13;
					}
				}
				
				SClient winnerClient = clients.get(winner);
				winnerClient.points += points;
				winnerClient.outpacks.sendData(pc_iwon, new byte[] {});
				winnerClient.outpacks.sendData(pc_currentpoints, new byte[] {(byte) winnerClient.points});
				broadcast(pc_message, (winnerClient.name + " won the trick. " + DebugUtil.bstr(trickCards)).getBytes());
				broadcast(pc_message, (winnerClient.name + " has " + winnerClient.points + " point(s).").getBytes());
				
				lastwinner = winner;
				System.out.println(lastwinner);
				
				
			} // main round for loop
			
			// check for shooting the moon
			for (SClient s : clients) {
				if (s.points == 26) {
					broadcast(pc_message, (s.name + " has shot the moon! Everyone else gets 26 points!").getBytes());
				}
			}
			
			broadcast(pc_message, " --- CURRENT STANDINGS --- \nPoints | ".getBytes());
			for (SClient s : clients) {
				broadcast(pc_message, (padRight(s.points + "", 6) + "   " + s.name).getBytes());
			}
		} catch (Exception e) {
			broadcast(pc_message, ("[Server] Sorry, there has been a server-side error. ").getBytes());
			window.chat.println(e.getMessage());
			e.printStackTrace();
		} // main round try clause
	}
	public void broadcast(byte messageType, byte[]... content) {
		window.chat.println("Broadcast " + messageType + "_" + DebugUtil.bstr(Packet.ccat(content)));
		if (messageType == pc_message) {
			window.chat.println(new String(Packet.ccat(content)));
		}
		for (SClient to : clients) {
			to.outpacks.send(messageType, content);
		}
	}
	
	public static void main(String[] args) {
		
		Server server = new Server(5555);
		server.startServer();
	}
	public static String padRight(String s, int n) {
	     return String.format("%1$-" + n + "s", s);  
	}

	public static String padLeft(String s, int n) {
	    return String.format("%1$" + n + "s", s);  
	}

}
