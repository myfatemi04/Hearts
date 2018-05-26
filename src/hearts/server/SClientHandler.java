package hearts.server;

import static hearts.PacketConstants.*;

import hearts.DebugUtil;
import hearts.Packet;

public class SClientHandler extends Thread {
	SClient parentClient;
	Server parentServer;
	
	public SClientHandler(SClient parent) {
		this.parentClient = parent;
		this.parentServer = parentClient.parentServer;
	}
	public void run() {
		while (parentClient.alive) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			while (parentClient.packets.size() > 0) {
				Packet p = parentClient.packets.remove(0);
				byte packetType = p.packetType;
				byte[] packetContent = p.payload;
				switch(packetType) {
				case pc_keepalive:
					break;
				case pc_message:
					String msg = new String(packetContent);
					parentServer.broadcast(pc_message, (parentClient.name + ":" + msg).getBytes());
					break;
				case pc_datapayload:
					int data = packetContent[0];
					byte[] payload = Packet.shiftBackwards(packetContent);
					System.out.println(data + ";" + DebugUtil.bstr(payload));
					switch (data) {
					case pc_myname:
						parentClient.name = new String(payload);
						parentServer.broadcast(pc_datapayload, new byte[] {pc_playerid}, Packet.longToBytes(parentClient.id));
						parentServer.broadcast(pc_datapayload, new byte[] {pc_playername}, Packet.longToBytes(parentClient.id), parentClient.name.getBytes());
						break;
					default:
						break;
					}
					break;
				}
			}
		}
	}
}
