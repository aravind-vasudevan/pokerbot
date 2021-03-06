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

import poker.Card;
import poker.HandHoldem;
import poker.PokerMove;

import com.stevebrecher.HandEval;

/**
 * This class is the brains of your bot. Make your calculations here and return the best move with GetMove
 */
public class BotStarter implements Bot {

	/**
	 * Implement this method to return the best move you can. Currently it will return a raise the ordinal value
	 * of one of our cards is higher than 9, a call when one of the cards has a higher ordinal value than 5 and
	 * a check otherwise.
	 * @param state : The current state of your bot, with all the (parsed) information given by the engine
	 * @param timeOut : The time you have to return a move
	 * @return PokerMove : The move you will be doing
	 */
	@Override
	public PokerMove getMove(BotState state, Long timeOut) {

		// Fold if the time left in our time bank is too low (i.e. less than half a second)
		if( timeOut < 500 )
			return new PokerMove(state.getMyName(), "fold", 0);

		HandHoldem hand = state.getHand();
		HandEval.HandCategory handCategory = getHandCategory(hand, state.getTable());
		String handCategoryString = handCategory.toString();
		System.err.printf("my hand is %s, opponent action is %s, pot: %d\n", handCategory, state.getOpponentAction(), state.getPot());
		
		// Get the ordinal values of the cards in your hand
		int height1 = hand.getCard(0).getHeight().ordinal();
		int height2 = hand.getCard(1).getHeight().ordinal();

		switch( state.getStreetNumber() ) {

			// Pre-flop
			case 0: {
				if( height1+height2 > 14 || state.onButton() )
					return new PokerMove(state.getMyName(), "call", state.getAmountToCall());
				else
					return new PokerMove(state.getMyName(), "fold", 0);
				// break;
			}

			// Flop to Turn
			case 3: {
				if( handCategory.ordinal() > HandEval.HandCategory.PAIR.ordinal() || state.getNumberOfOuts() >=8 )
					return new PokerMove(state.getMyName(), "call", state.getAmountToCall());
				break;
			}

			// Turn to River
			case 4: {
				if( handCategory.ordinal() > HandEval.HandCategory.PAIR.ordinal() || state.getNumberOfOuts() >=10 )
					return new PokerMove(state.getMyName(), "call", state.getAmountToCall());
				break;
			}

			// Post River. Final round of betting.
			case 5: {
				if( handCategory.ordinal() == HandEval.HandCategory.PAIR.ordinal() )
					return new PokerMove(state.getMyName(), "call", state.getAmountToCall());
				break;
			}
		}	

		// switch( state.onButton() ) {
		// 	// We are on the button. i.e. small blind
		// 	case true: {
		// 		switch( state.getStreetNumber() ) {

		// 			// Pre-flop
		// 			case 0: {
		// 				break;
		// 			}

		// 			// Flop to Turn
		// 			case 3: {
		// 				break;
		// 			}

		// 			// Turn to River
		// 			case 4: {
		// 				break;
		// 			}

		// 			// Post River. Final round of betting.
		// 			case 5: {
		// 				break;
		// 			}
		// 		}
		// 		break;
		// 	}

		// 	// Opponent is on the button. i.e. big blind
		// 	case false: {
		// 		switch( state.getStreetNumber() ) {

		// 			// Pre-flop
		// 			case 0: {
		// 				break;
		// 			}

		// 			// Flop to Turn
		// 			case 3: {
		// 				break;
		// 			}

		// 			// Turn to River
		// 			case 4: {
		// 				break;
		// 			}

		// 			// Post River. Final round of betting.
		// 			case 5: {
		// 				break;
		// 			}
		// 		}
		// 		break;
		// 	}
		// }
	
		// Return the appropriate move according to our amazing strategy
		// if( height1 > 9 || height2 > 9 ) {
		// 	return new PokerMove(state.getMyName(), "raise", 2*state.getBigBlind());
		// } else if( height1 > 5 && height2 > 5 ) {
		// 	return new PokerMove(state.getMyName(), "call", state.getAmountToCall());
		// } else {
		// 	return new PokerMove(state.getMyName(), "check", 0);
		// }
		return new PokerMove(state.getMyName(), "check", 0);
	}
	
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
