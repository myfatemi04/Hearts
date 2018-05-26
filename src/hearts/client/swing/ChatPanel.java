package hearts.client.swing;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import hearts.Packet;
import hearts.PacketConstants;
import hearts.client.CClient;

public class ChatPanel extends JPanel {
	public JTextArea chat;
	public JTextField entry;
	public JScrollPane scroll;
	public ClientWindow display;
	public CClient c;
	public ChatPanel(CClient c) {
		super();
		
		this.setLayout(new GridLayout(0, 1));
		this.setBorder(new TitledBorder(new EtchedBorder(), "Chat"));
		entry = new JTextField(50);
		chat = new JTextArea("", 5, 50);
		display = c.window;
		scroll = new JScrollPane(chat);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		this.c = c;
		
		chat.setEditable(false);
		
		entry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String message = entry.getText();
				
				entry.setText("");
				try {
					Packet.sendPacket(c.out, PacketConstants.pc_message, message.getBytes());
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		
		});
		this.add(scroll);
		this.add(entry);
	}
	public void clearChat() {
		this.chat.setEditable(true);
		this.chat.setText("");
		this.chat.setEditable(false);
	}
	public void println(String message) {
		this.chat.setEditable(true);
		this.chat.setText(this.chat.getText() + "\n" + message);
		this.chat.setEditable(false);
	}
}
