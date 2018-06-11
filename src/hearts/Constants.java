package hearts;

import java.awt.Font;

public class Constants {
	public static final byte pc_keepalive = 0;
	public static final byte pc_message = 1; // as in a chat message
	public static final byte pc_datarequest = 2;
	public static final byte pc_datapayload = 3; 
		public static final byte pc_myname = 0x00;
		public static final byte pc_myid = 0x01;
		public static final byte pc_playernameid = 0x02;
		public static final byte pc_currentpoints = 0x03;
		 
		public static final byte pc_currentcards = 0x10;
		public static final byte pc_playablecards = 0x11;

		public static final byte pc_startplayer = 0x20;
		public static final byte pc_tricksuit = 0x21;
		public static final byte pc_lasttrickwinner = 0x22;
		public static final byte pc_myplaycard = 0x23;
		public static final byte pc_myturn = 0x24;
		public static final byte pc_trickcards = 0x25;
		public static final byte pc_trickreset = 0x26;
		public static final byte pc_mystart = 0x27;
		public static final byte pc_iwon = 0x28;
		public static final byte pc_confirmcard = 0x29;
		
		public static final byte pc_gamestarted = 0x30;
		public static final byte pc_turnwarning = 0x31;
		public static final byte pc_autoplay = 0x32; // tells user that the computer played for them
		
		public static final byte pc_youdc = 0x40;
		public static final byte pc_playerdc = 0x41;
		
		
		/*
		 * Data Payload Formats
		 * 
		 * Name: Bytestring
		 * ID: 8-Bytes Long
		 * 
		 * PlayerID: 8-Bytes Long
		 * PlayerName: (PlayerID, Bytestring)
		 * 
		 * TurnWarning: 8-Bytes Long, Time in millis of autoplay.
		 * AutoPlay: Tells you that the server played for you.
		 * 
		 * 
		 */
	public static final Font consolas = new Font("Consolas", Font.PLAIN, 12);
}
