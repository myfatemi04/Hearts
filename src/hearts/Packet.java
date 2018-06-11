package hearts;

import static hearts.Constants.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Packet {
	public byte[] payload;
	public byte packetType;
	public Packet(byte packetType, byte[] payload) {
		this.packetType = packetType;
		this.payload = payload;
	}
	
	public void send(DataOutputStream out) throws SocketException, IOException, SocketTimeoutException{
		Packet.sendPacket(out, packetType, payload);
	}
	
	public static byte[] ccat(byte[][] data) {
		byte[] total = null;
		int len = 0;
		int counter = 0;
		for (byte[] b : data)
			len += b.length;
		total = new byte[len];
		for (byte[] b : data) {
			for (int i = 0; i < b.length; i++) {
				total[counter++] = b[i];
			}
		}
		return total;
	}

	public static void sendRequestPacket(DataOutputStream out, byte datatype, byte[]... packetContent) throws SocketException, IOException, SocketTimeoutException {
		sendPacket0(out, pc_datarequest, Packet.ccat(new byte[][] {new byte[] {datatype}, Packet.ccat(packetContent)}));
	}

	public static void sendDataPacket(DataOutputStream out, byte datatype, byte[]... packetContent) throws SocketException, IOException, SocketTimeoutException {
		System.out.println("Sending data packet: " + datatype + "; content=" + DebugUtil.bstr(Packet.ccat(packetContent)));
		sendPacket0(out, pc_datapayload, Packet.ccat(new byte[][] {new byte[] {datatype}, Packet.ccat(packetContent)}));
	}

	public static void sendPacket0(DataOutputStream out, int packetType, byte[] packetContent) throws SocketException, IOException, SocketTimeoutException {
		int len = (packetContent.length + 1);
		byte[] packet = new byte[len];
		
		packet[0] = (byte)packetType;
		for (int i = 1; i < len; i++) {
			packet[i] = packetContent[i - 1];
		}
		
		out.writeInt(len);
		out.write(packet);
	}

	public static void sendPacket(DataOutputStream out, int packetType, byte[]... packetContent) throws SocketException, IOException {
		Packet.sendPacket0(out, packetType, Packet.ccat(packetContent));
	}

	public static byte[] join(byte[]... b) {
		return Packet.ccat(b);
	}
	
	public static byte[] shiftBackwards(byte[] b, int amount) { // shift bytes over by 1, aka substring(1)
		byte[] content = new byte[b.length - amount];
		for (int i = 0; i < b.length - amount; i++) {
			content[i] = b[i + amount];
		}
		return content;
	}
	public static byte[] shiftBackwards(byte[] b) {
		return shiftBackwards(b, 1);
	}
	public static byte[] shiftForwards(byte[] b) {
		byte[] content = new byte[b.length + 1];
		for (int i = 0; i < b.length; i++) {
			content[i + 1] = b[i];
		}
		return content;
	}
	
	public static byte[] longToBytes(long l) {
	    byte[] result = new byte[8];
	    for (int i = 7; i >= 0; i--) {
	        result[i] = (byte)(l & 0xFF);
	        l >>= 8;
	    }
	    return result;
	}

	public static long bytesToLong(byte[] b) {
	    long result = 0;
	    for (int i = 0; i < 8; i++) {
	        result <<= 8;
	        result |= (b[i] & 0xFF);
	    }
	    return result;
	}
	public static Packet decode(byte[] b) {
		byte packetType = b[0];
		return new Packet(packetType, shiftBackwards(b));
	}
	public static Packet nextPacket(DataInputStream in) throws IOException, SocketException, SocketTimeoutException {
		int len = in.readInt();
		byte[] packetbytes = new byte[len];
		in.read(packetbytes);
		return decode(packetbytes);
	}
}
