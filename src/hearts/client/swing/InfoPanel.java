package hearts.client.swing;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import hearts.Constants;
import hearts.Deck;
import hearts.client.CClient;

public class InfoPanel extends JPanel {
	public CClient parent = null;
	public JTextArea cards = new JTextArea(5, 20);
	public JTextArea playableCards = new JTextArea(5, 20);
	public JTextArea trickCards = new JTextArea(4, 20);
	public JScrollPane scrollCards;
	public JScrollPane scrollPlayable;

	public InfoPanel(CClient c) {
		super();
		this.setLayout(new GridLayout(0, 1));
		this.setBorder(new TitledBorder(new EtchedBorder(), "Cards"));
		parent = c;
		cards.setBorder(new TitledBorder(new EtchedBorder(), "All cards"));
		
		cards.setFont(Constants.consolas);
		playableCards.setFont(Constants.consolas);
		trickCards.setFont(Constants.consolas);
		
		playableCards.setBorder(new TitledBorder(new EtchedBorder(), "Playable cards"));
		trickCards.setBorder(new TitledBorder(new EtchedBorder(), "Cards from this trick"));
		scrollCards = new JScrollPane(cards);
		scrollCards.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPlayable = new JScrollPane(playableCards);
		scrollPlayable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		this.add(scrollCards);
		this.add(scrollPlayable);
		this.add(trickCards);
		
		cards.setEditable(false);
		playableCards.setEditable(false);
	}
	public void setCards(JTextArea area, Deck d) {
		area.setEditable(true);
		area.setText(d.toNumberedString());
		area.setEditable(false);
	}
	
}