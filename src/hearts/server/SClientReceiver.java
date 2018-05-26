package hearts.server;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import hearts.Packet;

public class SClientReceiver extends Thread {
	SClient parent;
	
	public SClientReceiver(SClient parent) {
		this.parent = parent;
	}
	public void run() {
		while (parent.alive) {
			try {
				parent.packets.add(Packet.nextPacket(parent.in));
			} catch (SocketException | SocketTimeoutException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				parent.dispose();
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
