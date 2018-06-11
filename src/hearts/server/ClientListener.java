package hearts.server;

import java.io.IOException;
import java.net.Socket;

public class ClientListener extends Thread {
	private Server parent;
	public ClientListener(Server parent) {
		this.parent = parent;
	}
	public void run() {
		parent.window.chat.println("Listening on " + parent.ip + ":" + parent.port + "...");
		while (parent.status == parent.STATUS_ACCEPTING) {
			try {
				Socket socket = parent.serverSocket.accept();
				SClient client = new SClient(socket, parent); // create a client with the socket
				parent.addClient(client);
				parent.window.chat.println(" received. " + parent.clients.size() + "/" + parent.maxplayers);
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			if (parent.clients.size() == parent.maxplayers) {
				parent.status = parent.STATUS_INGAME;
			}
		}
	}
}
