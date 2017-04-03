package poker;

public class AutomatedPokerPlayer extends PokerPlayer {

	public AutomatedPokerPlayer(DeckOfCards inputDeck) throws InterruptedException {
		super(inputDeck);
	}
	
	public static void main(String[] args) throws InterruptedException {	
		int numTestsToRun = 100;
		/*
		 * Here I run tests of 100 random hands that start as a random hand and discards
		 * and replenishes cards until every card in the hand has a discard
		 * probability of 0 (no more changes possible), or no more cards can be dealt.
		 */
		for(int k=0;k<numTestsToRun;k++){
			DeckOfCards deck = new DeckOfCards();
			PokerPlayer player = new PokerPlayer(deck);
			HandOfCards hand = player.hand;

			//This stores a string representation of the hand of cards before discarding
			String handBeforeDiscarding = hand.toString();

			//This is the hand type before discarding.
			String startingHandType = player.getHandType();

			int totalCardsDiscarded = 0;
			int roundsOfDiscards = 0;

			/*
			 * Stop discarding cards when there are no possible discards left. 
			 */
			while(!(player.noPossibleDiscardsLeft()) && roundsOfDiscards <50){
				totalCardsDiscarded += hand.discard();
				roundsOfDiscards++;
			}
			//This stores a String with the hand type (high hand, flush, etc.) of the hand after discarding
			String newHandType = player.getHandType();

			System.out.println("Test Number " + (k+1)+ ":");
			System.out.println("----------------------------------------------------------------------------------------");
			System.out.println("Went from a " +startingHandType+ " to a "+newHandType + " by discarding "+totalCardsDiscarded+ " cards in "+roundsOfDiscards+" rounds of discards.");
			System.out.println(handBeforeDiscarding + "  --->  " + hand);
			System.out.println("----------------------------------------------------------------------------------------\n");

		}
	}


	
}
