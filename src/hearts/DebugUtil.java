package hearts;

public class DebugUtil {
	public static String bstr(byte[] b) {
		String s = "";
		for (byte c : b) {
			s += c + ", ";
		}
		return s;
	}
	
	public String packetToString(int packetType, byte[] packet) {
		String pack = "Packet " + packetType + ", content=";
		for (int i = 0; i < packet.length; i++) {
			pack += packet[i];
			if (i < packet.length - 1) {
				pack += ", ";
			}
		}
		return pack;
	}
}
