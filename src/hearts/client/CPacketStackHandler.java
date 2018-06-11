package hearts.client;

import static hearts.Constants.*;

import java.io.IOException;

import hearts.Packet;

public class CPacketStackHandler extends Thread {
	private CClient parent;
	private PacketStack<Packet> stack;
	public CPacketStackHandler(CClient c) {
		parent = c;
		stack = new PacketStack<Packet>();
		this.setName("Packet Stack Handler");
		this.setDaemon(true);
		this.start();
	}
	
	public void sendData(byte dataType, byte[]... content) {
		Packet pack = new Packet(pc_datapayload, Packet.ccat(new byte[][] {new byte[] {dataType}, Packet.ccat(content)}));
		stack.add(pack);
	}
	
	public void send(byte packetType, byte[]... content) {
		Packet pack = new Packet(packetType, Packet.ccat(content));
		stack.add(pack);
	}
	
	public void run() {
		while (parent.running) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				// this ensures that the while loop is run each time
				e1.printStackTrace();
			}
			if (stack.size() > 0) {
				try {
					stack.pop().send(parent.out);
				} catch (IOException e) {
					e.printStackTrace();
					parent.window.chat.println("Error with packet sending: " + e.getMessage());
					parent.running = false;
				}
			}
		}
	}
}
