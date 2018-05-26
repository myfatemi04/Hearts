package hearts.client;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
@SuppressWarnings("all")
public class SocketTest {
	public static void main(String[] args) throws Exception {
		Scanner s = new Scanner(System.in);
		
		// get ip
		String ip = "0.0.0.0";
		boolean haveIp = false;
		while (!haveIp) {
			System.out.print("IP Address:");
			String ipstr = s.nextLine();
			if (!ipstr.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
				System.out.println("Invalid IP Address.");
			} else {
				ip = ipstr;
				haveIp = true;
			}
		}
		
		
		// get port
		int port = -1;
		while (port < 0) {
			System.out.print("Port:");
			try {
				port = Integer.parseInt(s.nextLine());
				if (port < 0) {
					System.out.println("Error: Invalid port");
				}
			} catch (NumberFormatException e) {
				System.out.println("Error: Invalid port");
			}
		}
		
		s.close();
		Socket connection = null;
		try {
			connection = connectToAddr(ip, port);
		} catch (IOException e) {
			System.out.println("Error when connecting to server.");
			e.printStackTrace();
			return;
		}
		
		DataInputStream input = new DataInputStream(connection.getInputStream());
		DataOutputStream output = new DataOutputStream(connection.getOutputStream());
		output.writeUTF("confirm?");
		
		try {
			connection.close();
		} catch (IOException e) {
			System.err.println("Error when closing the socket.");
		}
	
	}
	public static Socket connectToAddr(String ip, int port) throws IOException {
		InetAddress addr = null;
		addr = InetAddress.getByName(ip);
		Socket connection = new Socket(addr, port);
		
		return connection;
	}
}
