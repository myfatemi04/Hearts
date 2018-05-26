package hearts.client;

import static hearts.PacketConstants.*;

import java.io.IOException;
import java.net.SocketException;
import java.util.Scanner;

import hearts.Packet;

public class UserIO extends Thread {
	CClient client = null;
	Scanner s = new Scanner(System.in);
	public UserIO(CClient c) {
		this.client = c;
	}
	
	public void run() {
		while (client.running) {
			try {
				String line = s.nextLine();
				if (line.startsWith("/setname ")) {
					String rest = line.substring("/setname ".length());
					Packet.sendPacket(client.out, pc_datapayload, new byte[] {pc_myname}, rest.getBytes());
				} else {
					Packet.sendPacket(client.out, pc_message, line.getBytes());
				}
			} catch (SocketException e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
				client.running = false;
				break;
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
			}
		}
	}
	
	public void requestData(byte datatype, String message) {
		
	}
}
