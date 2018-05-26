package hearts;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A wrapper class for a deck of cards.
 * Includes functions for sending over the tcp connection.
 * Has functions to convert card id to string.
 * @author legoc
 *
 */

public class Hand {
	public byte[] cards = new byte[52];
	public static final String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
	public static final char[] numbers = {'A','2','3','4','5','6','7','8','9','0','J','Q','K'};
	// suit order: hearts, diamonds, clubs, spades.
	public Hand() {
		for (byte i = 0; i < 52; i++) {
			cards[i] = i;
		}
	}
	
	public Hand(byte[] cards) {
		this.cards = cards;
	}
	
	public String toString() {
		String str = "";
		
		return str;
	}
	
	public void shuffle() {
		shuffleArray(cards);
	}
	
	public void tcpBytes() {
		
	}
	
	public static Hand shuffled() {
		Hand d = new Hand();
		d.shuffle();
		return d;
	}
	
	public static Hand unshuffled() {
		return new Hand();
	}
	
	public static String cardToString(byte card) {
		int suit = card/4;
		int number = card % 13;
		
		return numbers[number] + " of " + suit;
	}
	
	// Implementing Fisher–Yates shuffle
	public static void shuffleArray(byte[] ar) {
		// If running on Java 6 or older, use `new Random()` on RHS here
		Random rnd = ThreadLocalRandom.current();
		for (int i = ar.length - 1; i > 0; i--)
		{
			int index = rnd.nextInt(i + 1);
			// Simple swap
			byte a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}
}
