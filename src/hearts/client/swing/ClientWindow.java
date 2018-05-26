package hearts.client.swing;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

import hearts.client.CClient;

public class ClientWindow extends JFrame {
	CClient parent;
	public JLabel ipport = new JLabel();
	public ChatPanel chat;
	
	public ClientWindow(CClient parent) {
		super();
		
		this.parent = parent;
		this.chat = new ChatPanel(parent);
		
		prepare();
		display();
	}
	
	public void prepare() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		ipport.setText("IP/port: " + parent.socket.getInetAddress().toString() + ":" + parent.socket.getPort());
		this.add(ipport, BorderLayout.NORTH);
		this.add(chat, BorderLayout.WEST);
		
	}
	
	public void display() {
		this.setTitle("Hearts Client v1.0");
		this.setSize(640, 480);
		this.setResizable(false);
		this.setVisible(true);
	}
}
