package hearts.server.swing;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

import hearts.server.Server;

public class ServerWindow extends JFrame {
	Server parent;
	public JLabel ipport = new JLabel();
	public ChatPanel chat;
	
	public ServerWindow(Server parent) {
		super();
		
		this.parent = parent;
		this.chat = new ChatPanel(parent);
		
		prepare();
		display();
	}
	
	public void prepare() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		ipport.setText("IP/port: " + parent.serverSocket.getInetAddress().toString() + ":" + parent.serverSocket.getLocalPort());
		this.add(ipport, BorderLayout.NORTH);
		this.add(chat, BorderLayout.WEST);
	}
	
	public void display() {
		this.setTitle("Hearts Server v1.0");
		this.setSize(900, 600);
		this.setResizable(false);
		this.setVisible(true);
	}
}
