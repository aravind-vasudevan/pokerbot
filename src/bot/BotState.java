/**
 * www.TheAIGames.com 
 * Heads Up Omaha pokerbot
 *
 * Last update: May 07, 2014
 *
 * @author Jim van Eeden, Starapple
 * @version 1.0
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package bot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;
import java.util.*;

import java.lang.Object;

import poker.Card;
import poker.HandHoldem;
import poker.PokerMove;

import com.stevebrecher.HandEval;

/**
 * Class that parses strings given by the engine and stores values for later use.
 */
public class BotState {
	
	private int round, smallBlind, bigBlind;
	
	private boolean onButton;
	
	private int myStack, opponentStack;
	
	private int pot;
	
	private PokerMove opponentMove;
	
	private int currentBet;
	
	private int amountToCall;
	
	private HandHoldem hand;
	
	private Card[] table;
	
	private Map<String,String> settings = new HashMap<String,String>();
	
	private String myName = "Jeeves";
	
	private int[] sidepots;
	
	private int timeBank, timePerMove;
	
	private int handsPerLevel;

	// My variables
	private boolean beginningOfRound;

	private Map<String, ArrayList<Card>> outs = new HashMap<String, ArrayList<Card>>();

	public static Set<String> handCategoryValues = new HashSet<String>();
	
	/**
	 * Parses the settings for this game
	 * @param key : key of the information given
	 * @param value : value to be set for the key
	 */
	protected void updateSetting(String key, String value) {

		// Calculate the hand category values once (enum to set of strings)
		if( handCategoryValues.size() == 0 )
			 for (HandEval.HandCategory me : HandEval.HandCategory.values())
		        handCategoryValues.add( me.toString() );

		settings.put(key, value);
		if( key.equals("your_bot") ) {
			myName = value;
		} else if ( key.equals("timebank") ) {			// Maximum amount of time your bot can take for one response
			timeBank = Integer.valueOf(value);
		} else if ( key.equals("time_per_move") ) {		// The extra amount of time you get per response
			timePerMove = Integer.valueOf(value);
		} else if ( key.equals("hands_per_level") ) {	// Number of rounds before the blinds are increased
			handsPerLevel = Integer.valueOf(value);
		} else if ( key.equals("starting_stack") ) {	// Starting stack for each bot
			myStack = Integer.valueOf(value);
			opponentStack = Integer.valueOf(value);
		} else {
			System.err.printf("Unknown settings command: %s %s\n", key, value);
		}
	}

	/**
	 * Parses the match information
	 * @param key : key of the information given
	 * @param value : value to be set for the key
	 */
	protected void updateMatch(String key, String value) {
		if( key.equals("round") ) { 				// Round number
			round = Integer.valueOf(value);
			System.err.println("Round " + round);   //printing the round to the output for debugging
            resetRoundVariables();
		} else if( key.equals("small_blind") ) {	// Value of the small blind
			smallBlind = Integer.valueOf(value);
		} else if( key.equals("big_blind") ) {		// Value of the big blind
			bigBlind = Integer.valueOf(value);
		} else if( key.equals("on_button") ) {		// Which bot has the button, onButton is true if it's your bot
			onButton = value.equals(myName);
		} else if( key.equals("max_win_pot") ) {	// The size of the current pot
			pot = Integer.valueOf(value);
		} else if( key.equals("amount_to_call") ) {	// The amount of the call
			amountToCall = Integer.valueOf(value);
		} else if ( key.equals("table") ) {			// The cards on the table
			table = parseCards(value);
		} else {
			System.err.printf("Unknown match command: %s %s\n", key, value);
		}
	}

	/**
	 * Parses the information given about stacks, blinds and moves
	 * @param bot : bot that this move belongs to (either you or the opponent)
	 * @param key : key of the information given
	 * @param amount : value to be set for the key
	 */
	protected void updateMove(String bot, String key, String amount) {
		if( bot.equals(myName) ) {
			if( key.equals("stack") ) {					// The amount in your starting stack
				myStack = Integer.valueOf(amount);
			}
			else if ( key.equals("post") ) {			// The amount you have to pay for the blind
				myStack -= Integer.valueOf(amount);
			}
			else if( key.equals("hand") ) {				// Your cards
				Card[] cards = parseCards(amount);
				hand = new HandHoldem(cards[0], cards[1]);
			} else if ( key.equals("wins") ) {
				// Your winnings, not stored
			} else {
				// That should be all
			}
		} else { // assume it's the opponent
			if( key.equals("stack") ) {					// The amount in your opponent's starting stack
				opponentStack = Integer.valueOf(amount);
			} else if ( key.equals("post") ) {			// The amount your opponent paid for the blind
				opponentStack -= Integer.valueOf(amount);
			} else if ( key.equals("hand")){
				// Hand of the opponent on a showdown, not stored
			} else if ( key.equals("wins") ) {
				// Opponent winnings, not stored
			} else {									// The move your opponent did
                opponentMove = new PokerMove(bot, key, Integer.valueOf(amount));					
			}
		}
	}

	/**
	 * Parse the input string from the engine to actual Card objects
	 * @param String value : input
	 * @return Card[] : array of Card objects
	 */
	private Card[] parseCards(String value) {
		if( value.endsWith("]") ) { value = value.substring(0, value.length()-1); }
		if( value.startsWith("[") ) { value = value.substring(1); }
		if( value.length() == 0 ) { return new Card[0]; }
		String[] parts = value.split(",");
		Card[] cards = new Card[parts.length];
		for( int i = 0; i < parts.length; ++i ) {
			cards[i] = Card.getCard(parts[i]);
		}
		return cards;
	}
	
