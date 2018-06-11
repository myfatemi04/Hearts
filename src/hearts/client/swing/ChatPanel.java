package hearts.client.swing;

import static hearts.Constants.*;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import hearts.Packet;
import hearts.client.CClient;

public class ChatPanel extends JPanel {
	public JTextArea chat;
	public JTextField entry;
	public JScrollPane scroll;
	public ClientWindow display;
	public CClient c;
	public ArrayList<String> chatStack = new ArrayList<String>();
	public ChatPanel(CClient c) {
		super();
		
		this.setLayout(new GridLayout(0, 1));
		this.setBorder(new TitledBorder(new EtchedBorder(), "Chat"));
		entry = new JTextField(50);
		chat = new JTextArea("", 5, 50);
		
		entry.setFont(consolas);
		chat.setFont(consolas);
		
		display = c.window;
		scroll = new JScrollPane(chat);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		this.c = c;
		
		chat.setEditable(false);
		
		entry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String message = entry.getText();
				if (message.startsWith("/card ")) {
					try {
						byte card = Byte.parseByte(message.substring(6));
						c.outpacks.sendData(pc_myplaycard, Packet.longToBytes(card));
					} catch (NumberFormatException e) {
						println("Invalid number for card. ");
					}
					
					entry.setText("");
				} else {
					entry.setText("");
					c.outpacks.send(pc_message, message.getBytes());
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
		scrollToBottom(scroll);
		this.chat.setEditable(true);
		this.chat.setText(this.chat.getText() + "\n" + message);
		this.chat.setEditable(false);
		this.chat.repaint();
		scrollToBottom(scroll);
	}
	private void scrollToBottom(JScrollPane scrollPane) {
		JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
		verticalBar.setValue(verticalBar.getMaximum());
		try {
			Thread.sleep(75);
		} catch (Exception e) {}
	}
}