	/**
	 * Reset all the variables at the start of the round,
	 * just to make sure we don't use old values
	 */
	private void resetRoundVariables() {
		beginningOfRound = true;
		smallBlind = 0;
		bigBlind = 0;
		pot = 0;
		opponentMove = null;
		amountToCall = 0;
		hand = null;
		table = new Card[0];
		outs.clear();
	}

	public int getRound() {
		return round;
	}

	public int getSmallBlind() {
		return smallBlind;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	public boolean onButton() {
		return onButton;
	}

	public void setbeginningOfRound(boolean b) {
		beginningOfRound = b;
	}

	public int getmyStack() {
		return myStack;
	}

	public int getOpponentStack() {
		return opponentStack;
	}
	
	public int getPot() {
		return pot;
	}
	
	public PokerMove getOpponentAction() {
		return opponentMove;
	}
	
	public int getCurrentBet() {
		return currentBet;
	}

	public HandHoldem getHand() {
		return hand;
	}

	public Card[] getTable() {
		return table;
	}
	
	public String getSetting(String key) {
		return settings.get(key);
	}

	public int[] getSidepots() {
		return sidepots;
	}

	public String getMyName() {
		return myName;
	}
	
	public int getAmountToCall() {
		return amountToCall;
	}

	// Copying getHandCategory method from BotStarter 
	/**
	 * Calculates the bot's hand strength, with 0, 3, 4 or 5 cards on the table.
	 * This uses the com.stevebrecher package to get hand strength.
	 * @param hand : cards in hand
	 * @param table : cards on table
	 * @return HandCategory with what the bot has got, given the table and hand
	 */
	public HandEval.HandCategory getHandCategory(HandHoldem hand, Card[] table) {
		if( table == null || table.length == 0 ) { // there are no cards on the table
			return hand.getCard(0).getHeight() == hand.getCard(1).getHeight() // return a pair if our hand cards are the same
					? HandEval.HandCategory.PAIR
					: HandEval.HandCategory.NO_PAIR;
		}
		long handCode = hand.getCard(0).getNumber() + hand.getCard(1).getNumber();

		for( Card card : table ) { handCode += card.getNumber(); }
		
		if( table.length == 3 ) { // three cards on the table
			return rankToCategory(HandEval.hand5Eval(handCode));
		}
		if( table.length == 4 ) { // four cards on the table
			return rankToCategory(HandEval.hand6Eval(handCode));
		}
		return rankToCategory(HandEval.hand7Eval(handCode)); // five cards on the table
	}
	
	/**
	 * small method to convert the int 'rank' to a readable enum called HandCategory
	 */
	public HandEval.HandCategory rankToCategory(int rank) {
		return HandEval.HandCategory.values()[rank >> HandEval.VALUE_SHIFT];
	}

	// My methods

	public boolean getbeginningOfRound() {
		return beginningOfRound;
	}

	public int getStreetNumber() {
		return table.length;
	}

	/**
	 * This function calculates the number of outs after the flop is out
	 */

	public void calculateOuts() {

		// Right now, we recalculate outs every time we get a new card. This can be
		// changed with a bit of memoization. Must look into this.
		outs.clear();
		ArrayList <Card> cardsToBeChecked = new ArrayList <Card>();

		// Bounds checking. Can't calculate outs when table is pre-flop
		// or post river
		if( table.length ==0 || table.length == 5 )
			return;

		// This search space of 52 cards can be reduced further
		// if we know what patterns to look for
		// i.e. look only for three of a kind and full houses 
		// because a straight or flush is not possible
		for( int i=0; i<52; ++i )
			cardsToBeChecked.add( new Card(i) );
		for( int i=0; i<table.length; ++i )
			cardsToBeChecked.remove( table[i] ); // Remove cards that are on the table
		for( int i=0; i<hand.getNumberOfCards(); ++i )
			cardsToBeChecked.remove( hand.getCard(i) ); // Remove hole cards
 
		for( int i=0; i<cardsToBeChecked.size(); ++i ) {
			ArrayList<Card> newTable = new ArrayList<Card>( Arrays.asList(table) );
			newTable.add( cardsToBeChecked.get(i) );
			String handCategory = getHandCategory(hand, newTable.toArray( new Card[newTable.size()] )).toString();
			if( handCategoryValues.contains(handCategory) == true ) {
				if( outs.get(handCategory) == null )
					outs.put( handCategory, new ArrayList<Card>() );
				outs.get( handCategory ).add( cardsToBeChecked.get(i) );
			}
		}
	}

	public int getNumberOfOuts() {
		// We can't calculate number of outs pre-flop or post river
		if( table.length == 0 || table.length == 5 )
			return -1;

		if( outs.isEmpty() == true )
			calculateOuts();

		int counter=0;
		for( Map.Entry<String, ArrayList<Card>> entry : outs.entrySet() )
			if( !entry.getKey().equals("NO_PAIR") && 
				!entry.getKey().equals("PAIR") )
				counter += entry.getValue().size();
		return counter;
	}

	/**
	 * Small function that calculates pot-odds
	 */
	public double getPotOdds() {
		return (double) pot/amountToCall;
	}

	/**
	 * Small function that returns the number of cards that 
	 * remain the deck
	 */
	public int getNumberOfRemainingCards() {
		return (52-table.length-hand.getNumberOfCards());
	}

}